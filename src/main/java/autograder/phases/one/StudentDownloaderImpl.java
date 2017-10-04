package autograder.phases.one;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.inject.Inject;

import autograder.canvas.responses.User;
import autograder.portal.PortalConnection;
import autograder.student.AutograderSubmission;

public class StudentDownloaderImpl implements StudentDownloader {
	public static Logger LOGGER = Logger.getLogger(StudentDownloaderImpl.class.getName());
	private PortalConnection mPortal;
	
	private Map<Integer, User> userMap;

	@Inject
	public StudentDownloaderImpl(PortalConnection portal) {
		mPortal = portal;
	}

	@Override
	public User[] getStudents() {
		LOGGER.info("Downloading students.");
		return mPortal.getStudents();
	}

	@Override
	public void fillUser(AutograderSubmission submission) {
		if(userMap == null) {
			userMap = new HashMap<>();
			User[] users = getStudents();
			for(User user : users) {
				userMap.put(user.id, user);
			}
		}
		User studentById = userMap.get(submission.submissionInfo.user_id);
		submission.setUser(studentById);
	}
}
