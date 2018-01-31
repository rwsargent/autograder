package autograder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import autograder.configuration.CmdLineParser;
import autograder.configuration.Configuration;
import autograder.modules.DefaultModule;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String... args ) {
		
		Configuration configuration = null;
		setup(args);
		do {
			long starttime = System.currentTimeMillis();
			String configpath = args.length > 0 ? args[0] : Constants.DEFAULT_CONFIGURATION;
			Gson gson = new Gson();
			try(FileReader reader = new FileReader(new File(configpath))) {
				configuration = gson.fromJson(reader, Configuration.class);
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not find configuration file at " + configpath + ".", e);
				return;
			} catch (IOException e) {
				LOGGER.error("Exception thrown when trying to read configuration, ending program.", e);
				return;
			}
			
			AbstractModule module = getModule(configuration);
			LOGGER.info("Running with " + module.getClass().getSimpleName());
			if(module != null) {
				Injector injector = Guice.createInjector(module);	
				Autograder autograder = injector.getInstance(Autograder.class);
				autograder.execute();
			}
			
			if(configuration != null && configuration.runContinuously) {
				try {
					long timeout = getTimeoutDuration(starttime, configuration);
					if(timeout > 0) {
						Thread.sleep(timeout);
					}
				} catch (InterruptedException e) {
					LOGGER.error(e.getMessage(), e);
					return;
				}
			}
			logDurationOfRun(starttime);
		} while(configuration != null && configuration.runContinuously); 
	}

	private static void logDurationOfRun(long starttime) {
		long endTime = System.currentTimeMillis();
		long durationInSeconds = (endTime - starttime) / 1000;
		long convertedMinues = (durationInSeconds%3600)/60;
		long convertedSeconds = durationInSeconds%60;
		
		LOGGER.info("Run took: " + String.format("%02d:%02d",convertedMinues, convertedSeconds));
	}

	private static long getTimeoutDuration(long starttime, Configuration configuration) {
		final int defaultTimeout = 15 * 60 * 1000; // Fifteen minutes
		long minDuration = configuration.defaultDuration == 0 ? defaultTimeout : configuration.defaultDuration;
		long now = System.currentTimeMillis();
		long programDuration = now - starttime;
		return minDuration - programDuration;
	}

	private static AbstractModule getModule(Configuration configuration) {
		if (configuration.moduleClass == null) {
			return new DefaultModule(configuration);
		} else {
			try {
				Class<?> moduleClass = Main.class.getClassLoader().loadClass(configuration.moduleClass);
				Constructor<?> moduleConstructor = moduleClass.getConstructor(Configuration.class);
				return (AbstractModule) moduleConstructor.newInstance(configuration);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException 
					| SecurityException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.error("Could not load module class " + configuration.moduleClass, e);
				return null;
			}
		}
	}
	
	public static void setup(String[] args) {
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
