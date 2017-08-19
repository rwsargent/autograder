package autograder;

import java.io.File;

import org.apache.commons.cli.CommandLine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import autograder.configuration.CmdLineParser;
import autograder.configuration.Configuration;
import autograder.modules.DefaultModule;

public class Main {
	
	public static void main(String... args ) {
		Configuration configuration = new Configuration(Constants.DEFAULT_CONFIGURATION);
		setup(args, configuration);
		AbstractModule module = getModule(configuration);
		
		Injector injector = Guice.createInjector(module);
		Autograder autograder = injector.getInstance(Autograder.class);
		
		autograder.execute();
	}

	private static DefaultModule getModule(Configuration configuration) {
		return new DefaultModule(configuration);
	}
	
	public static void setup(String[] args, Configuration configuration) {
		CmdLineParser parser = new CmdLineParser();
		CommandLine commandLine = parser.parse(args);
		if(commandLine.hasOption("h")) {
			parser.printHelp();
			System.exit(0);
			return; // unreachable code, being explicit about control flow
		} else if (commandLine.hasOption('m') || commandLine.hasOption("mail")) {
//			shouldMail = true;
		} 
		
		new File(Constants.SUBMISSIONS).mkdir();
		new File(Constants.ZIPS).mkdir();
		System.out.println("Created submission folder");
	}
}
