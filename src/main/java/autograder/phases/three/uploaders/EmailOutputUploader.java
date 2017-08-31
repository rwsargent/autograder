package autograder.phases.three.uploaders;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.configuration.Configuration;
import autograder.mailer.Mailer;
import autograder.phases.three.SubmissionUploader;
import autograder.student.AutograderSubmission;

public class EmailOutputUploader implements SubmissionUploader {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailOutputUploader.class);
	private Configuration configuration;
	private Mailer mailer;
	
	@Inject
	public EmailOutputUploader(Configuration configuration, Mailer mailer) {
		this.configuration = configuration;
		this.mailer = mailer;
	}
	
	@Override
	public void upload(AutograderSubmission submission) {
		if(submission.studentInfo.email == null) {
			LOGGER.debug("Couldn't get email off of student information for " + submission);
			return;
		}
		
		String buildSummary = submission.getResult().buildSummary();
		mailer.sendMail(submission.studentInfo.email, "Autograder result for " + configuration.assignment, 
				buildSummary);
	}

}
