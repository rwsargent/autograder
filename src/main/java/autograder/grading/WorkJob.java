package autograder.grading;

import autograder.student.AutograderSubmission;

public class WorkJob {
	private AutograderSubmission mStudent;

	public WorkJob(AutograderSubmission student) {
		mStudent = student;
	}
	
	public AutograderSubmission getStudent() {
		return mStudent;
	}
	
	public String toString() {
		return mStudent.studentInfo.name;
	}
}
