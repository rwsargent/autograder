package autograder.phases.one;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import autograder.canvas.responses.Submission;
import autograder.portal.PortalConnection;

public class SubmissionsFromWeb implements SubmissionDownloader {
	private PortalConnection portal;
	private SubmissionFilter filter;
	
	@Inject
	public SubmissionsFromWeb(PortalConnection portal, SubmissionFilter submissionFilter) {
		this.portal = portal;
		this.filter = submissionFilter;
	}
	
	@Override
	public List<Submission> downloadSubmissions() {
		ArrayList<Submission> submissions = new ArrayList<>();
		Submission[] allSubmissions = portal.getAllSubmissions();
		
		for(Submission possibleSubmission : allSubmissions) {
			if(filter.allow(possibleSubmission)) {
				submissions.add(possibleSubmission);
			}
		}
		return submissions;
		
	}
}
