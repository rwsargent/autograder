package autograder.metrics;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import autograder.student.AutograderSubmissionMap;

/**
 * Gathers statistics on the number of submissions per student. The autograder only 
 * grades the most recent submission, so the attempt of the grader submission is the 
 * total number of submissions the student has made.
 * 
 * The metrics returned include max, min, mean, mode, median, and standard deviation
 * @author ryansargent
 */
public class SubmissionAttemptMetrics {

	public String getMetrics(AutograderSubmissionMap submissions) {
		List<Double> attempts = submissions.listStudents().stream()
				.map(sub -> (double)sub.submissionInfo.attempt)
				.collect(Collectors.toList());
		
		double[] submissionAttempts = convertToDoubleArray(attempts);
		double mean = StatUtils.mean(submissionAttempts);
		double[] mode = StatUtils.mode(submissionAttempts);
		double max = StatUtils.max(submissionAttempts);
		double min = StatUtils.min(submissionAttempts);
		double median = submissionAttempts[submissionAttempts.length / 2];
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%-7s %02.2f\n", "max", max));
		sb.append(String.format("%-7s %02.2f", "min", min));
		sb.append(String.format("%-7s %02.2f", "mean", mean));
		sb.append(String.format("%-7s %2s", "mode", Arrays.toString(mode)));
		sb.append(String.format("%-7s %02.2f", "median", median));
		
		StandardDeviation stdv = new StandardDeviation();
		stdv.evaluate(submissionAttempts, mean);
		
		return sb.toString();
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
