package autograder.portal;

import java.io.InputStream;
import java.util.Map;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;

public interface PortalConnection {
	public User[] getStudents();
	public User getStudentById(String id);
	public Submission[] getSubmissions();
	public Submission[] getAllSubmissions();
	public Submission getUserSubmissions(String student);
	public InputStream downloadFile(String url);
	public void gradeStudentSubmission(String student, String assignment, Map<String, String> data);
	
	public Submission[] getAllSubmissions(String courseId, String assignmentId);
}
