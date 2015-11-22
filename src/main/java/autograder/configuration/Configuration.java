package autograder.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public class Configuration {
	
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

	public String testClassName;
	public String smtpUsername;
	public String smtpPassword;
	public String smtpHost = "smtp.gmail.com";
	public String smtpPort = "587";
	
	private volatile static Configuration mInstance;
	public static synchronized Configuration getConfiguration() {
		if(mInstance == null) {
			mInstance = new Configuration();
			mInstance.loadConfiguration();
		}
		return mInstance;
	}
	
	private Configuration() {
		// make the default constructor private so it cannot be instantiated outside of this class
	}
	
	private void loadConfiguration() {
		File configFile = findPropertyFile();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configFile));
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
		fillProperties(properties);
		LOGGER.info("Configuration successfully loaded");
	}

	private void fillProperties(Properties properties) {
		try {
			for(Object key : properties.keySet()) {
				Field field = this.getClass().getField((String)key);
				String property = properties.getProperty((String)key);
				String value = property != null && !property.isEmpty() ? property : (String) field.get(mInstance);
				if(value == null || value.isEmpty()) {
					value = System.getProperty(field.getName());
					if(value == null) {
						throw new ConfigurationException("The required configuration " + field.getName() + " was not specified. Please look to configuration.properties, or add as a VM"
								+ " argument. (-DfieldName=value)");
					}
				}
				field.set(this, value);
			}
		} catch (ClassCastException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new ConfigurationException(e);
		}
	}

	private File findPropertyFile() {
		String confiruationFileName = System.getProperty("configuration");
		if(confiruationFileName == null) {
			confiruationFileName = Constants.DEFAULT_CONFIGURATION;
		}
		
		File configFile = new File(confiruationFileName);
		if(!configFile.exists()) {
			URL configUrl = getClass().getClassLoader().getResource(confiruationFileName);
			if(configUrl == null) {
				throw new ConfigurationException("Could not find configuration.properties file");
			}
			configFile = new File(configUrl.getPath());
		}
		return configFile;
	}
}
