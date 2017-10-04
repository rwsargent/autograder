package autograder.phases.three.uploaders;

import static autograder.Constants.Names.ASSIGNMENT;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import autograder.Constants;
import autograder.metrics.MetricReport;
import autograder.phases.three.AssignmentUploader;
import autograder.student.AutograderSubmissionMap;

/**
 * Write the basic metrics for an assignment to file.
 * File is "metrics.txt" under the output directory for this assignment.
 * @author ryansargent
 */
public class AssignmentMetricsUploader implements AssignmentUploader {
	
	private static final String ASSIGNMENT_METRICS_FILENAME = "metrics.txt";
	private final Logger LOGGER = LoggerFactory.getLogger(AssignmentMetricsUploader.class);
	private String assignment;
	private Set<MetricReport> metricReports;

	@Inject
	public AssignmentMetricsUploader(@Named(ASSIGNMENT)String assignment, Set<MetricReport> metricReports) {
		this.assignment = assignment;
		this.metricReports = metricReports;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void upload(AutograderSubmissionMap submissions) {
		LOGGER.info("Calculating metrics for " + assignment);
		StringBuilder report = new StringBuilder();
		for(MetricReport metricReport : metricReports) {
			metricReport.calculateMetrics(submissions);
			report.append(metricReport.generateMetricReport() + "\n");
		}
		if(report.length() != 0 ) {
			report.setLength(report.length() - 1); // chop off final new line
		}
		File outputDir = Paths.get(Constants.ZIPS, assignment).toFile();
		outputDir.mkdirs();
		
		try {
			Files.write(report.toString(), new File(outputDir, ASSIGNMENT_METRICS_FILENAME), Charset.defaultCharset());
		} catch (IOException e) {
			LOGGER.error("Could not write historgram text file for " + assignment, e);
		}
	}
}
