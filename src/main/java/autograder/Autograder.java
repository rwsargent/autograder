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

import com.google.inject.Inject;

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
import autograder.grading.jarring.Jarrer;
import autograder.mailer.Mailer;
import autograder.phases.PhaseOne;
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
	
	private Bundler mBundler;
	private Configuration mConfig;
	private CanvasConnection mConnection;
	private SubmissionParser mSubParser;
	private Mailer mMailer;
	private PartitionSubmissions partSubmissions;
	private TeacherAssistantRegistry mTARegistry;
	private PhaseOne mPhaseOne;
	private SubmissionPairer mPairer;
	
	@Inject
	public Autograder(PhaseOne phaseOne, SubmissionPairer pairer) {
		mPhaseOne = phaseOne;
		mPairer = pairer;
	}
	
	@Inject
	public Autograder(Configuration configuration, Bundler bundler, CanvasConnection connection, SubmissionParser subParser, Mailer mailer, PartitionSubmissions subPartitioner,
			TeacherAssistantRegistry taRegistry) {
		mBundler = bundler;
		mConfig = configuration;
		mConnection = connection;
		mSubParser = subParser;
		mMailer = mailer;
		partSubmissions = subPartitioner;
		mTARegistry = taRegistry;
	}
	
	public void execute() {
		StudentMap studentMap = mPhaseOne.setupSubmissions();
	}
	
	public void run() {
		System.out.println("Download all students");
		Map<Integer, User> userMap = buildUserMap(mConnection.getAllStudents(""));
		
		System.out.println("Download Submissions");
		Submission[] canvasSubmissions = getDesiredSubmissions(userMap);
		StudentMap submissions = mSubParser.parseSubmissions(userMap, canvasSubmissions);
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
		
		HashMap<String, Set<SubmissionPair>> studentsForTas = partSubmissions.partition(submissionData);
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
		Map<String, File> zippedFiles = mBundler.bundleStudents(studentsForTas);
		System.out.println("Bundled");
		// email out the zipped files of the students.
		if(shouldMail) {
			System.out.println("Emailing");
			emailTAs(zippedFiles);
		}
		LOGGER.info("Completed");
	}

	private Submission[] getDesiredSubmissions(Map<Integer, User> userMap) {
		String studentsToGradeCsv = mConfig.studentsToGradeCsv;
		if (studentsToGradeCsv == null) {
			return mConnection.getAllSubmissions();
		}
		
		ArrayList<Submission> submissions = new ArrayList<>();
		String[] students = studentsToGradeCsv.split(",");
		for(String student : students) {
			submissions.add(mConnection.getUserSubmissions(student));
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
//			if(student.assignProps != null) {?
				workQueue.add(new WorkJob(student));
//			}
		}
	}

	private void emailTAs(Map<String, File> zippedFiles) {
		String subject = "[CS2420] Submissions for " + mConfig.assignment;
		String body = "Happy grading!\n\n Tas Rule!";
		for(String ta : zippedFiles.keySet()) {
			TAInfo taInfo = mTARegistry.get(ta);
			mMailer.sendMailWithAttachment(taInfo.email, subject, body, zippedFiles.get(ta));
		}
	}
	
	private Queue<WorkJob> buildQueueFromPairs(Set<SubmissionPair> pairs) {
		Queue<WorkJob> queue = new LinkedBlockingQueue<>(210);
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
		Map<String, TAInfo> tas = mTARegistry.getMap();
		double totalHours = tas.entrySet().stream().mapToDouble(ta -> ta.getValue().hours).sum();
		tas.forEach((name, ta) -> ta.assignmentsToGrade = (int) Math.round((ta.hours / totalHours) * totalSubmissions));	
	}

	public void setup(String[] args) {
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
		
		new File(Constants.SUBMISSIONS).mkdir();
		new File(Constants.ZIPS).mkdir();
		System.out.println("Created submission folder");
	}
	
	private Grader[] startGraderThreads(Queue<WorkJob> queue) {
		int processorCount = Runtime.getRuntime().availableProcessors() - 1;
		Grader[] threads = new Grader[processorCount];
		for(int threadIdx = 0; threadIdx < threads.length; threadIdx++) {
			Grader thread = new Grader(mConfig, new Jarrer(mConfig));
			threads[threadIdx] = thread;
			thread.start();
		}
		
		return threads;
	}
	
//	public static void main(String[] args) {
//	long start = System.currentTimeMillis();
//	Autograder grader = new Autograder(new Bundler(Configuration.getConfiguration()));
//	grader.setup(args);
//	grader.run();
//	
//	long stop = System.currentTimeMillis();
//	long time = stop - start;
//	long second = (time / 1000) % 60;
//	long minute = (time / (1000 * 60)) % 60;
//
//	System.out.println(String.format("Total time: %02d:%02d", minute, second));
//}
}
