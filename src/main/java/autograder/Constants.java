package autograder;

import java.util.Arrays;
import java.util.List;

public interface Constants {

	public interface ClassList {
		public static final String CL_NAME_HEADER = "name";
		public static final String CL_UID_HEADER = "uid";
		public static final String CL_CANVAS_HEADER = "canvasid";
		public static final String[] CL_CSV_HEADERS = new String[] {CL_NAME_HEADER, CL_UID_HEADER, CL_CANVAS_HEADER};
	}
	
	public interface TaConfiguration {
		public static final String TA_NAME_HEADER = "name";
		public static final String TA_EMAIL_HEADER = "email";
		public static final String TA_HOURS_HEADER = "hours";
		public static final String[] TA_CSV_HEADERS = new String[] {TA_NAME_HEADER,TA_EMAIL_HEADER, TA_HOURS_HEADER};
	}
	
	public interface MetaData {
		public static final String SUBMISSION = "submission.json";
		public static final String STUDENT_INFO = "studentInfo.json";
	}
	
	public interface CanvasApi {
		public static final String SUMBISSION_COMMENT_TEXT = "comment[text_comment]";
		public static final String GROUP_COMMENT = "comment[group_comment]";
		public static final String GRADE = "submission[posted_grade]";
	}
	
	public interface SubmissionProperties {
		public static final String COMPILED = "compiled";
	}
	
	public interface Names {
		public static final String ASSIGNMENT = "assignment";
		public static final String CONFIG_PATH = "configpath";
		public static final String TAS = "tas";
	}
	
	public static final String FILE_REGEX = "([a-zA-Z\\-]+)(-late)?_(\\d*)_(\\d*)_(.*)";
	public static final String LINK_REGEX = "[<](https://utah.instructure.com/api/v1/[a-zA-Z0-9/?&=_]+)[>]; rel=\"next\"";
	public static final int NAME_GROUP = 1;
	public static final int CANVAS_ID_GROUP = 3;
	public static final int LATE_GROUP = 2;
	public static final int SUBMISSION_NAME_GROUP = 5;
	
	public static List<String> VALID_FILE_TYPES = Arrays.asList(".pdf", ".properties", ".java");
	
	public static final String DEFAULT_CONFIGURATION = "configuration.json";
	public static final String DEFAULT_CLASS_LIST = "class_list.csv";
	
	public static final String SUBMISSIONS = "submissions";
	public static final String ZIPS = "outgoing";
	public static final String BUNDLED_TIMESTAMP = "YY-MM-dd'T'kk.mm.ss";
	public static final String PREVIOUS_RUNS = "previousRuns";
	public static final String COMPILE_ERROR_FILENAME = "compile_error.txt";
}
