package autograder.tas;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import autograder.student.SubmissionPair;

public class PartitionSubmissions {
	public static HashMap<String, Set<SubmissionPair>> partition(List<TAInfo> tas, List<SubmissionPair> submissions) {
		HashMap<String, Set<SubmissionPair>> tasToSubmissions = new HashMap<>();
		tas.forEach(ta -> tasToSubmissions.put(ta.name, new HashSet<>()));
		Collections.shuffle(submissions);
		int start = 0;
		for(TAInfo ta : tas) {
			submissions.subList(start, start + ta.assignmentsToGrade).forEach(sub -> tasToSubmissions.get(ta.name).add(sub));
			start += ta.assignmentsToGrade;
		}
		int taIdx = 0;
		
		for(;start<submissions.size();start++, taIdx = ++taIdx % tas.size()) {
			tasToSubmissions.get(tas.get(taIdx).name).add(submissions.get(start));
		}
		return tasToSubmissions;
	}
}
