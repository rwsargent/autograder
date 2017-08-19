package autograder.student;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class StudentErrorRegistry {
	private List<AutograderSubmission> mInvalidAssignmentProperties = new ArrayList<>();
	private List<AutograderSubmission> mInvalidSubmissions = new ArrayList<>();
	
	@Inject
	public StudentErrorRegistry() {
	}
	
	public void addInvalidAssignmentProperties(AutograderSubmission student) {
		mInvalidAssignmentProperties.add(student);
	}
	
	public void addInvalidSubmission(AutograderSubmission student) {
		mInvalidSubmissions.add(student);
	}
}
