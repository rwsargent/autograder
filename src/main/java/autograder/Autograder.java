package autograder;

import org.apache.commons.cli.CommandLine;

import autograder.configuration.CmdLineParser;
import autograder.configuration.Configuration;
import autograder.configuration.TeacherAssistantRegistry;

public class Autograder {

	public static void main(String[] args) {
		Autograder grader = new Autograder();
		grader.setup(args);
		grader.run();
	}
	
	public void run() {
		// get assignments
		
		// build workQueue
		
		// execute workQueue
	}
	
	private void setup(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		CommandLine commandLine = parser.parse(args);
		if(commandLine.hasOption("h")) {
			parser.printHelp();
			System.exit(0);
			return;
		}
		
		String configPath = commandLine.getOptionValue("c", null);
		Configuration config = Configuration.getConfiguration(configPath);
		
		config.testClassName = commandLine.getOptionValue("t");
		
		//load TA config
		String taFilePath = commandLine.getOptionValue('a', "ta.csv");
		taFilePath = getClass().getClassLoader().getResource(taFilePath).toString();
		TeacherAssistantRegistry.loadConfiguration(taFilePath);
	}
}
