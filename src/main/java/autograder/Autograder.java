package autograder;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;

import autograder.configuration.CmdLineParser;
import autograder.configuration.Configuration;
import autograder.configuration.TeacherAssistantRegistry;
import autograder.filehandling.SubmissionReader;
import autograder.grading.Grader;
import autograder.grading.WorkJob;
import autograder.student.ClassListRegistry;
import autograder.student.Student;
import autograder.student.StudentInfo;
import autograder.student.StudentSubmissionRegistry;
import autograder.student.SubmissionPair;
import autograder.tas.PartitionSubmissions;
import autograder.tas.TAInfo;

public class Autograder {
	
	public static Logger LOGGER = Logger.getLogger(Autograder.class.getName());

	public static void main(String[] args) {
		Autograder grader = new Autograder();
		String submissionPath = grader.setup(args);
		System.out.println("Submission Path at: " + submissionPath);
		grader.run(submissionPath);
	}
	
	public void run(String submissionPath) {
		// get assignments
		SubmissionReader reader = new SubmissionReader();
		reader.unzipSubmissions(submissionPath);
		// build workQueue
		Queue<WorkJob> workQueue = StudentSubmissionRegistry.getInstance().buildQueueFromSubmissions();
		// execute workQueue
		Grader[] threads = startGraderThreads(workQueue);
		// pair submissions
		Set<SubmissionPair> pairs = pairSubmissions();
		//wait for grading to finish
		for(Grader graderThread : threads) {
			try {
				graderThread.join();
			} catch (InterruptedException e) {
				LOGGER.severe("You gotta be KIDDING me! Grader thread " + graderThread.getId() + " was interrupted somehow.");
			}
		}
		// combine, and email the tas
		calculateTaGrading(pairs.size());
		PartitionSubmissions.partition(TeacherAssistantRegistry.getInstance().toList(),
				pairs.stream().collect(Collectors.toList()));
	}
	
	private void calculateTaGrading(int totalSubmissions) {
		Map<String, TAInfo> tas = TeacherAssistantRegistry.getInstance().getMap();
		double totalHours = tas.entrySet().stream().mapToDouble(ta -> ta.getValue().hours).sum();
		tas.forEach((name, ta) -> ta.assignmentsToGrade = (int) Math.round((ta.hours / totalHours) * totalSubmissions));
	}

	private Set<SubmissionPair> pairSubmissions() {
		Set<SubmissionPair> pairs = new HashSet<>();
		Set<Student> invalidProperties = new HashSet<>();
		StudentSubmissionRegistry registry = StudentSubmissionRegistry.getInstance();
		Map<String, StudentInfo> classList = ClassListRegistry.getInstance().getMap();
		StudentSubmissionRegistry.getInstance().forEach((name, student) -> {
			if(student.assignProps != null) {
				invalidProperties.add(student);
				return;
			}
			Student submitter, partner;
			if(student.assignProps.submitted) {
				submitter = student;
				partner = registry.getStudentById(classList.get(student.assignProps.partner_uid).canvasid);
			} else {
				partner = student;
				submitter = registry.getStudentById(classList.get(student.assignProps.partner_uid).canvasid);
			}
			SubmissionPair pair = new SubmissionPair(submitter, partner);
			pairs.add(pair);
		});
		return pairs;
	}

	private String setup(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CommandLine commandLine = parser.parse(args);
		if(commandLine.hasOption("h")) {
			parser.printHelp();
			System.exit(0);
			return null; // unreachable code, being explicit about control flow
		}
		
		String configPath = commandLine.getOptionValue("c", null);
		Configuration config = Configuration.getConfiguration(configPath);
		
		config.graderName = commandLine.getOptionValue("g");
		
		//load TA config
		String taFilePath = commandLine.getOptionValue('a', "ta.csv");
		taFilePath = getClass().getClassLoader().getResource(taFilePath).toString();
		Configuration.getConfiguration().taFilePath = taFilePath;
 		System.out.println("TA Filepath found at " + taFilePath + " and loaded successfully.");
		new File(Constants.SUBMISSIONS).mkdir();
		System.out.println("Created submission folder");
		
		return commandLine.getOptionValue("s");
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
