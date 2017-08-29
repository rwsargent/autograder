package autograder.phases.three.uploaders;

import java.io.File;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import autograder.Constants;
import autograder.configuration.Configuration;
import autograder.filehandling.Zipper;
import autograder.student.AutograderSubmissionMap;

public class WrapUpGraderRun {

	private Zipper zipper;
	private Configuration configuration;

	@Inject
	public WrapUpGraderRun(Configuration configuration, Zipper zipper) {
		this.zipper = zipper;
		this.configuration = configuration;
	}
	
	public void cleanUp(AutograderSubmissionMap submissions) {
		// zip up submissions and place them somewhere. 
		
		File assignmentRootDir = Paths.get(Constants.SUBMISSIONS, configuration.assignment).toFile();
		try {
			ZipFile outputZip = zipper.zipDirectory(assignmentRootDir);
			
		} finally {
			IOUtils.closeQuietly(zipper);
		}
	}
}
