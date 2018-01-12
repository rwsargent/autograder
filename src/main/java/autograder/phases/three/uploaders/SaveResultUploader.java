package autograder.phases.three.uploaders;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import autograder.configuration.Configuration;
import autograder.phases.three.SubmissionUploader;
import autograder.student.AutograderSubmission;
import autograderutils.results.AutograderResult;

/**
 * Persist the result in an easily retrievable manner
 * @author ryansargent
 */
public class SaveResultUploader implements SubmissionUploader {
	
	private Configuration config;
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Inject
	public SaveResultUploader(Configuration configuration) {
		this.config = configuration;
	}

	@Override
	public void upload(AutograderSubmission submission) {
		File results = createDirectory("results");
		File assignment = createDirectory(results, this.config.assignment);
		File studentDir = createDirectory(assignment, submission.studentInfo.sortableName);
		//create folder for student;
		String savedFileName = createSaveFileName(submission);
		File destination = new File(studentDir, savedFileName);
		AutograderResult result = submission.getResult();
		
		try {
			Files.write(result.getSummary(), destination, Charset.defaultCharset());
		} catch (IOException e) {
			LOGGER.error("Could not write result for " + submission.studentInfo.name, e);
		}
	}

	private String createSaveFileName(AutograderSubmission submission) {
		return submission.studentInfo.id + "_" + submission.submissionInfo.attempt + ".result";
	}

	private File createDirectory(String name) {
		File results = new File(name);
		return createFileAsDirectory(results);
	}

	private File createFileAsDirectory(File results) {
		if(!results.exists()) {
			if(results.mkdirs()) {
				LOGGER.warn("Failed to create " + results.getAbsolutePath() + " as a directory");
			}
		}
		return results;
	}
	private File createDirectory(File parent, String name) {
		File results = new File(parent, name);
		return createFileAsDirectory(results);
	}
}
