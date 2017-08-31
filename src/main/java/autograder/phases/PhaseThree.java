package autograder.phases;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autograder.filehandling.SeenSubmissions;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.WrapUpGraderRun;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

/**
 * The upload phase. This will gather any information 
 * @author ryans
 *
 */
public class PhaseThree {
	
	private SeenSubmissions seenSubmission;
	
	private Set<SubmissionUploader> uploaders;
	private Set<AssignmentUploader> assignmentUploaders;

	private WrapUpGraderRun wrapperUpper;
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(PhaseThree.class);

	@Inject
	public PhaseThree(Set<SubmissionUploader> uploaders, Set<AssignmentUploader> assignmentUploaders, SeenSubmissions seenSubmissions, WrapUpGraderRun wrapperUpper) {
		this.uploaders = uploaders;
		this.assignmentUploaders = assignmentUploaders;
		this.seenSubmission = seenSubmissions;
		this.wrapperUpper = wrapperUpper;
	}
	
	public void wrapItUp(AutograderSubmissionMap map)  {
		LOGGER.info("Starting Phase Three!");
		
		for(AutograderSubmission submission : map.listStudents()) {
			for(SubmissionUploader uploader : uploaders) {
				try {
					uploader.upload(submission);
				} catch (Exception e ) {
					LOGGER.error("Failed submission upload on " + submission, e);
				}
			}
			seenSubmission.addSubmission(submission);
			seenSubmission.save();
		}
		
		for(AssignmentUploader uploader : assignmentUploaders) {
			uploader.upload(map);
		}
		
		wrapperUpper.cleanUp(map);
		
		LOGGER.info("Done!");
	}
	
}
