package autograder.configuration;

import java.io.IOException;
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
	
	public void configure() {
		map = new HashMap<>();
		String filename = getFileName();
		CSVFormat format = CSVFormat.DEFAULT.withHeader(getCsvHeaders());
		try {
			CSVParser parser = CSVParser.parse(filename, format);
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

	protected abstract String getFileName();
	protected abstract String[] getCsvHeaders();
	protected abstract RegistryObject constructObject(CSVRecord record);
	protected abstract String getKey(CSVRecord record);
}
