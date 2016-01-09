package autograder.student;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class SubmissionPairer {
	
	private static final Logger LOGGER = Logger.getLogger(SubmissionPairer.class.getName());

	public SubmissionData pairSubmissions(List<Student> students){
		SubmissionData submissionData = new SubmissionData();
		StudentSubmissionRegistry registry = StudentSubmissionRegistry.getInstance();
		Map<String, StudentInfo> classList = ClassListRegistry.getInstance().getMap();
		
		for(Student student : students) {
			if(student.assignProps == null) {
				submissionData.invalidStudents.add(student);
				continue;
			}
			
			Student partner = getPartner(student, registry, classList);
			if(partner != null) {
				createPair(partner, student, submissionData);
			} else {
				submissionData.invalidStudents.add(student);
			}
		}
		removeFalseNegatives(submissionData);
		return submissionData;
	}
	
	private void removeFalseNegatives(SubmissionData submissionData) {
		for(SubmissionPair pair : submissionData.pairs) {
			submissionData.invalidStudents.remove(pair.submitter);
			submissionData.invalidStudents.remove(pair.partner);
		}
	}

	private void createPair(Student partner, Student student, SubmissionData submissionData) {
		if(student.assignProps.submitted) {
			submissionData.pairs.add(new SubmissionPair(student, partner));
		} else {
			submissionData.pairs.add(new SubmissionPair(partner, student));
		}
	}

	private Student getPartner(Student student, StudentSubmissionRegistry registry, Map<String, StudentInfo> classList) {
		StudentInfo partnerInfo = classList.get(student.assignProps.partner_uid);
		if(partnerInfo == null) {
			LOGGER.warning(String.format("Could not match %s to the partner with %s (%s)", student.name, student.assignProps.partner_name, student.assignProps.partner_uid));
			return null;
		}
		Student partner = registry.getStudentById(partnerInfo.canvasid);
		if(partner == null) {
			LOGGER.warning(String.format("Could not match %s to the %s, with canvas id: %s", student.name, partnerInfo.name, partnerInfo.canvasid));
		}
		return partner;
	}

	public class SubmissionData{
		public Set<SubmissionPair> pairs = new HashSet<>();
		public Set<Student> invalidStudents = new HashSet<>();
	}
}
