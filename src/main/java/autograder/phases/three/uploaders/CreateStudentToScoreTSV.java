package autograder.phases.three.uploaders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.phases.three.AssignmentUploader;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.student.GradeCalculator;

public class CreateStudentToScoreTSV implements AssignmentUploader{

	private GradeCalculator calculator;
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateStudentToScoreTSV.class);
	
	@Inject
	public CreateStudentToScoreTSV(GradeCalculator calculator) {
		this.calculator = calculator;
	}
	
	@Override
	public void upload(AutograderSubmissionMap submissions) {
		LOGGER.info("Creating student to score csv");
		try(FileWriter fw = new FileWriter(new File("studenToScore.csv"))) {
			fw.write("student,score\n");
			List<AutograderSubmission> listStudents = submissions.listStudents();
			listStudents.sort((lhs, rhs) -> lhs.studentInfo.sis_user_id.compareTo(rhs.studentInfo.sis_user_id));
			for(AutograderSubmission submission : listStudents) {
				double grade = calculator.calculateGrade(submission);
				fw.write(String.format("%s\t%.1f\n", submission.studentInfo.sis_user_id, grade));
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
