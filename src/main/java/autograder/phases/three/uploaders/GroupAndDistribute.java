package autograder.phases.three.uploaders;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.filehandling.Bundler;
import autograder.mailer.Mailer;
import autograder.phases.three.AssignmentUploader;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.student.SubmissionPair;
import autograder.student.SubmissionPairer;
import autograder.student.SubmissionPairer.SubmissionData;
import autograder.tas.TAInfo;

public class GroupAndDistribute implements AssignmentUploader {

	private SubmissionPairer pairer;
	private Mailer emailer;
	private Map<String, TAInfo> tas;
	private Configuration config;
	private Bundler bundler;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GroupAndDistribute.class);

	@Inject
	public GroupAndDistribute(SubmissionPairer pairer, Mailer mailer, @Named(Constants.Names.TAS) Map<String, TAInfo> tas, Configuration config,
			Bundler bundler) {
		this.pairer = pairer;
		this.emailer = mailer;
		this.tas = tas;
		this.config = config;
		this.bundler = bundler;
	} 
	
	@Override
	public void upload(AutograderSubmissionMap submissions) {
		// pair
		LOGGER.info("Pairing submissions");
		SubmissionData submissionData = pairer.pairSubmissions(submissions);

		LOGGER.info("Partitiong pairs");
		Map<String, List<SubmissionPair>> partition = partitionGroups(submissionData);
		
		LOGGER.info("Bundle for tas");
		Map<String, File> outbound = bundler.bundlePair(partition);
		//email
		for(Entry<String, File> taToBundle : outbound.entrySet()) {
			LOGGER.info("Emailing bundled filed to: " + taToBundle.getKey());
			emailer.sendMailWithAttachment(taToBundle.getKey(), 
					emailSubject(), 
					emailBody(), 
					taToBundle.getValue());
		}
	}

	private Map<String, List<SubmissionPair>> partitionGroups(SubmissionData submissionData) {
		submissionData.pairs.sort((lhs, rhs) -> {
			if(lhs.submitter != null && rhs.submitter == null) {
				return -1;
			} else if (lhs.submitter == null && rhs.submitter != null) {
				return 1;
			} else if(lhs.submitter == null && rhs.submitter == null) {
				return 0;
			} else {
				return lhs.submitter.compareTo(rhs.submitter);
			}
		});
		
		Map<String, List<SubmissionPair>> partition = new HashMap<>();
		List<List<SubmissionPair>> overflows = new ArrayList<>(); // to catch any missed submissions
		int cursor = 0;
		for(TAInfo ta : tas.values()) {
			double portion = ta.hours / calculateTotalHours();
			int count = (int)Math.floor(submissionData.pairs.size() * portion);
			
			List<SubmissionPair> tasWorkLoad = new ArrayList<>(submissionData.pairs.subList(cursor, (cursor + count)));
			cursor += count;
			partition.put(ta.email, tasWorkLoad);
			overflows.add(tasWorkLoad);
		}
		
		for(; cursor < submissionData.pairs.size(); cursor++) {
			SubmissionPair submission = submissionData.pairs.get(cursor);
			overflows.get(cursor % overflows.size()).add(submission);
		}

		int keyIdx = 0;
		List<String> keys = new ArrayList<>(partition.keySet());
		for(AutograderSubmission invalid : submissionData.invalidStudents) {
			partition.get(keys.get(keyIdx++ % keys.size())).add(SubmissionPair.createSingleStudentPair(invalid));
		}
		return partition;
	}
	
	private double calculateTotalHours() {
		return tas.values().stream().mapToDouble(info -> info.hours).sum();
	}
	
	private String emailSubject() {
		return StringUtils.capitalize(config.assignment) + " Grading Distrubution";
	}
	
	private String emailBody() {
		return "TAs Rule!\nHappy grading!";
	}
}
