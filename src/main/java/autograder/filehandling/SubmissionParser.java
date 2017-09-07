package autograder.filehandling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.canvas.CanvasConnection;
import autograder.canvas.responses.Attachment;
import autograder.canvas.responses.Submission;
import autograder.configuration.Configuration;
import autograder.phases.one.FileDirector;
import autograder.phases.one.Unzipper;
import autograder.student.AutograderSubmission;
import autograder.student.StudentErrorRegistry;

/**
 * SubmissionParser will create a student for every submission it receives,
 * and write whatever attachemnts it has to disk. 
 * @author ryansargent
 */
public class SubmissionParser {
	
	private Pattern excludePattern;
	protected Configuration config;
	protected CanvasConnection connection;
	protected StudentErrorRegistry errorRegistry;
	protected Unzipper unzipper;
	private String assignment;
	
	private Logger LOGGER = LoggerFactory.getLogger(SubmissionParser.class);
	private Map<String, FileDirector> fileDirectors;
	
	@Inject
	public SubmissionParser(Configuration configuration, CanvasConnection connection, StudentErrorRegistry errorRegistry, @Named("assignment") String assignment, Unzipper unzipper, Map<String, FileDirector> fileDirectors) {
		this.config = configuration;
		this.connection = connection;
		this.errorRegistry = errorRegistry;
		this.assignment = assignment;
		this.unzipper = unzipper;
		this.excludePattern = createExcludePattern();
		this.fileDirectors = fileDirectors;
	}
	
	public AutograderSubmission parseAndCreateSubmission(Submission submission) {
		AutograderSubmission autoSubmission = new AutograderSubmission(submission, new File("submissions/" + assignment));
	  	if(submission.attachments != null ) {
			for(Attachment attachment : submission.attachments) {
				LOGGER.debug("Downloading " + attachment.filename + " for " + submission);
				writeAttatchmentToDisk(autoSubmission, attachment);
			}
		}
		return autoSubmission;
	}
	
	private void writeAttatchmentToDisk(AutograderSubmission submission, Attachment attachment) {
		if (excludePattern.matcher(attachment.filename).find()) {
			return;
		}
		byte[] filedata = connection.downloadFile(attachment.url);
		try (ByteArrayInputStream bais = new ByteArrayInputStream(filedata)) {
			// special case for zip files...oh well.
			if (canBeUnzipped(attachment)) {
				ZipInputStream zipStream = new ZipInputStream(bais);
				unzipper.unzipForSubmission(zipStream, submission);
				IOUtils.closeQuietly(zipStream);
			} else {
				FileDirector fileDirector = fileDirectors.get(FilenameUtils.getExtension(attachment.filename));
				if(fileDirector == null) {
					LOGGER.debug("Skipping " + attachment.filename + " as it has an non-configured extension.");
				}
				fileDirector.directFile(bais, submission, attachment.filename);
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't unzip attachment for " + submission, e);
		}
	}

	private boolean canBeUnzipped(Attachment attachment) {
		return attachment.filename.endsWith(".zip") || attachment.filename.endsWith(".jar");
	}
	
	private Pattern createExcludePattern() {
		if(config.ignorePattern != null) {
			return Pattern.compile(config.ignorePattern);
		}
		return Pattern.compile("$^"); // matches empty strings.
	}
}
