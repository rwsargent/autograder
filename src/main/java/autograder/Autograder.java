package autograder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;

import autograder.canvas.CanvasConnection;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.CmdLineParser;
import autograder.configuration.Configuration;
import autograder.configuration.TeacherAssistantRegistry;
import autograder.filehandling.Bundler;
import autograder.filehandling.SubmissionParser;
import autograder.grading.Grader;
import autograder.grading.WorkJob;
import autograder.mailer.Mailer;
import autograder.student.Student;
import autograder.student.StudentMap;
import autograder.student.SubmissionPair;
import autograder.student.SubmissionPairer;
import autograder.student.SubmissionPairer.SubmissionData;
import autograder.tas.PartitionSubmissions;
import autograder.tas.TAInfo;

public class Autograder {
	
	public static Logger LOGGER = Logger.getLogger(Autograder.class.getName());
	private boolean shouldMail = false;
	private boolean onlyLate;

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Autograder grader = new Autograder();
		grader.setup(args);
		String submissionPath = Configuration.getConfiguration().submission;
		System.out.println("Submission Path at: " + submissionPath);
		grader.run(submissionPath);
		long stop = System.currentTimeMillis();
		long time = stop - start;
		long second = (time / 1000) % 60;
		long minute = (time / (1000 * 60)) % 60;

		System.out.println(String.format("Total time: %02d:%02d", minute, second));
	}
	
	public void run(String submissionPath) {
		System.out.println("Download all students");
		Map<Integer, User> userMap = buildUserMap(CanvasConnection.getAllStudents());
		System.out.println("Download Submissions");
		SubmissionParser submissionParser = new SubmissionParser();
		Submission[] canvasSubmissions = getDesiredSubmissions(userMap);
		StudentMap submissions = submissionParser.parseSubmissions(userMap, onlyLate, canvasSubmissions);
		SubmissionPairer pairer = new SubmissionPairer();
		System.out.println("Pair submissions");
		SubmissionData submissionData = pairer.pairSubmissions(submissions);
		Set<SubmissionPair> pairs = submissionData.pairs;
		// build workQueue
		Queue<WorkJob> workQueue = buildQueueFromPairs(pairs);
		maybeAddInvalidStudentToWorkQueue(workQueue, submissionData.invalidStudents);
		// execute workQueue
		System.out.println("Starting grading");
		Grader[] threads = startGraderThreads(workQueue);
//		Grader grader = new Grader(workQueue);
//		grader.run();
		// seperate submissions
		calculateTaGrading(submissionData.pairs.size());
		
		HashMap<String, Set<SubmissionPair>> studentsForTas = PartitionSubmissions.partition(TeacherAssistantRegistry.getInstance().toList(), submissionData);
		System.out.println("Partitioned students");
		//wait for grading to finish
		for(Grader graderThread : threads) {
			try {
				graderThread.join();
			} catch (InterruptedException e) {
				LOGGER.severe("You gotta be KIDDING me! Grader thread " + graderThread.getId() + " was interrupted somehow.");
			}
		}
//		try {
//			grader.join(300 * 1000);
//		} catch (InterruptedException e) {
//			LOGGER.severe("You gotta be KIDDING me! Grader thread " + grader.getId() + " was interrupted somehow.");
//		}
		System.out.println("Finished grading");
		//bundle files per ta
		Map<String, File> zippedFiles = Bundler.bundleStudents(studentsForTas);
		System.out.println("Bundled");
		// email out the zipped files of the students.
		if(shouldMail) {
			System.out.println("Emailing");
			emailTAs(zippedFiles);
		}
		LOGGER.info("Completed");
	}

	private Submission[] getDesiredSubmissions(Map<Integer, User> userMap) {
		String studentsToGradeCsv = Configuration.getConfiguration().studentsToGradeCsv;
		if (studentsToGradeCsv == null) {
			return CanvasConnection.getAllSubmissions();
		}
		
		ArrayList<Submission> submissions = new ArrayList<>();
		String[] students = studentsToGradeCsv.split(",");
		for(String student : students) {
			submissions.add(CanvasConnection.getUserSubmissions(student));
		}
		return submissions.toArray(new Submission[submissions.size()]);
	}

	private Map<Integer, User> buildUserMap(User[] allStudents) {
		HashMap<Integer, User> map = new HashMap<>();
		for(User user : allStudents) {
			map.put(user.id, user);
		}
		return map;
	}

	private void maybeAddInvalidStudentToWorkQueue(Queue<WorkJob> workQueue, Set<Student> invalidStudents) {
		for(Student student : invalidStudents) {
			if(student.assignProps != null) {
				workQueue.add(new WorkJob(student));
			}
		}
	}

	private void emailTAs(Map<String, File> zippedFiles) {
		Mailer mailer = new Mailer();
		TeacherAssistantRegistry tas = TeacherAssistantRegistry.getInstance();
		String subject = "[CS2420] Submissions for " + Configuration.getConfiguration().assignment;
		String body = "Happy grading!\n\n Tas Rule!";
		for(String ta : zippedFiles.keySet()) {
			TAInfo taInfo = tas.get(ta);
			mailer.sendMailWithAttachment(taInfo.email, subject, body, zippedFiles.get(ta));
		}
	}
	
	private Queue<WorkJob> buildQueueFromPairs(Set<SubmissionPair> pairs) {
		Queue<WorkJob> queue = new LinkedBlockingQueue<>(150);
		for(Iterator<SubmissionPair> it = pairs.iterator(); it.hasNext();) {
			SubmissionPair pair = it.next();
			queue.add(new WorkJob(pair.submitter));
			if(!pair.sorted) {
				queue.add(new WorkJob(pair.partner));
			}
		}
		return queue;
	}

	private void calculateTaGrading(int totalSubmissions) {
		Map<String, TAInfo> tas = TeacherAssistantRegistry.getInstance().getMap();
		double totalHours = tas.entrySet().stream().mapToDouble(ta -> ta.getValue().hours).sum();
		tas.forEach((name, ta) -> ta.assignmentsToGrade = (int) Math.round((ta.hours / totalHours) * totalSubmissions));	
	}

	private void setup(String[] args) {
//		System.setProperty("sun.zip.disableMemoryMapping", "true");
		CmdLineParser parser = new CmdLineParser();
		CommandLine commandLine = parser.parse(args);
		if(commandLine.hasOption("h")) {
			parser.printHelp();
			System.exit(0);
			return; // unreachable code, being explicit about control flow
		} else if (commandLine.hasOption('m') || commandLine.hasOption("mail")) {
			shouldMail = true;
		} else if (commandLine.hasOption('l') || commandLine.hasOption("late")) {
			onlyLate = true;
		}
		
		String configPath = commandLine.getOptionValue("c", Constants.DEFAULT_CONFIGURATION);
		Configuration.getConfiguration(configPath);
		
		new File(Constants.SUBMISSIONS).mkdir();
		new File(Constants.ZIPS).mkdir();
		System.out.println("Created submission folder");
	}
	
	private Grader[] startGraderThreads(Queue<WorkJob> queue) {
		int processorCount = Runtime.getRuntime().availableProcessors() - 1;
		Grader[] threads = new Grader[processorCount];
		for(int threadIdx = 0; threadIdx < threads.length; threadIdx++) {
			Grader thread = new Grader(queue);
			threads[threadIdx] = thread;
			thread.start();
		}
		
		return threads;
	}
}
