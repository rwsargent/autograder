package autograder.phases;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import autograder.canvas.responses.Submission;
import autograder.filehandling.SeenSubmissions;
import autograder.filehandling.SubmissionParser;
import autograder.phases.one.StudentDownloader;
import autograder.phases.one.SubmissionDownloader;
import autograder.student.AutograderSubmissionMap;

/**
 * The Setup
 * @author ryans 2017
 *
 */
public class PhaseOne {
	protected SubmissionDownloader submissionDownloader;
	protected SubmissionBuilder submissionBuilder;
	protected SeenSubmissions seenSubmissions;
	private StudentDownloader studentDownloader;
	private SubmissionParser parser;
	
	@Inject
	public PhaseOne(SubmissionDownloader submissionDownloader, SubmissionBuilder subBuilder, SeenSubmissions seenSubmissions, StudentDownloader studentDownloader, SubmissionParser parser) {
		this.submissionDownloader = submissionDownloader;
		this.submissionBuilder = subBuilder;
		this.seenSubmissions = seenSubmissions;
		this.studentDownloader = studentDownloader;
		this.parser = parser;
	}
	
	public AutograderSubmissionMap setupSubmissions(boolean rerun, String assignment) {
		AutograderSubmissionMap submissionMap = new AutograderSubmissionMap();
		if(rerun) {
			// read submissions and meta data from disk.
		} else {
			// Get get all possible submissions
			List<Submission> fullSubmissions = submissionDownloader.downloadSubmissions();
			List<Submission> subsToRemove = new ArrayList<>();
			
			for(Submission submission : fullSubmissions) {
				if(seenSubmissions.alreadySeenSubmission(submission)) {
					subsToRemove.add(submission);
				}
			}
			fullSubmissions.remove(subsToRemove);
			
			// create AutograderSubmission
			fullSubmissions.stream()
				.map(parser::parseAndCreateSubmission)
				.peek(studentDownloader::fillUser)
				.peek(submission -> submission.writeMetaData())
				.forEach(submissionMap::addSubmission);
		}
		return submissionMap;
	}
}
