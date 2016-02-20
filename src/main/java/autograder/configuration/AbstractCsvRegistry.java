package autograder.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * The idea behind this class to handling parsing CSV in a generic fashion. It will fill map,
 * with a key from a specified value of the CSV, with objects of the Type the subclass of {@link AbstractCsvRegistry} is
 * parameterized with. 
 * @author Ryan
 *
 * @param <RegistryObject> This is an object that represents a row in the CSV file.
 */
public abstract class AbstractCsvRegistry<RegistryObject> {
	
	protected Map<String, RegistryObject> map;
	protected static Logger LOGGER;
	
	protected void configure() {
		map = new HashMap<>();
		File csvFile = new File(getFileName());
		if(!csvFile.exists()) {
			throw new ConfigurationException("Can't find " + getFileName() + ".");
		}
		CSVFormat format = CSVFormat.DEFAULT.withHeader(getCsvHeaders()).withSkipHeaderRecord();
		try(CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(),  format)) {
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
