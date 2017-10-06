package autograder.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

/**
 * Used to generate a histogram of failed tests. 
 * 
 * This class does NOT rely on {@link org.junit.runner.Result}, but instead on 
 * the generated output of the AutograderResult. This class counts how many 
 * times a test name appears in the output of a result file for all the 
 * submissions of an assignment. Because this relies on the SubmissionMap 
 * having all the of the required submissions, it's best to use this metric for
 * a complete run. 
 * 
 * The result file needs to have follow this regular expression:
 * <pre>
 * TEST FAILED: [a-zA-Z]+
 * </pre>
 * @author ryansargent
 */
public class MissedTestMetric implements MetricReport {
	private static final Logger LOGGER = LoggerFactory.getLogger(MissedTestMetric.class);
	private static final Pattern failureName = Pattern.compile("TEST FAILED: ([a-zA-Z]+)");
	private HashMap<String, Integer> testHistogram;
	private int submissionCount = 0;
	
	/**
	 * Text summary of all miss tests. Give their name, failed count, and percentage of students who
	 * missed.
	 * 
	 * @param submissions
	 * @return
	 */
	public String getHistogramSummary(AutograderSubmissionMap submissions) { 
		return getHistogramSummary(-1);
	}
	
	/**
	 * @param limit - use the top LIMIT number of data points. If limit is <= 0, use all.
	 * @return
	 */
	public String getHistogramSummary(int limit) {
		if(testHistogram == null) {
			throw new IllegalStateException("Please call 'calculateMetrics' first");
		}
		
		List<DataPoint> data = new ArrayList<>();
		int longestTestName = 0;
		for(Entry<String, Integer> datapoint : testHistogram.entrySet()) {
			data.add(new DataPoint(datapoint.getKey(), datapoint.getValue()));
			longestTestName = Math.max(longestTestName, datapoint.getKey().length());
		}
		data.sort(null);
		if(limit > 0) {
			data = data.subList(0, limit);
		}
		StringBuilder textOutput = new StringBuilder();
		textOutput.append("Total submission count: " + submissionCount + "\n");
		for(DataPoint dp : data) {
			double percentage = 100 * (dp.frequency / (double) submissionCount);
			textOutput.append(String.format("%-" + longestTestName + "s %02.2f%% (%d)\n", dp.testName, percentage, dp.frequency));
		}
		return textOutput.toString();
	}
	
	protected Map<String, Integer> buildHistogram(AutograderSubmissionMap submissions) {
		LOGGER.info("Analyzing all missed test cases.");
		testHistogram = new HashMap<>();
		submissionCount = submissions.size();
		
		for(AutograderSubmission submission : submissions.listStudents()) {
			for(String line : submission.getResult().getTestResults().split("\n")) {
				Matcher matcher = failureName.matcher(line);
				if(matcher.find()) {
					String testName = matcher.group(1);
					int failedTestFrequency = testHistogram.containsKey(testName) ? testHistogram.get(testName) : 0 ;
					testHistogram.put(testName, ++failedTestFrequency);
				}
			}
		}
		return testHistogram;
	}
	
	@Override
	public String generateMetricReport() {
		return "Missed Tests Information\n**********\n" + getHistogramSummary(-1);
	}

	@Override
	public void calculateMetrics(AutograderSubmissionMap submissions) {
		buildHistogram(submissions);
	}
	
	protected class DataPoint implements Comparable<DataPoint>{
		String testName;
		int frequency;
		
		public DataPoint(String name, int frequency) {
			this.testName = name;
			this.frequency = frequency;
		}
		
		@Override
		public int compareTo(DataPoint o) {
			return  o.frequency - this.frequency;
		}
	}

	
}
