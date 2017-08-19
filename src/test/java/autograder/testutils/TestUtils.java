package autograder.testutils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

import autograder.AutograderTestModule;
import autograder.Constants;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;

public class TestUtils {
	
	public static Configuration loadConfiguration() {
		return new Configuration(AutograderTestModule.TEST_CONFIG_PATH);
	}
	
	public static AutograderSubmission getSubmission(String directory) {
		File dir = new File("src/test/resources/submissions/");
		AutograderSubmission autograderSubmission = new AutograderSubmission(getMetaData(new File(dir, directory), Constants.MetaData.SUBMISSION, Submission.class), dir);
		autograderSubmission.setUser(getMetaData(autograderSubmission.getDirectory(), Constants.MetaData.STUDENT_INFO, User.class));
		
		return autograderSubmission;
	}

	public static <T> T getMetaData(File dir, String name, Class<T> clazz) {
		try(FileReader fr = new FileReader(new File(dir, "meta/" + name))) {
			Gson gson = new Gson();
			return gson.fromJson(fr, clazz);
		} catch(IOException e) {
			throw new AssertionError(e);
		}
	}

}
