package autograder.phases.three.uploaders;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.configuration.Configuration;
import autograder.filehandling.Bundler;
import autograder.mailer.Mailer;
import autograder.phases.three.AssignmentUploader;
import autograder.student.AutograderSubmissionMap;

public class EmailBundleToTasUploader implements AssignmentUploader{
	
	private Bundler bundler;
	private Mailer emailer;
	private Configuration config;
	
	private final Logger LOGGER = LoggerFactory.getLogger(EmailBundleToTasUploader.class);

	@Inject
	public EmailBundleToTasUploader(Configuration configuration, Bundler bundler, Mailer emailer) {
		this.config = configuration;
		this.bundler = bundler;
		this.emailer = emailer;
	}

	@Override
	public void upload(AutograderSubmissionMap submissions) {
		LOGGER.info("Starting to bundle and email for this assignment.");
		// we need to read file somehow
		Map<String, List<String>> tasToStudents = tasToStudents();
		Map<String, File> results = bundler.bundle(submissions, tasToStudents);
		for(Entry<String, File> result : results.entrySet()) {
			emailer.sendMailWithAttachment(getRecipient(result.getKey()), 
					emailSubject(), 
					emailBody(), 
					result.getValue());
		}
		
	}
	
	// need TA to Student map
	private String getRecipient(String key) {
		return null;
	}

	private String emailSubject() {
		return StringUtils.capitalize(config.assignment) + " Grading Distrubution";
	}

	private Map<String, List<String>> tasToStudents() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String emailBody() {
		return "TAs Rule!\nHappy grading!";
	}

}
