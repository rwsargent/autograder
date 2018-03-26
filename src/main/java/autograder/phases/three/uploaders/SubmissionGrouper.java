package autograder.phases.three.uploaders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.configuration.AssignmentProperties;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.student.group.AutograderSubmissionGroup;

public class SubmissionGrouper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionGrouper.class);

	public List<AutograderSubmissionGroup> groupSubmissions(AutograderSubmissionMap submissions) {
		List<AutograderSubmissionGroup> groups = new ArrayList<>();

		for(AutograderSubmission submission : submissions.listStudents()) {
			AssignmentProperties assignmentProperties = submission.createAssignmentProperties();
		}
		return null;
	}
	
	public class SubmissionData{
		public List<AutograderSubmissionGroup> pairs = new ArrayList<>();
		public Set<AutograderSubmission> invalidStudents = new HashSet<>();
	}
}
