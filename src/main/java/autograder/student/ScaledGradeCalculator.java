package autograder.student;

import javax.inject.Inject;

import autograder.configuration.Configuration;

public class ScaledGradeCalculator extends GradeCalculator{

	@Inject
	public ScaledGradeCalculator(Configuration configuration, LatePenalty latePenalty) {
		super(configuration, latePenalty);
	}
	
	@Override
	public double calculateGrade(AutograderSubmission submission) {
		double rawScore = super.calculateGrade(submission);
		return (rawScore / 100) * configuration.scaledDouble;
	}
}
