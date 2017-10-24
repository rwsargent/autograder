package autograder.canvas;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

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
//		String s = new String(connection.downloadFile("https://utah.instructure.com/files/54175714/download?download_frd=1&amp;verifier=cAApBod2lJEWSIKiCLX4ZRaF10wTRrwYUFNj6RBI"));
//		assertTrue(s.length() > 0);
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
	
	@Test
	public void testPut() {
		Configuration config = new Configuration(AutograderTestModule.TEST_CONFIG_PATH);
		config.canvasToken = "";// REDACTED - fill with token to retest;
		config.canvasCourseId = "460997";
		connection = new CanvasConnection(config);
		Map<String, String> data = new HashMap<>();
		data.put("comment[text_comment]", "Test for Java, baby!");
		connection.gradeStudentSubmission("1795440", "4329488", data);
	}
}
