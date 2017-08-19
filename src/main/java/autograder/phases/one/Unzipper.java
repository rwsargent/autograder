package autograder.phases.one;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.StudentErrorRegistry;

/**
 * Unzips a submission zip file. 
 * @author ryans
 */
public class Unzipper {
	private Configuration configuration;
	private Pattern excludePattern;
	private Map<String, FileDirector> fileDirectors;
	
	private Logger LOGGER = Logger.getLogger(Unzipper.class.getName());
	private StudentErrorRegistry errorRegistry;

	@Inject
	public Unzipper(Configuration configuration, Map<String, FileDirector> fileDirectors, StudentErrorRegistry errorRegistry) {
		this.configuration = configuration;
		excludePattern = createExcludePattern();
		this.fileDirectors = fileDirectors;
		this.errorRegistry = errorRegistry;
	}

	/**
	 * 
	 * @param zipStream - Caller is responsible for closing this stream
	 * @param submission - Destination for zipfile
	 */
	public void unzipForSubmission(ZipInputStream zipStream, AutograderSubmission submission) {
		ZipEntry entry = null;
		try {
			while ((entry = zipStream.getNextEntry()) != null) {
				// check to see if we want to skip writing to disk
				if(entry.isDirectory()) {
					continue; //we'll be careful for structure needed for each file in the FileDirector
				}
				if(excludePattern.matcher(entry.getName()).find() || invalidFile(entry.getName())) {
					LOGGER.log(Level.FINE, "Excluding " + entry.getName());
					zipStream.closeEntry();
					continue;
				}
				String extension = FilenameUtils.getExtension(entry.getName());
				FileDirector fileDirector = fileDirectors.get(extension);
				if(fileDirector == null) { // I want to be exclusive. If I don't know how to download it, I'm not gonna
					LOGGER.log(Level.FINE, "Skipping " + entry.getName() + " because I don't know how to handle it!");
					continue;
				}
				File outfile = fileDirector.directFile(zipStream, submission, entry.getName());
				if(extension.equals("zip") || extension.equals("jar")) { // recursively unzip this damn thing.
					try(FileInputStream fis = new FileInputStream(outfile);
							ZipInputStream recurZipStream = new ZipInputStream(fis)) {
						unzipForSubmission(zipStream, submission);
					}
					FileUtils.forceDelete(outfile); // remove the zip file, as it was just expanded
				}
				zipStream.closeEntry();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to unzip " + submission, e);
			errorRegistry.addInvalidSubmission(submission);
		}
	}
	
	private boolean invalidFile(String entryName) {
		return FilenameUtils.getName(entryName).startsWith(".") ||
				entryName.isEmpty() || 
				entryName.startsWith("org.");
	}
	
	private Pattern createExcludePattern() {
		if(configuration.ignorePattern != null) {
			return Pattern.compile(configuration.ignorePattern);
		}
		return Pattern.compile("$^"); // matches empty strings.
	}
}
