package autograder.metrics;

import autograder.student.AutograderSubmissionMap;

public interface MetricReport {

	/**
	 * Makes whatever calculations are necessary for this metric report.
	 * @param submissions
	 */
	public void calculateMetrics(AutograderSubmissionMap submissions);
	
	/**
	 * Creates a human friendly report from caculated metrics. 
	 * @return
	 */
	public String generateMetricReport();
}
