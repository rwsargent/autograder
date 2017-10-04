package autograder.metrics;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import autograder.student.AutograderSubmissionMap;

/**
 * Gathers statistics on the number of submissions per student. The autograder only 
 * grades the most recent submission, so the attempt of the grader submission is the 
 * total number of submissions the student has made.
 * 
 * @author ryansargent
 */
public class SubmissionAttemptMetrics implements MetricReport {

	private Metrics metric;

	public String getMetrics(AutograderSubmissionMap submissions) {
		return metric.printBasicReport();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculateMetrics(AutograderSubmissionMap submissions) {
		List<Double> attempts = submissions.listStudents().stream()
				.map(sub -> (double)sub.submissionInfo.attempt)
				.collect(Collectors.toList());
		
		double[] submissionAttempts = convertToDoubleArray(attempts);
		metric = Metrics.calculate(submissionAttempts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateMetricReport() {
		return "Submission Attempts\n**********\n" + metric.printBasicReport();
	}
	
	private double[] convertToDoubleArray(List<Double> attempts) {
		double[] samples = new double[attempts.size()];
		for(int idx = 0; idx < samples.length; idx++) {
			samples[idx] = attempts.get(idx);
		}
		Arrays.sort(samples);
		return samples;
	}
}
