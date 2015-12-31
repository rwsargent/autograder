package autograder.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import autograder.Constants;

public class TeacherAssistantRegistry extends AbstractCsvRegistry<TAInfo> {
	public static TeacherAssistantRegistry instance;
	private final static Logger LOGGER = Logger.getLogger(TeacherAssistantRegistry.class.getName()); 
	
	private Map<String, TAInfo> taMap = new HashMap<>();
	
	public static TeacherAssistantRegistry getInstance() {
		if (instance == null) {
			throw new ConfigurationException("You need to configure it's settings first!");
		}
		return instance;
	}
	
	public static TeacherAssistantRegistry loadConfiguration(String filename) {
		instance = new TeacherAssistantRegistry();
		CSVFormat format = CSVFormat.DEFAULT.withHeader(Constants.TaConfiguration.TA_CSV_HEADERS);
		try {
			CSVParser parser = CSVParser.parse(filename, format);
			List<CSVRecord> records = parser.getRecords();
			for(CSVRecord record : records) {
				instance.taMap.put(record.get(Constants.TaConfiguration.TA_NAME_HEADER), 
						new TAInfo(record.get(Constants.TaConfiguration.TA_NAME_HEADER), record.get(Constants.TaConfiguration.TA_EMAIL_HEADER), Double.parseDouble(record.get(Constants.TaConfiguration.TA_HOURS_HEADER))));
			}
		} catch (IOException e) {
			LOGGER.severe("An issue with the CSV for TAs. " + e.getMessage());
		}
		return instance;
	}

	public Object getValue(String taName, String columnHeader) {
		TAInfo ta = taMap.get(taName);
		Object value;
		try {
			value = ta.getClass().getField(columnHeader).get(ta);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new ConfigurationException(e);
		}
		return value;
	}
	
	private TeacherAssistantRegistry() {}
	
	@Override
	protected String getFileName() {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] getCsvHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected autograder.configuration.TAInfo constructObject(CSVRecord record) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getKey(CSVRecord record) {
		// TODO Auto-generated method stub
		return null;
	}
}
