package autograder.grading;

import autograder.student.Student;

public class WorkJob {
	private Student mStudent;

	public WorkJob(Student student) {
		mStudent = student;
	}
	
	public Student getStudent() {
		return mStudent;
	}
	
	public String toString() {
		return mStudent.name;
	}
}
