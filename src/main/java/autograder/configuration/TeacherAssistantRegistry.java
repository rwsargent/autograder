package autograder.configuration;

import org.apache.commons.csv.CSVRecord;

import autograder.Constants;
import autograder.tas.TAInfo;

/**
 * This class parses the csv file that is found at the specified location in Configuration,
 * and keeps track of the TAs, their hours, and their email. 
 * @author Ryan
 *
 */
public class TeacherAssistantRegistry extends AbstractCsvRegistry<TAInfo> {
	public static TeacherAssistantRegistry instance;
	
	public synchronized static TeacherAssistantRegistry getInstance() {
		if (instance == null) {
			instance = new TeacherAssistantRegistry();
			instance.configure();
		}
		return instance;
	}
	
	private TeacherAssistantRegistry() {};
	
	@Override
	protected String getFileName() {
		return Configuration.getConfiguration().taFilePath;
	}

	@Override
	protected String[] getCsvHeaders() {
		return Constants.TaConfiguration.TA_CSV_HEADERS;
	}

	@Override
	protected TAInfo constructObject(CSVRecord record) {
		TAInfo taInfo = new TAInfo(record.get(Constants.TaConfiguration.TA_NAME_HEADER), record.get(Constants.TaConfiguration.TA_EMAIL_HEADER), Double.parseDouble(record.get(Constants.TaConfiguration.TA_HOURS_HEADER)));
		return taInfo;
	}

	@Override
	protected String getKey(CSVRecord record) {
		return record.get(Constants.TaConfiguration.TA_NAME_HEADER);
	}
}
