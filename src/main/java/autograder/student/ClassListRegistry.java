package autograder.student;

import org.apache.commons.csv.CSVRecord;

import com.google.inject.Inject;

import autograder.Constants;
import autograder.Constants.ClassList;
import autograder.canvas.CanvasConnection;
import autograder.configuration.AbstractCsvRegistry;
import autograder.configuration.Configuration;

/**
 * Ideally, you want to use the CanvasApi API to download all possible students. If you don't have access to the API, 
 * then this is a singleton that will read in a csv of all the students, their CanvasApi id, and their UID. 
 * @author Ryan
 * @deprecated Use {@link CanvasConnection#getAllStudents()} instead of a flat CSV file. 
 */
@Deprecated
public class ClassListRegistry extends AbstractCsvRegistry<StudentInfo> {

	@Inject
	public ClassListRegistry(Configuration configuration) {
		super(configuration);
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
