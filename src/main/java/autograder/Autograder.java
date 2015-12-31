package autograder;

import java.io.File;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;

import autograder.configuration.CmdLineParser;
import autograder.configuration.Configuration;
import autograder.configuration.TeacherAssistantRegistry;
import autograder.filehandling.SubmissionReader;
import autograder.grading.Grader;
import autograder.grading.WorkJob;
import autograder.student.StudentSubmissionRegistry;
import autograder.student.SubmissionPair;

public class Autograder {
	
	public static Logger LOGGER = Logger.getLogger(Autograder.class.getName());

	public static void main(String[] args) {
		Autograder grader = new Autograder();
		String submissionPath = grader.setup(args);
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
		pairSubmissions();
		// email tas
	}
	
	private void pairSubmissions() {
		Set<SubmissionPair> pairs = new HashSet<>();
		StudentSubmissionRegistry.getInstance().forEach((name, student) -> {
			
		});
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
		TeacherAssistantRegistry.loadConfiguration(taFilePath);
		
		new File(Constants.SUBMISSIONS).mkdir();
		
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
