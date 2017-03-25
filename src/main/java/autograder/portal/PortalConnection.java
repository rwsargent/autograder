package autograder.portal;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;

public interface PortalConnection {
	public User[] getStudents();
	public Submission[] getSubmissions();
	public Submission[] getAllSubmissions();
	public Submission getUserSubmissions(String student);
}
