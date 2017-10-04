package autograder.phases.three.uploaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.filehandling.Zipper;
import autograder.mailer.Mailer;
import autograder.metrics.MetricReport;
import autograder.phases.three.AssignmentUploader;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

public class FullAssignmentUpload implements AssignmentUploader {
	
	private final Logger LOGGER = LoggerFactory.getLogger(FullAssignmentUpload.class);
	
	private Configuration configuration;
	private Mailer emailer;
	private Set<MetricReport> metricReports;

	@Inject
	public FullAssignmentUpload(Configuration configuration, Set<MetricReport> metricReports, Mailer emailer) {
		this.configuration = configuration;
		this.emailer = emailer;
		this.metricReports = metricReports;
	}
	
	@Override
	public void upload(AutograderSubmissionMap submissions) {
		File assignmentZip = Paths.get(Constants.ZIPS, configuration.assignment + "_full.zip").toFile();
		try(Zipper zipper = new Zipper()) {
			// add Metrics
			zipper.init(assignmentZip);
			zipper.addEntry("assignment_stats.txt", createAssignmentStatistics(submissions));
			
			for(AutograderSubmission submission : submissions.listStudents()) {
				zipper.zipDirectory(submission.getDirectory(), file ->  {
					return !FilenameUtils.getExtension(file.getName()).equals("class") && !file.getName().equals("meta"); 
				});
			}
		} catch (IOException e) {
			LOGGER.error("Could not zip up full submission", e);
		}
		
		emailer.sendMailWithAttachment(configuration.senderEmail,
				configuration.assignment + " full zip", 
				"Full zip file of attached code",
				assignmentZip);
		
	}

	private String createAssignmentStatistics(AutograderSubmissionMap submissions) {
		StringBuilder report = new StringBuilder();
		for(MetricReport metricReport : metricReports) {
			metricReport.calculateMetrics(submissions);
			report.append(metricReport.generateMetricReport() + "\n");
		}
		if(report.length() != 0 ) {
			report.setLength(report.length() - 1); // chop off final new line
		}
		return report.toString();
	}
	
}
