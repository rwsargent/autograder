package autograder.grading;

import autograder.student.Student;

public class WorkJob {
	private Student mStudent;

	public WorkJob(Student student) {
		mStudent = student;
	}
	
	public void gradeStudentSubmission(Grader grader) {
		grader.compileAndRunTester(mStudent);
	}
	
	public Student getStudent() {
		return mStudent;
	}
}
