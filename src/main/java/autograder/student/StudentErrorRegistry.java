package autograder.student;

import java.util.ArrayList;
import java.util.List;

public class StudentErrorRegistry {
	public static StudentErrorRegistry mInstance;
	
	private List<AutograderSubmission> mInvalidAssignmentProperties = new ArrayList<>();
	private List<AutograderSubmission> mInvalidSubmissions = new ArrayList<>();
	
	public synchronized static StudentErrorRegistry getInstance() {
		if(mInstance == null ){
			mInstance = new StudentErrorRegistry();
		}
		return mInstance;
	}
	
	private StudentErrorRegistry() {
		mInvalidAssignmentProperties = new ArrayList<>();
	}
	
	public void addInvalidAssignmentProperties(AutograderSubmission student) {
		mInvalidAssignmentProperties.add(student);
	}
	
	public void addInvalidSubmission(AutograderSubmission student) {
		mInvalidSubmissions.add(student);
	}
}
