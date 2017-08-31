package autograder.tas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograder.configuration.TeacherAssistantRegistry;
import autograder.student.AutograderSubmission;
import autograder.student.SubmissionPair;
import autograder.student.SubmissionPairer.SubmissionData;

/**
 * Use {@link #partition(List, SubmissionData)} to divy up the submissions among the TAs. 
 * @author Ryan
 *
 */
public class PartitionSubmissions {
	
	private Configuration configuration;
	private TeacherAssistantRegistry mTARegistry;
	
	@Inject	
	public PartitionSubmissions(Configuration config, TeacherAssistantRegistry TARegistry) {
		configuration = config;
		mTARegistry = TARegistry;
	}
	
	/**
	 * Randomly divy up the pairs and invalid submissions in {@code submissionData} among the tas. The basis of the algorithm is have a one to many 
	 * relationship between a TA and a submission. The submissions are shuffled, then each TA gets a subsection of the list according to their workload.
	 * Any remaining submissions are round-robin distributed to the TAs. 
	 * @param tas - A list of {@link TAInfo} objects.
	 * @param submissionData - Collection of partnerships and invalid submissions in an instance of {@link SubmissionData}
	 * @return
	 */
	public HashMap<String, Set<SubmissionPair>> partition(SubmissionData submissionData) {
		// Create a mapping of TAs to a submission set, create a shuffleable collection from the submissions
		List<TAInfo> tas = mTARegistry.toList();
		HashMap<String, Set<SubmissionPair>> tasToSubmissions = new HashMap<>();
		tas.forEach(ta -> tasToSubmissions.put(ta.name, new HashSet<>()));
		List<SubmissionPair> submissions = new ArrayList<>(submissionData.pairs);

		// add invalid submissions to the list to be shufffled
		for(AutograderSubmission student : submissionData.invalidStudents) {
			submissions.add(SubmissionPair.createSingleStudentPair(student));
		}
		
		Collections.shuffle(submissions, new Random(configuration.graderClassName.toString().hashCode()));
		
		// Calculate a subList for each ta
		int start = 0;
		for(TAInfo ta : tas) {
			int stop = start + ta.assignmentsToGrade;
			submissions.subList(start, stop).forEach(sub -> tasToSubmissions.get(ta.name).add(sub));
			start = stop;
		}
		int taIdx = 0;
		
		// finish off the rest.
		for(;start<submissions.size();start++, taIdx = ++taIdx % tas.size()) {
			tasToSubmissions.get(tas.get(taIdx).name).add(submissions.get(start));
		}
		return tasToSubmissions;
	}
}
