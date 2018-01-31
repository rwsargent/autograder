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
 * Upload. After all the workers have run during PhaseTwo, PhaseThree kicks in to 
 * distribute the information. Uploading doesn't necessarily mean over a socket, 
 * this is for writing to disk, networked file system, and email. Anything that happens
 * after the grading is handled by PhaseThree.
 * 
 * <h4>Plugin Points </h4>
 * {@link SubmissionUploader }
 * 	 - These will happen PER submission. The reason this is broken out is after all 
 *     SubmissionUploaders have run on a submission, the SeenSubmissions runs, adding it to the 
 *     seen list and saving the data. This way if something catastrophic happens during the 
 *     upload phase, you'll only lose / duplicate one submissions worth of work. 
 *     
 *  {@link AssignmentUploader}
 *    - This uploader executes for the entire run. The idea here is anything that needs to happen 
 *      for the entire assignment, not just per submission should be handled in the AssignmentUploader
 *      
 * 
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
