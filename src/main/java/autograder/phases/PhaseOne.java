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
 * 
 * Phase One's main purpose is to download the submissions, arrange them of the file 
 * system and create an AutograderSubmission map
 * 
 * Plugin points include: <br/>
 * {@link SubmissionDownloader} 
 * 		- Start of parsing submissions. This object can have data to link to attachments
 * {@link SubmissionBuilder} 
 * 		- Using POJO from submissionDownloader, start building file system and internal object
 * {@link SeenSubmissions}
 * 	    - Used to cull unwanted submissions that SubmissionDownloader might produced
 * {@link StudentDownloader}
 *      - Used to flesh out submission details, if they didn't come down with SubmissionDownloader
 *  {@link SubmissionParser}
 *  	- Filters out files to write to disk for AutograderSubmission
 * @author ryans 2017
 */
public class PhaseOne {
	protected SubmissionDownloader submissionDownloader;
	protected SubmissionBuilder submissionBuilder;
	protected SeenSubmissions seenSubmissions;
	private StudentDownloader studentDownloader;
	private SubmissionParser parser;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PhaseOne.class);
	
	/**
	 * For a description of parameters, see plugin points in the class definition
	 * @param submissionDownloader
	 * @param subBuilder
	 * @param seenSubmissions
	 * @param studentDownloader
	 * @param parser
	 */
	@Inject
	public PhaseOne(SubmissionDownloader submissionDownloader, SubmissionBuilder subBuilder, SeenSubmissions seenSubmissions, StudentDownloader studentDownloader, SubmissionParser parser) {
		this.submissionDownloader = submissionDownloader;
		this.submissionBuilder = subBuilder;
		this.seenSubmissions = seenSubmissions;
		this.studentDownloader = studentDownloader;
		this.parser = parser;
	}
	
	/**
	 * 
	 * @param assignment 
	 * 	- The name of the assignment the autograder is currently running
	 * @param rerun
	 * 	- Whether or not to read from disk from a previous run, or to pull from SubmissionDownloader
	 * @return
	 * 	- and {@link AutograderSubmissionMap} that reprents this autograder run's submissions.
	 */
	public AutograderSubmissionMap setupSubmissions(String assignment, boolean rerun) {
		AutograderSubmissionMap submissionMap = new AutograderSubmissionMap();
		if(rerun) {
			LOGGER.info("Reruning autograder's previous group of submissions");
			File assignmentDirectory = Paths.get("submissions", assignment).toFile();
			if(!assignmentDirectory.exists()) {
				throw new IllegalStateException("Can't find " + assignmentDirectory.getAbsolutePath() + ". Are you sure you should be re-running?");
			}
			// read submissions and meta data from disk.
			Gson gson = new Gson();
			File[] listFiles = assignmentDirectory.listFiles();
			LOGGER.debug("Found " + listFiles.length + " files to rerun.");
			for (File assignmentDir : listFiles) {
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
			LOGGER.debug("Downloading most recent submissions");
			List<Submission> fullSubmissions = submissionDownloader.downloadSubmissions();
			List<Submission> subsToRemove = new ArrayList<>();
			
			LOGGER.debug("Removing already seen submissions.");
			for(Submission submission : fullSubmissions) {
				if(seenSubmissions.alreadySeenSubmission(submission)) {
					subsToRemove.add(submission);
				}
			}
			fullSubmissions.removeAll(subsToRemove);
			
			if(fullSubmissions.size() == 0) {
				LOGGER.info("No new submissions this run");
			}
			
			// create AutograderSubmission
			fullSubmissions.stream()
				.peek(submission -> LOGGER.info("Creating " + submission))
				.map(parser::parseAndCreateSubmission)
				.peek(studentDownloader::fillUser)
				.peek(submission -> submission.writeMetaData())
				.forEach(submissionMap::addSubmission);
		}
		return submissionMap;
	}
}
