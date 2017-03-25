package autograder.canvas;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;

public class TestCanvas {
	
	protected CanvasConnection connection = new CanvasConnection();

	@Test
	public void testAssignmentFileDownload() {
		String s = new String(connection.downloadFile("https://utah.instructure.com/files/54175714/download?download_frd=1&amp;verifier=cAApBod2lJEWSIKiCLX4ZRaF10wTRrwYUFNj6RBI"));
		assertTrue(s.length() > 0);
	}
	
	@Test
	public void testLink() {
		User[] students = connection.getAllStudents("");
		assertNotNull(students);
	}
	
	@Test
	public void testSubmission() {
		Submission[] submissions = connection.getAllSubmissions();
		assertNotNull(submissions);
	}
}
