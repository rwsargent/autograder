package autograder.phases.three.uploaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.filehandling.Zipper;
import autograder.student.AutograderSubmissionMap;

public class WrapUpGraderRun {

	private Zipper zipper;
	private Configuration configuration;
	
	private final Logger LOGGER = LoggerFactory.getLogger(WrapUpGraderRun.class);

	@Inject
	public WrapUpGraderRun(Configuration configuration, Zipper zipper) {
		this.zipper = zipper;
		this.configuration = configuration;
	}
	
	public void cleanUp(AutograderSubmissionMap submissions) {
		// no submissions this round, nothing to do.
		if(submissions.size() == 0) {
			return;
		}
		
		// zip up submissions and place them somewhere.
		File assignmentRootDir = Paths.get(Constants.SUBMISSIONS, configuration.assignment).toFile();
		File parentDirs = Paths.get(Constants.PREVIOUS_RUNS, configuration.assignment).toFile();
		try {
			parentDirs.mkdirs();
			zipper.init(new File(parentDirs, generateFilename()));
			zipper.zipDirectory(assignmentRootDir, filename -> !filename.endsWith(".class"));
		} catch (IOException e) {
			LOGGER.error("Could not fully zip up a run of " + configuration.assignment, e);
		} finally {
			IOUtils.closeQuietly(zipper);
		}
		
		try {
			LOGGER.info("Deleting " + assignmentRootDir);
			FileUtils.deleteDirectory(assignmentRootDir);
		} catch (IOException e) {
			LOGGER.error("Could not delete the assignment folder.", e);
		}
	}

	private String generateFilename() {
		Date now = Date.from(Instant.now());
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.BUNDLED_TIMESTAMP);
		return sdf.format(now) + ".zip";
	}
}
