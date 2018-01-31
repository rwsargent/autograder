package autograder.testutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import autograder.AutograderTestModule;
import autograder.Constants;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

public class TestUtils {
	
	public static Configuration loadConfiguration() {
		try {
			return new Gson().fromJson(new FileReader(new File(AutograderTestModule.TEST_CONFIG_PATH)), Configuration.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
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
	
	public static AutograderSubmissionMap fillMapWithMocks(int size) {
		AutograderSubmissionMap submissions = Mockito.mock(AutograderSubmissionMap.class);
		List<AutograderSubmission> submissionList = new ArrayList<>();
		for(int i = 0; i< size; i++) {
			AutograderSubmission submission = Mockito.mock(AutograderSubmission.class);
			Mockito.when(submission.getDirectory()).thenReturn(new File(Integer.toString(i)));
			submissionList.add(submission);
		}
		Mockito.when(submissions.listStudents()).thenReturn(submissionList);
		return submissions;
	}
}
