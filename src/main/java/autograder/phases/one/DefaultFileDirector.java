package autograder.phases.one;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.student.AutograderSubmission;

/**
 * This will write the file to the top level of the Autograder Submission, using the base filename. 
 * NOTE: This will overwrite files named the same, but in different directories from a zip file.
 * @author ryans
 */
public class DefaultFileDirector implements FileDirector {
	private Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Override
	public File directFile(InputStream fileData, AutograderSubmission submission, String filepath) throws FileNotFoundException, IOException {
		String baseName = FilenameUtils.getName(filepath);
		File destination = new File(submission.getDirectory(), baseName);
		if(destination.exists()) {
			LOGGER.warn("The file" + filepath + " is going to be replace the old file at " + baseName);
		}
		writeToDisk(fileData, destination);
		return destination;
	}
}
