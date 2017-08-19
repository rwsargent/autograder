package autograder.phases.one;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import autograder.student.AutograderSubmission;

/**
 * Anything runnable should be directed by this class. This class differs from Default and Source 
 * file directors by preserving the file structure.
 * @author ryans
 */
public class BinaryFileDirector implements FileDirector {

	@Override
	public File directFile(InputStream stream, AutograderSubmission submission, String filepath) throws IOException {
		File parentDirs = new File(submission.getClassesDirectory(), FilenameUtils.getPath(filepath));
		parentDirs.mkdirs();
		
		File destination = new File(parentDirs, FilenameUtils.getBaseName(filepath));
		writeToDisk(stream, destination);
		return destination;
	}

}
