package autograder.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import autograder.Constants;

public class Configuration extends AbstractProperties {
	
	public Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

	public String assignment; 
	
	public String graderClassName;
	public String graderFile;
	public String extraClassPathFiles;
	
	public String smtpUsername;
	public String smtpPassword;
	public String smtpHost = "smtp.gmail.com";
	public String smtpPort = "587";
	public String senderEmail;
	
	public String canvasUsername;
	public String canvasPassword;
	public String canvasCourseId;
	public String canvasAssignmentId;
	
	public String canvasToken;
	
	public String taFilePath = "ta.csv";
	public String studentFilePath;
	public String submission;
	
	public String ignorePattern;
	
	public String studentsToGradeCsv;
	
	public String validFileExtensions;
	public String validFileNames;
	
	@Optional(defaultValue="")
	public String mainClass;
	
	@Optional(defaultValue="")
	public String extraBundledFilesCsv;

	@Optional
	public String junitPlugin;
	
	@Optional(defaultValue="")
	public String graderJVMOptions;
	
	@Optional(defaultValue = "60")
	public String timeout;

	@Optional
	public String outputDestination;

	public int defaultDuration;

	public boolean rerun;

	public boolean runContinuously;

	public int adjustedTotal;

	public String moduleClass;

	public int threadCount;

	public long phaseTwoTimeout;
	
	@Inject
	public Configuration(@Named("configpath") String configPath) {
		loadConfiguration(findPropertyFile(configPath));
	}
	
	private void loadConfiguration(File configFile) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configFile));
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
		fillProperties(properties, this);
		LOGGER.info("Configuration successfully loaded.");
	}

	@Override
	protected String getDefaultPropertiesLocation() {
		return Constants.DEFAULT_CONFIGURATION;
	}
}
