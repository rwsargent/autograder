package autograder.filehandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.tas.TAInfo;

public class RandomizedPartitioner implements SubmissionPartitioner {
	
	private Configuration config;
	private Map<String, TAInfo> tas;

	@Inject
	public RandomizedPartitioner(@Named(Constants.Names.TAS) Map<String, TAInfo> tas, Configuration configuration) {
		this.config = configuration;
		this.tas = tas;
	}

	@Override
	public Map<String, List<AutograderSubmission>> partition(AutograderSubmissionMap submissions) {
		// Calculate total hours, give number of students proportional to TAs hours.
		double totalHours = calculateTotalHours();
		Random rand = new Random(Math.abs(config.canvasAssignmentId.hashCode()));
		
		List<AutograderSubmission> scrambledSubmissions = new ArrayList<>(submissions.listStudents());
		scrambledSubmissions.sort((lhs, rhs) -> lhs.getDirectory().compareTo(rhs.getDirectory()));
		Collections.shuffle(scrambledSubmissions, rand);
		
		Map<String, List<AutograderSubmission>> partition = new HashMap<>();
		List<List<AutograderSubmission>> overflows = new ArrayList<>(); // to catch any missed submissions
		int cursor = 0;
		for(TAInfo ta : tas.values()) {
			double portion = ta.hours / totalHours;
			int count = (int)Math.floor(scrambledSubmissions.size() * portion);
			
			List<AutograderSubmission> tasWorkLoad = new ArrayList<>(scrambledSubmissions.subList(cursor, (cursor + count)));
			cursor += count;
			partition.put(ta.email, tasWorkLoad);
			overflows.add(tasWorkLoad);
		}

		// Add any missing submission from the scrambled list that were lost due to rounding errors. 
		for(; cursor < scrambledSubmissions.size(); cursor++) {
			AutograderSubmission submission = scrambledSubmissions.get(cursor);
			overflows.get(cursor % overflows.size()).add(submission);
		}
		return partition;
	}

	private double calculateTotalHours() {
		return tas.values().stream().mapToDouble(info -> info.hours).sum();
	}
}
