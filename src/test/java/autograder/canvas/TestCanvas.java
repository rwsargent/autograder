package autograder.canvas;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import autograder.AutograderTestModule;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;

public class TestCanvas {
	
	protected CanvasConnection connection;
	
	@Before
	public void setup() {
		Configuration config = new Configuration(AutograderTestModule.TEST_CONFIG_PATH);
		connection = new CanvasConnection(config);
	}

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
