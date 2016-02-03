package autograder.canvas;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;

public class TestCanvas {

	@Test
	public void testAssignmentFileDownload() {
		String s = new String(Network.downloadFile("https://utah.instructure.com/files/54175714/download?download_frd=1&amp;verifier=cAApBod2lJEWSIKiCLX4ZRaF10wTRrwYUFNj6RBI"));
		Properties props = new Properties();
		assertTrue(s.length() > 0);
	}
	
	@Test
	public void testLink() {
		User[] students = CanvasConnection.getAllStudents();
		assertNotNull(students);
	}
	
	@Test
	public void testSubmission() {
		Submission[] submissions = CanvasConnection.getAllSubmissions();
		assertNotNull(submissions);
	}
}
