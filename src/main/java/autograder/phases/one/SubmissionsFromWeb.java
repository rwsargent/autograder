package autograder.phases.one;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;

import autograder.canvas.responses.Submission;
import autograder.configuration.Configuration;
import autograder.portal.PortalConnection;

public class SubmissionsFromWeb implements SubmissionDownloader {
	private Configuration mConfig;
	private PortalConnection mPortal;
	
	@Inject
	public SubmissionsFromWeb(Configuration configuration, PortalConnection portal) {
		mConfig = configuration;
		mPortal = portal;
	}
	
	@Override
	public List<Submission> downloadSubmissions() {
		String studentsToGradeCsv = mConfig.studentsToGradeCsv;
		if (studentsToGradeCsv == null) {
			return Arrays.asList(mPortal.getAllSubmissions());
		}
		
		ArrayList<Submission> submissions = new ArrayList<>();
		String[] students = studentsToGradeCsv.split(",");
		for(String student : students) {
			submissions.add(mPortal.getUserSubmissions(student));
		}
		return submissions;
	}
}
