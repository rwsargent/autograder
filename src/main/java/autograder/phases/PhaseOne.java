package autograder.phases;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;

import autograder.Constants;
import autograder.canvas.responses.Submission;
import autograder.canvas.responses.User;
import autograder.filehandling.SeenSubmissions;
import autograder.filehandling.SubmissionParser;
import autograder.phases.one.StudentDownloader;
import autograder.phases.one.SubmissionBuilder;
import autograder.phases.one.SubmissionDownloader;
import autograder.student.AutograderSubmission;
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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PhaseOne.class);
	
	@Inject
	public PhaseOne(SubmissionDownloader submissionDownloader, SubmissionBuilder subBuilder, SeenSubmissions seenSubmissions, StudentDownloader studentDownloader, SubmissionParser parser) {
		this.submissionDownloader = submissionDownloader;
		this.submissionBuilder = subBuilder;
		this.seenSubmissions = seenSubmissions;
		this.studentDownloader = studentDownloader;
		this.parser = parser;
	}
	
	public AutograderSubmissionMap setupSubmissions(String assignment, boolean rerun) {
		AutograderSubmissionMap submissionMap = new AutograderSubmissionMap();
		if(rerun) {
			File assignmentDirectory = Paths.get("submissions", assignment).toFile();
			if(!assignmentDirectory.exists()) {
				throw new IllegalStateException("Can't find " + assignmentDirectory.getAbsolutePath() + ". Are you sure you should be re-running?");
			}
			// read submissions and meta data from disk.
			Gson gson = new Gson();
			for (File assignmentDir : assignmentDirectory.listFiles()) {
				// read meta files
				try {
					File meta = new File(assignmentDir, "meta");
					Submission submission = gson.fromJson(FileUtils.readFileToString(new File(meta, Constants.MetaData.SUBMISSION), Charset.defaultCharset()), Submission.class);
					User user = gson.fromJson(FileUtils.readFileToString(new File(meta, Constants.MetaData.STUDENT_INFO), Charset.defaultCharset()), User.class);
					
					AutograderSubmission autograderSubmission = new AutograderSubmission(submission, assignmentDir.getParentFile());
					autograderSubmission.setUser(user);
					
					submissionMap.addSubmission(autograderSubmission);
				} catch(IOException e) {
					LOGGER.debug(assignmentDir.getAbsolutePath() + " had a problem.", e);
				}
			}
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
