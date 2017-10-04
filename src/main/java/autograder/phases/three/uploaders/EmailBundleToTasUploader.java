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
import autograder.filehandling.SubmissionPartitioner;
import autograder.mailer.Mailer;
import autograder.phases.three.AssignmentUploader;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

/**
 * 
 * @author ryans
 */
public class EmailBundleToTasUploader implements AssignmentUploader{
	
	private Bundler bundler;
	private Mailer emailer;
	private Configuration config;
	
	
	private final Logger LOGGER = LoggerFactory.getLogger(EmailBundleToTasUploader.class);
	private SubmissionPartitioner partitioner;

	@Inject
	public EmailBundleToTasUploader(Configuration configuration, Bundler bundler, Mailer emailer, SubmissionPartitioner partitioner) {
		this.config = configuration;
		this.bundler = bundler;
		this.emailer = emailer;
		this.partitioner = partitioner;
	}

	@Override
	public void upload(AutograderSubmissionMap submissions) {
		LOGGER.info("Starting to bundle and email for this assignment.");
		// we need to read file somehow
		Map<String, List<AutograderSubmission>> tasToStudents = partitioner.partition(submissions);
		Map<String, File> bundledSubmissions = bundler.bundle(tasToStudents);
		for(Entry<String, File> taToBundle : bundledSubmissions.entrySet()) {
			LOGGER.info("Emailing bundled filed to: " + taToBundle.getKey());
			emailer.sendMailWithAttachment(taToBundle.getKey(), 
					emailSubject(), 
					emailBody(), 
					taToBundle.getValue());
		}
	}
	
	private String emailSubject() {
		return StringUtils.capitalize(config.assignment) + " Grading Distrubution";
	}
	
	private String emailBody() {
		return "TAs Rule!\nHappy grading!";
	}
}
