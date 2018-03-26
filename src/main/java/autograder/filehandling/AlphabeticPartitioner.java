package autograder.filehandling;

import java.util.ArrayList;
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

public class AlphabeticPartitioner implements SubmissionPartitioner {

	private Configuration config;
	private Map<String, TAInfo> tas;

	@Inject
	public AlphabeticPartitioner(@Named(Constants.Names.TAS) Map<String, TAInfo> tas, Configuration configuration) {
		this.config = configuration;
		this.tas = tas;
	}
	
	@Override
	public Map<String, List<AutograderSubmission>> partition(AutograderSubmissionMap submissions) {
		double totalHours = calculateTotalHours();
		Random rand = new Random(Math.abs(config.canvasAssignmentId.hashCode()));
		List<AutograderSubmission> reorderedSubmissions = new ArrayList<>(submissions.listStudents());
		int rotatedStartIndex = rand.nextInt(reorderedSubmissions.size());
		reorderedSubmissions.sort((lhs, rhs) ->  {
			if(lhs.studentInfo == null) {
				return rhs.submissionInfo == null ? 0 : 1;
			}
			if(rhs.studentInfo == null ) {
				return lhs.submissionInfo == null ? 0 : -1 ;
			}
			return lhs.studentInfo.sortableName.compareTo(rhs.studentInfo.sortableName);
		});
		
		ArrayList<AutograderSubmission> rotatedList = new ArrayList<>(reorderedSubmissions.subList(rotatedStartIndex, reorderedSubmissions.size()));
		rotatedList.addAll(reorderedSubmissions.subList(0, rotatedStartIndex));
		reorderedSubmissions = rotatedList;
		
		Map<String, List<AutograderSubmission>> partition = new HashMap<>();
		List<List<AutograderSubmission>> overflows = new ArrayList<>(); // to catch any missed submissions
		int cursor = 0;
		for(TAInfo ta : tas.values()) {
			double portion = ta.hours / totalHours;
			int count = (int)Math.floor(reorderedSubmissions.size() * portion);
			
			List<AutograderSubmission> tasWorkLoad = new ArrayList<>(reorderedSubmissions.subList(cursor, (cursor + count)));
			cursor += count;
			partition.put(ta.email, tasWorkLoad);
			overflows.add(tasWorkLoad);
		}

		// Add any missing submission from the scrambled list that were lost due to rounding errors. 
		for(; cursor < reorderedSubmissions.size(); cursor++) {
			AutograderSubmission submission = reorderedSubmissions.get(cursor);
			overflows.get(cursor % overflows.size()).add(submission);
		}
		return partition;
	}
	
	private double calculateTotalHours() {
		return tas.values().stream().mapToDouble(info -> info.hours).sum();
	}

}
