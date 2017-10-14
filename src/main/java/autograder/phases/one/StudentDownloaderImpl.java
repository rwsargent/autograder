package autograder.phases.one;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.canvas.responses.User;
import autograder.portal.PortalConnection;
import autograder.student.AutograderSubmission;

public class StudentDownloaderImpl implements StudentDownloader {
	private final Logger LOGGER = LoggerFactory.getLogger(StudentDownloaderImpl.class);
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
