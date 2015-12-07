package autograder.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;


public abstract class AbstractProperties {
	
	protected abstract String getDefaultPropertiesLocation();
	
	protected void mapProperties(String filePath) {
		File propFile = findPropertyFile(filePath);
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propFile));
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
		fillProperties(properties, this);
	}
	
	protected void fillProperties(Properties properties, AbstractProperties object) {
		try {
			for(Object key : properties.keySet()) {
				Field field = this.getClass().getField((String)key);
				String property = properties.getProperty((String)key);
				String value = property != null && !property.isEmpty() ? property : (String) field.get(object);
				if(value == null || value.isEmpty()) {
					value = System.getProperty(field.getName());
					if(value == null) {
						throw new ConfigurationException("The required configuration " + field.getName() + " was not specified. Please look to make sure the property file has all the required fields, or add as a VM"
								+ " argument. (-DfieldName=value)");
					}
				}
				field.set(this, value);
			}
		} catch (ClassCastException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new ConfigurationException(e);
		}
	}

	public File findPropertyFile(String filePath) {
		String configurationFileName = filePath;
		if(configurationFileName == null) {
			configurationFileName = getDefaultPropertiesLocation();
		}
		
		File configFile = new File(configurationFileName);
		if(!configFile.exists()) {
			URL configUrl = getClass().getClassLoader().getResource(configurationFileName);
			if(configUrl == null) {
				throw new ConfigurationException("Could not find configuration.properties file");
			}
			configFile = new File(configUrl.getPath());
		}
		return configFile;
	}

}
