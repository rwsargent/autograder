package autograder.student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autograder.canvas.responses.User;

public class AutograderSubmissionMap {
	private Map<String, AutograderSubmission> mUIdMap;
	private Map<Integer, AutograderSubmission> mCanvasMap;
	private List<AutograderSubmission> submissions;
	
	public AutograderSubmissionMap() {
		mUIdMap = new HashMap<>();
		mCanvasMap = new HashMap<>();
		submissions = new ArrayList<>();
	}
	
	public AutograderSubmissionMap(User[] students) {
		this();
		fillMaps(students);
	}
	
	private void fillMaps(User[] students) {
		for(User user : students) {
			addStudent(user);
		}
	}

	@Deprecated
	public void addStudent(User user) {
		AutograderSubmission student = new AutograderSubmission(user);
		mUIdMap.put(user.sis_user_id, student);
		mCanvasMap.put(user.id, student);
		submissions.add(student);
	}
	
	public void addSubmission(AutograderSubmission submission) {
		mUIdMap.put(submission.studentInfo.sis_user_id, submission);
		mCanvasMap.put(submission.studentInfo.id, submission);
		submissions.add(submission);
	}

	public AutograderSubmission get(int canvasId) {
		return mCanvasMap.get(canvasId);
	}
	
	public AutograderSubmission get(String uId) {
		return mUIdMap.get(uId);
	}
	
	public List<AutograderSubmission> listStudents() {
		return submissions;
	}
	
	public int size() {
		return submissions.size();
	}
}
