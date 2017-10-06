package autograder.phases.three.uploaders;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import autograder.configuration.Configuration;
import autograder.phases.three.SubmissionUploader;
import autograder.student.AutograderSubmission;

public class CopyToDestinationUploader implements SubmissionUploader {

	private Configuration configuration;
	private static final Logger LOGGER = LoggerFactory.getLogger(CopyToDestinationUploader.class); 

	@Inject
	public CopyToDestinationUploader(Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void upload(AutograderSubmission submission) {
		String outputFileName = buildOutputFileName(submission);
		String outputContents = getOutputContents(submission);
		
		try {
			Files.write(outputContents, Paths.get(getDestination(), outputFileName).toFile(), Charset.defaultCharset());
		} catch (IOException e) {
			LOGGER.error("Trying to write  output file for " +  submission, e);
		}
	}

	private String getOutputContents(AutograderSubmission submission) {
		return submission.getResult().getSummary();
	}

	private String getDestination() {
		return configuration.outputDestination;
	}
	
	private String buildOutputFileName(AutograderSubmission submission) {
		return configuration.assignment + "_" +
				submission.studentInfo.sis_user_id + "_" + 
				submission.submissionInfo.submitted_at + ".result";
	}
}
