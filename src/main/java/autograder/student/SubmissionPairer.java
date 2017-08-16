package autograder.student;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class SubmissionPairer {
	
	private static final Logger LOGGER = Logger.getLogger(SubmissionPairer.class.getName());

	public SubmissionData pairSubmissions(AutograderSubmissionMap studentMap){
		SubmissionData submissionData = new SubmissionData();
		for(AutograderSubmission student : studentMap.listStudents()) {
			try {
				if(student.assignProps == null) {
					submissionData.invalidStudents.add(student);
					continue;
				}
				
				AutograderSubmission partner = getPartner(student, studentMap);
				if(partner != null) {
					createPair(partner, student, submissionData);
				} else {
					submissionData.invalidStudents.add(student);
				}
			} catch (Exception e) {
				LOGGER.severe("Exception thrown when trying to pair " + student.studentInfo.name +". " + e.getMessage());
				submissionData.invalidStudents.add(student);
			}
		}
		removeFalseNegatives(submissionData);
		return submissionData;
	}
	
	private void removeFalseNegatives(SubmissionData submissionData) {
		for(SubmissionPair pair : submissionData.pairs) {
			submissionData.invalidStudents.removeAll(pair.getStudents());
		}
	}

	private void createPair(AutograderSubmission partner, AutograderSubmission student, SubmissionData submissionData) {
		submissionData.pairs.add(new SubmissionPair(partner, student));
	}

	private AutograderSubmission getPartner(AutograderSubmission student, AutograderSubmissionMap studentMap) {
		if(student.assignProps.partner_name.equals("default")){ 
			return null;
		}
		AutograderSubmission partner = studentMap.get(student.assignProps.partner_uid.toLowerCase());
		if(partner == null) {
			LOGGER.warning(String.format("Could not match %s to the partner with %s (%s)", student.studentInfo.name, student.assignProps.partner_name, student.assignProps.partner_uid));
			return null;
		}
		return partner;
	}

	public class SubmissionData{
		public Set<SubmissionPair> pairs = new HashSet<>();
		public Set<AutograderSubmission> invalidStudents = new HashSet<>();
	}
}
