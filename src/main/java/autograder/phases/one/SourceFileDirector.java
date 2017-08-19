package autograder.phases.one;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import autograder.student.AutograderSubmission;

/**
 * Any file considered source code is placed in the "src" directory for the Autograder Submission
 * @author ryans
 */
public class SourceFileDirector implements FileDirector {

	@Override
	public File directFile(InputStream stream, AutograderSubmission submission, String filepath) throws IOException {
		File destination = new File(submission.getSourceDirectory(), FilenameUtils.getBaseName(filepath));
		writeToDisk(stream, destination);
		return destination;
	}

}
