package autograder.student;

import java.util.ArrayList;
import java.util.List;

public class StudentErrorRegistry {
	public static StudentErrorRegistry mInstance;
	
	private List<Student> mInvalidAssignmentProperties;
	
	public synchronized static StudentErrorRegistry getInstance() {
		if(mInstance == null ){
			mInstance = new StudentErrorRegistry();
		}
		return mInstance;
	}
	
	private StudentErrorRegistry() {
		mInvalidAssignmentProperties = new ArrayList<>();
	}
	
	public void addInvalidAssignmentProperties(Student student) {
		mInvalidAssignmentProperties.add(student);
	}
}
