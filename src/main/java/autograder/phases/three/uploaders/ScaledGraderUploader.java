package autograder.phases.three.uploaders;

import java.io.IOException;
import java.nio.file.Files;

import javax.inject.Inject;

import autograder.phases.three.SubmissionUploader;
import autograder.student.AutograderSubmission;
import autograder.student.GradeCalculator;

public class ScaledGraderUploader implements SubmissionUploader {

	@Inject GradeCalculator graderCalculator;
	
	@Override
	public void upload(AutograderSubmission submission) {
		String filename = "autograderScore_" + graderCalculator.calculateGrade(submission) + ".txt";
		try {
			Files.write(submission.getDirectory().toPath().resolve(filename), new byte[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
}
