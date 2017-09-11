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
public class MissedTestMetric {
	private static final Logger LOGGER = LoggerFactory.getLogger(MissedTestMetric.class);
	private static final Pattern failureName = Pattern.compile("TEST FAILED: ([a-zA-Z]+)");
	
	public String getHistogramSummary(AutograderSubmissionMap submissions) {
		Map<String, Integer> testHistogram = buildHistogram(submissions);
		
		List<DataPoint> data = new ArrayList<>();
		int longestTestName = 0;
		for(Entry<String, Integer> datapoint : testHistogram.entrySet()) {
			data.add(new DataPoint(datapoint.getKey(), datapoint.getValue()));
			longestTestName = Math.max(longestTestName, datapoint.getKey().length());
		}
		data.sort(null);
		StringBuilder textOutput = new StringBuilder();
		textOutput.append("Total submission count: " + submissions.size());
		for(DataPoint dp : data) {
			double percentage = 100 * (dp.frequency / (double) submissions.size());
			textOutput.append(String.format("%-" + longestTestName + "s: %02.2f%% (%d)\n", dp.testName, percentage, dp.frequency));
		}
		return textOutput.toString();
	}
	
	protected Map<String, Integer> buildHistogram(AutograderSubmissionMap submissions) {
		LOGGER.info("Analyzing all missed test cases.");
		HashMap<String, Integer> testHistogram = new HashMap<>();
		
		for(AutograderSubmission submission : submissions.listStudents()) {
			for(String line : submission.getResult().buildTestResults().split("\n")) {
				Matcher matcher = failureName.matcher(line);
				if(matcher.find()) {
					String testName = matcher.group(1);
					int failedTestFrequency = testHistogram.get(testName) == null ? 0 : testHistogram.get(testName);
					testHistogram.put(testName, failedTestFrequency++);
				}
			}
		}
		return testHistogram;
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
