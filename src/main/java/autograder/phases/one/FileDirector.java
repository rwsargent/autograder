package autograder.phases.one;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import autograder.student.AutograderSubmission;

/**
 * Directs files to be written to certain locations in an AutograderSubmission 
 * @author ryans
 */
public interface FileDirector {
	public File directFile(InputStream stream, AutograderSubmission submission, String filepath) throws IOException;
	
	public default int writeToDisk(InputStream stream, File destination) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(destination);
		int ret = IOUtils.copy(stream, fileOutputStream);
		IOUtils.closeQuietly(fileOutputStream);
		return ret;
	}
}
