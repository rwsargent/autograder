package autograder.grading;

import autograder.configuration.Student;

public class WorkJob {
	private Student mStudent;

	public WorkJob(Student student) {
		mStudent = student;
	}
	
	public void gradeStudentSubmission(Grader grader) {
		grader.compileAndRunTester(mStudent);
	}
}
