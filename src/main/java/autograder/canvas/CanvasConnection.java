package autograder.canvas;

import autograder.canvas.responses.User;
import autograder.configuration.Configuration;

public class CanvasConnection {
	
	public static User[] getAllStudents() {
		String url = String.format("courses/%s/users", Configuration.getConfiguration().canvasCourseId);
		return Network.httpGetCall(url, User[].class);
	}
}
