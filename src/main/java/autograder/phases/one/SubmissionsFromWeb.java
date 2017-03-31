package autograder.phases.one;

import java.util.ArrayList;
import java.util.Map;

import com.google.inject.Inject;

import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.configuration.Configuration;
import autograder.filehandling.SubmissionParser;
import autograder.portal.PortalConnection;
import autograder.student.StudentMap;

public class SubmissionsFromWeb implements SubmissionDownloader {
	private Configuration mConfig;
	private PortalConnection mPortal;
	private SubmissionParser parser;
	
	@Inject
	public SubmissionsFromWeb(Configuration configuration, PortalConnection portal, SubmissionParser parser) {
		mConfig = configuration;
		mPortal = portal;
		this.parser = parser; 
	}
	
	@Override
	public StudentMap parseSubmission(Map<Integer, User> users) {
		return parser.parseSubmissions(users, getDesiredSubmissions(users));
	}

	private Submission[] getDesiredSubmissions(Map<Integer, User> userMap) {
		String studentsToGradeCsv = mConfig.studentsToGradeCsv;
		if (studentsToGradeCsv == null) {
			return mPortal.getAllSubmissions();
		}
		
		ArrayList<Submission> submissions = new ArrayList<>();
		String[] students = studentsToGradeCsv.split(",");
		for(String student : students) {
			submissions.add(mPortal.getUserSubmissions(student));
		}
		return submissions.toArray(new Submission[submissions.size()]);
	}
}
