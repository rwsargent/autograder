package autograder;

import java.io.File;
import java.util.Queue;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;

import autograder.configuration.CmdLineParser;
import autograder.configuration.Configuration;
import autograder.configuration.StudentSubmissionRegistry;
import autograder.configuration.TeacherAssistantRegistry;
import autograder.filehandling.SubmissionReader;
import autograder.grading.Grader;
import autograder.grading.GraderThread;
import autograder.grading.WorkJob;

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
		Queue<WorkJob>workQueue = StudentSubmissionRegistry.getInstance().buildQueueFromSubmissions();
		// execute workQueue
		grade(workQueue);
	}
	
	private String setup(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CommandLine commandLine = parser.parse(args);
		if(commandLine.hasOption("h")) {
			parser.printHelp();
			System.exit(0);
			return null;
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
	
	private void grade(Queue<WorkJob> queue) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		GraderThread[] threads = new GraderThread[processorCount];
		for(int threadIdx = 0; threadIdx < threads.length; threadIdx++) {
			GraderThread thread = new GraderThread(queue, new Grader());
			threads[threadIdx] = thread;
			thread.start();
		}
		
		try {
			for(GraderThread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e ) {
			LOGGER.severe(e.getMessage());
		}
	}
}
