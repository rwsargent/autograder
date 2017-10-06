package autograder.phases.three.uploaders;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import autograder.phases.three.SubmissionUploader;
import autograder.student.AutograderSubmission;
import autograderutils.results.AutograderResult;

public class WriteResultToDisk implements SubmissionUploader {

	private final Logger LOGGER = LoggerFactory.getLogger(WriteResultToDisk.class);
	
	@Override
	public void upload(AutograderSubmission submission) {
		try {
			AutograderResult result = submission.getResult();
			if(result != null) {
				Files.write(result.getSummary().getBytes(), new File(submission.getDirectory(), buildFileNameForSubmission(submission)));
			}
		} catch (IOException e) {
			LOGGER.error("IOException for " + submission + " while writing result file to disk.", e);
		}
	}
	
	public String buildFileNameForSubmission(AutograderSubmission submission) {
		return submission.studentInfo.name + "_" + submission.submissionInfo.attempt + ".result";
	}

}
