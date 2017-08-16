package autograder.canvas;

import com.google.inject.Inject;

import autograder.canvas.responses.Assignment;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;
import autograder.portal.PortalConnection;

/** 
 * This hits a select few endpoints of the Canvas API by Instructure. 
 * @author Ryan
 *
 */
public class CanvasConnection extends Network implements PortalConnection{
	private Configuration mConfig;

	@Inject
	public CanvasConnection(Configuration config) {
		mConfig = config;
	}
	
	public CanvasConnection() {
	}
	
	@Override
	public User[] getStudents() {
		return getAllStudents(mConfig.canvasCourseId);
	}
	
	public  User[] getAllStudents(String courseId) {
		String url = String.format("courses/%s/students", configuration.canvasCourseId);
		return httpGetCall(url, User[].class);
	}
	
	public Submission[] getAllSubmissions() {
		return httpGetCall(String.format("courses/%s/assignments/%s/submissions", configuration.canvasCourseId, configuration.canvasAssignmentId), Submission[].class);
	}
	
	@Override
	public Submission[] getSubmissions() {
		return getAllSubmissions(mConfig.canvasCourseId, mConfig.canvasAssignmentId);
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

	@Override
	public User getStudentById(String id) {
		return httpGetCall(String.format("users/%s/profile", id), User.class);
	}
	
}
