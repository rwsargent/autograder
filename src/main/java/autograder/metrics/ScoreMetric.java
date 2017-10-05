package autograder.metrics;

import javax.inject.Inject;

import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.student.GradeCalculator;

/**
 * Gathers the standards metrics on the scores for the currently graded submissions.
 * @author ryans
 */
public class ScoreMetric implements MetricReport{
	
	private GradeCalculator calculator;
	
	private double[] data;
	private Metrics metrics;

	@Inject
	public ScoreMetric(GradeCalculator calculator) {
		this.calculator = calculator;
	}

	/**
	 * {@inheritDoc}
	 */
	public void calculateMetrics(AutograderSubmissionMap submissions) {
		data = new double[submissions.size()];
		int idx = 0;
		
		for(AutograderSubmission submission : submissions.listStudents()) {
			data[idx++] = calculator.calculateGrade(submission);
		}
		metrics = Metrics.calculate(data);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateMetricReport() {
		return "Assignment Scores\n**********\n" + metrics.printBasicReport();
	}
}
