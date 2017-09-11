package autograder.phases.three.uploaders;

import static autograder.Constants.Names.ASSIGNMENT;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import autograder.Constants;
import autograder.metrics.MissedTestMetric;
import autograder.phases.three.AssignmentUploader;
import autograder.student.AutograderSubmissionMap;

/**
 * 
 * @author ryansargent
 */
public class AssignmentMetricsUploader implements AssignmentUploader {
	
	private static final String ASSIGNMENT_METRICS_FILENAME = "failedHistogram.txt";
	private final Logger LOGGER = LoggerFactory.getLogger(AssignmentMetricsUploader.class);
	private String assignment;
	private MissedTestMetric missedTests;

	@Inject
	public AssignmentMetricsUploader(@Named(ASSIGNMENT)String assignment, MissedTestMetric missedTests) {
		this.assignment = assignment;
		this.missedTests = missedTests;
		
	}
	
	@Override
	public void upload(AutograderSubmissionMap submissions) {
		LOGGER.info("Calculating metrics for " + assignment);
		String misssedTestSummary = missedTests.getHistogramSummary(submissions);
		File outputDir = Paths.get(Constants.ZIPS, assignment).toFile();
		outputDir.mkdirs();
		
		try {
			Files.write(misssedTestSummary, new File(outputDir, ASSIGNMENT_METRICS_FILENAME), Charset.defaultCharset());
		} catch (IOException e) {
			LOGGER.error("Could not write historgram text file for " + assignment, e);
		}
	}
}
