package autograder.canvas;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;

/** 
 * This hits a select few endpoints of the Canvas API by Instructure. 
 * @author Ryan
 *
 */
public class CanvasConnection extends Network {
	
	public static User[] getAllStudents() {
		String url = String.format("courses/%s/students", Configuration.getConfiguration().canvasCourseId);
		return Network.httpGetCall(url, User[].class);
	}
	
	public static Submission[] getAllSubmissions() {
		Configuration config = Configuration.getConfiguration();
		return Network.httpGetCall(String.format("courses/%s/assignments/%s/submissions", config.canvasCourseId, config.canvasAssignmentId), Submission[].class);
	}

	public static Submission[] getUserSubmissions(User student) {
		Configuration config = Configuration.getConfiguration();
		return Network.httpGetCall(String.format("courses/%s/assignments/%s/submissions/%d", config.canvasCourseId, config.canvasAssignmentId, student.id), Submission[].class);
	}
	
	public static Submission getUserSubmissions(String uid) {
		Configuration config = Configuration.getConfiguration();
		return Network.httpGetCall(String.format("courses/%s/assignments/%s/submissions/sis_user_id:%s", config.canvasCourseId, config.canvasAssignmentId, uid), Submission.class);
	}

	public static byte[] downloadFile(String url) {
		return Network.downloadFile(url);
	}
}
