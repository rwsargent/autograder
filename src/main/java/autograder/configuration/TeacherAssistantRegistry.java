package autograder.configuration;

import java.io.File;

import org.apache.commons.csv.CSVRecord;

import autograder.Constants;
import autograder.tas.TAInfo;

public class TeacherAssistantRegistry extends AbstractCsvRegistry<TAInfo> {
	public static TeacherAssistantRegistry instance;
	private File taFile;
	
	public synchronized static TeacherAssistantRegistry getInstance() {
		if (instance == null) {
			instance = new TeacherAssistantRegistry();
			instance.configure();
		}
		return instance;
	}
	
	private TeacherAssistantRegistry() {
		taFile = new File("tas");
		taFile.mkdirs();
	}
	
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
		taInfo.taDirectory = new File(taFile.getAbsolutePath() + "/" + taInfo.name);
		taInfo.taDirectory.mkdir();
		return taInfo;
	}

	@Override
	protected String getKey(CSVRecord record) {
		return record.get(Constants.TaConfiguration.TA_NAME_HEADER);
	}
}
