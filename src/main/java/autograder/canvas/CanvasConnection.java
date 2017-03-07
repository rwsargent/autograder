package autograder.canvas;

import autograder.canvas.responses.Assignment;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;

/** 
 * This hits a select few endpoints of the Canvas API by Instructure. 
 * @author Ryan
 *
 */
public class CanvasConnection extends Network {
	
	public  User[] getAllStudents() {
		String url = String.format("courses/%s/students", configuration.canvasCourseId);
		return httpGetCall(url, User[].class);
	}
	
	public Submission[] getAllSubmissions() {
		return httpGetCall(String.format("courses/%s/assignments/%s/submissions", configuration.canvasCourseId, configuration.canvasAssignmentId), Submission[].class);
	}
	
	public Submission[] getAllSubmissions(String courseId, String assignmentId) {
		return httpGetCall(String.format("courses/%s/assignments/%s/submissions",courseId, assignmentId), Submission[].class);
	}

	public Submission[] getUserSubmissions(User student) {
		return httpGetCall(String.format("courses/%s/assignments/%s/submissions/%d", configuration.canvasCourseId, configuration.canvasAssignmentId, student.id), Submission[].class);
	}
	
	public Submission getUserSubmissions(String uid) {
		return httpGetCall(String.format("courses/%s/assignments/%s/submissions/sis_user_id:%s", configuration.canvasCourseId, configuration.canvasAssignmentId, uid), Submission.class);
	}

	public Assignment[] getAllAssignments(String courseId) {
		return httpGetCall(String.format("courses/%s/assignments", courseId), Assignment[].class);
	}
}
