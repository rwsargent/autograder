package autograder.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public abstract class AbstractCsvRegistry<RegistryObject> {
	
	protected Map<String, RegistryObject> map;
	protected static Logger LOGGER;
	
	protected void configure() {
		map = new HashMap<>();
		File csvFile = new File(getFileName());
		String fullPath = csvFile.getAbsolutePath();
		if(csvFile.exists()) {
			fullPath = csvFile.getAbsolutePath();
		} else {
			URL url;
			if ((url = getClass().getResource(csvFile.getAbsolutePath())) == null) {
				throw new ConfigurationException("Can't find " + getFileName() + ".");
			}
			fullPath = url.getPath();
		}
		CSVFormat format = CSVFormat.DEFAULT.withHeader(getCsvHeaders());
		try(CSVParser parser = CSVParser.parse(fullPath, format)) {
			List<CSVRecord> records = parser.getRecords();
			for(CSVRecord record : records) {
				map.put(getKey(record), constructObject(record));
			}
		} catch (IOException e) {
			LOGGER.severe("An issue with CSV parsing " + e.getMessage());
		}
	}
	
	public RegistryObject get(String key) {
		return map.get(key);
	}
	
	public Map<String, RegistryObject> getMap() {
		return map;
	}
	
	public List<RegistryObject> toList() {
		return new ArrayList<RegistryObject>(map.values());
	}
	
	protected abstract String getFileName();
	protected abstract String[] getCsvHeaders();
	protected abstract RegistryObject constructObject(CSVRecord record);
	protected abstract String getKey(CSVRecord record);
}
