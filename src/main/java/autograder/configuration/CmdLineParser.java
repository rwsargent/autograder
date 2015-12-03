package autograder.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CmdLineParser {
	private Options mOptions;
	
	public CmdLineParser() {
		buildOptions();
	}
	
	private void buildOptions() {
		Option testFileName = Option.builder("g").required().longOpt("grader")
				.hasArg().numberOfArgs(1)
				.desc("The name of the Java grader class, package included. E.g. package02.SortUtilGrader")
				.build();
		
		Option helpOption = Option.builder("h").longOpt("help").desc("Help").required(false).build();
		Option pathToTaCSV = Option.builder("").longOpt("tacsv").desc("Path to the TA csv.")
				.hasArg().numberOfArgs(1)
				.required(false)
				.build();
		
		Option pathToConfiguration = Option.builder("c").longOpt("config").desc("Path to configuration.properties")
				.argName("configPath").hasArg().numberOfArgs(1)
				.required(false)
				.build();
		
		Option pathToSubmissions = Option.builder("s").longOpt("submission").desc("Path to the submission zip file")
				.argName("submissionPath").hasArg().numberOfArgs(1)
				.required(false)
				.build();
		
		mOptions = new Options();
		mOptions.addOption(testFileName);
		mOptions.addOption(helpOption);
		mOptions.addOption(pathToTaCSV);
		mOptions.addOption(pathToConfiguration);
		mOptions.addOption(pathToSubmissions);
	}
	
	public void printHelp() {
		HelpFormatter help = new HelpFormatter();
		help.printHelp("autograder", mOptions);
	}

	public CommandLine parse(String[] commandLineString) {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(mOptions, commandLineString);
			return commandLine;
		} catch (ParseException e) {
			throw new ConfigurationException(e);
		}
	}
	
}
