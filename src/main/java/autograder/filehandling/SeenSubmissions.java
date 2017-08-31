package autograder.filehandling;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.canvas.responses.Submission;
import autograder.student.AutograderSubmission;

/**
 * This class handles the logic of whether or not a submission has been 
 * seen for the current assignment.
 * 
 * The file is simply a list of submission ids, separated by new lines
 * 
 * It's saved the basedir/assignmentname/
 * @author ryans
 */
public class SeenSubmissions {
	
	private Set<String> submissionIds = new HashSet<>();
	private String assignmentName;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SeenSubmissions.class);
	
	@Inject
	public SeenSubmissions(@Named("assignment") String assignment) {
		assignmentName = assignment;
		loadSubmissions();
	}
	
	public void loadSubmissions() {
		submissionIds.clear();
		Path path = generatePath();
		if(path.toFile().exists()) {
			try(Stream<String> idStream = Files.lines(path)) {
				idStream.forEach(submissionIds::add);
			} catch (IOException e) {
				LOGGER.debug("No submissions were loaded for " + assignmentName, e);
			}
		}
	}

	// at cwd level
	private Path generatePath() {
		return Paths.get(assignmentName + ".saved");
	}
	
	public boolean alreadySeenSubmission(Submission submission) {
		return submissionIds.contains(buildIdentifier(submission));
	}
	
	public boolean addSubmission(AutograderSubmission submission) {
		return submissionIds.add(buildIdentifier(submission.submissionInfo));
	}
	
	protected String buildIdentifier(Submission submission) {
		return submission.assignment_id + "_" + submission.user_id + "_" + submission.attempt;
	}
	
	/**
	 * Writes file to the current assignment's folder.
	 * 
	 * For example, if you're the current assignment is "Assignment 4", the saved file will be
	 * basedir/a04/seensubmissions.txt
	 * @return 
	 */
	public String save() {
		Path savePath = generatePath();
		try(BufferedWriter writer = Files.newBufferedWriter(savePath)) {
			for(String id : submissionIds) {
				writer.write(id + "\n");
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return savePath.toString();
	}
}
