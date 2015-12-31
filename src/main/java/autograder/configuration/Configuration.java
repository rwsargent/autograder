package autograder.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.sun.istack.internal.Nullable;

import autograder.Constants;

public class Configuration extends AbstractProperties {
	
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

	public String graderName;
	
	public String smtpUsername;
	public String smtpPassword;
	public String smtpHost = "smtp.gmail.com";
	public String smtpPort = "587";
	
	public String canvasUsername;
	public String canvasPassword;
	public String canvasCourseId;
	
	public String taFilePath;
	public String studentFilePath;
	
	
	private volatile static Configuration mInstance;
	
	public static synchronized Configuration getConfiguration() {
		return getConfiguration(null);
	}
	
	public static synchronized Configuration getConfiguration(@Nullable String configPath) {
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
