package autograder.student;

import org.apache.commons.csv.CSVRecord;

import autograder.Constants;
import autograder.Constants.ClassList;
import autograder.configuration.AbstractCsvRegistry;


public class ClassListRegistry extends AbstractCsvRegistry<StudentInfo> {

	private static ClassListRegistry mInstance;
	
	public static ClassListRegistry getInstance() {
		if(mInstance == null) {
			mInstance = new ClassListRegistry();
			mInstance.configure();
			mInstance.map.put("-1", new StudentInfo("placeholder", "-1", "-1"));
		}
		return mInstance;
	}
	
	@Override
	protected String getFileName() {
		return Constants.DEFAULT_CLASS_LIST;
	}

	@Override
	protected String[] getCsvHeaders() {
		return ClassList.CL_CSV_HEADERS;
	}

	@Override
	protected StudentInfo constructObject(CSVRecord record) {
		return new StudentInfo(record.get(ClassList.CL_NAME_HEADER),
				record.get(ClassList.CL_UID_HEADER), 
				record.get(ClassList.CL_CANVAS_HEADER));
	}

	@Override
	protected String getKey(CSVRecord record) {
		return record.get(ClassList.CL_UID_HEADER);
	}

}
