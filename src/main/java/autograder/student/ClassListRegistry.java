package autograder.student;

import org.apache.commons.csv.CSVRecord;

import autograder.Constants;
import autograder.Constants.ClassList;
import autograder.canvas.CanvasConnection;
import autograder.configuration.AbstractCsvRegistry;

/**
 * Ideally, you want to use the Canvas API to download all possible students. If you don't have access to the API, 
 * then this is a singleton that will read in a csv of all the students, their Canvas id, and their UID. 
 * @author Ryan
 * @deprecated Use {@link CanvasConnection#getAllStudents()} instead of a flat CSV file. 
 */
@Deprecated
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
