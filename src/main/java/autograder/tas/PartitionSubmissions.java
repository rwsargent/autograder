package autograder.tas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import autograder.configuration.Configuration;
import autograder.student.Student;
import autograder.student.SubmissionPair;
import autograder.student.SubmissionPairer.SubmissionData;

public class PartitionSubmissions {
	
	public static HashMap<String, Set<SubmissionPair>> partition(List<TAInfo> tas, SubmissionData submissionData) {
		HashMap<String, Set<SubmissionPair>> tasToSubmissions = new HashMap<>();
		tas.forEach(ta -> tasToSubmissions.put(ta.name, new HashSet<>()));
		
		List<SubmissionPair> submissions = submissionData.pairs.stream().collect(Collectors.toList());
		for(Student student : submissionData.invalidStudents) {
			submissions.add(SubmissionPair.createSingleStudentPair(student));
		}
		
		submissions.sort( (lhs, rhs) -> lhs.submitter.studentInfo.sortableName.compareTo(rhs.submitter.studentInfo.sortableName));
		int offset = Math.abs(Configuration.getConfiguration().assignment.hashCode()) % submissions.size();
		submissions = shiftLeft(submissions, offset);
		
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
	
	private static <T> List<T> shiftLeft(List<T> list, int offset) {
		ArrayList<T> shiftedList = new ArrayList<>();
		shiftedList.addAll(list.subList(offset, list.size()));
		shiftedList.addAll(list.subList(0, offset));
		return list;
	}
}
