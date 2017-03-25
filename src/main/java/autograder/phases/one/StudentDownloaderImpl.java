package autograder.phases.one;

import java.util.logging.Logger;

import com.google.inject.Inject;

import autograder.canvas.responses.User;
import autograder.portal.PortalConnection;

public class StudentDownloaderImpl implements StudentDownloader {
	public static Logger LOGGER = Logger.getLogger(StudentDownloaderImpl.class.getName());
	private PortalConnection mPortal;

	@Inject
	public StudentDownloaderImpl(PortalConnection portal) {
		mPortal = portal;
	}

	@Override
	public User[] getStudents() {
		LOGGER.info("Downloading students.");
		return mPortal.getStudents();
	}

}
