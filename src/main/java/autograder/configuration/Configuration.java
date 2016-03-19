package autograder.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import autograder.Constants;

public class Configuration extends AbstractProperties {
	
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());


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
	public String classList;
	public String studentFilePath;
	public String submission;
	
	public String ignorePattern;
	
	public String studentsToGradeCsv;
	public String extraBundledFilesCsv;
	
	
	private volatile static Configuration mInstance;
	
	public static synchronized Configuration getConfiguration() {
		return getConfiguration(null);
	}
	
	public static synchronized Configuration getConfiguration(String configPath) {
		if(mInstance == null) {
			mInstance = new Configuration();
			File configFile = mInstance.findPropertyFile(configPath);
			mInstance.loadConfiguration(configFile);
		}
		return mInstance;
	}
	
	private Configuration() {
		// make the default constructor private so it cannot be instantiated outside of this class
	}
	
	private void loadConfiguration(File configFile) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configFile));
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
		fillProperties(properties, mInstance);
		LOGGER.info("Configuration successfully loaded");
	}

	@Override
	protected String getDefaultPropertiesLocation() {
		return Constants.DEFAULT_CONFIGURATION;
	}
}
