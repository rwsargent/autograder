package autograder.portal;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;

public interface PortalConnection {
	public User[] getStudents();
	public User getStudentById(String id);
	public Submission[] getSubmissions();
	public Submission[] getAllSubmissions();
	public Submission getUserSubmissions(String student);
	public byte[] downloadFile(String url);
}
