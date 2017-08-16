package autograder.phases.one;

import autograder.canvas.responses.User;
import autograder.student.AutograderSubmission;

public interface StudentDownloader {
	public User[] getStudents();
	public void fillUser(AutograderSubmission submission);
}
