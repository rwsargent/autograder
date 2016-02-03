package autograder.canvas;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;

public class CanvasConnection {
	
	public static User[] getAllStudents() {
		String url = String.format("courses/%s/students", Configuration.getConfiguration().canvasCourseId);
		return Network.httpGetCall(url, User[].class);
	}
	
	public static Submission[] getAllSubmissions() {
		Configuration config = Configuration.getConfiguration();
		return Network.httpGetCall(String.format("courses/%s/assignments/%s/submissions", config.canvasCourseId, config.canvasAssignmentId), Submission[].class);
	}
}
