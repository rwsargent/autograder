package autograder.filehandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import autograder.Constants;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;
import autograder.tas.TAInfo;

/**
 * If you want TAs to grade the same student every time, create a configuration json file with 
 * the following format:
 * <pre>
 * {
 *    "name" : {
 *    	"students" : ["uids"], 
 *      "email" : "example@example.com",
 *      "name" : "name"
 *    }, 
 *    ...
 * }
 * </pre>
 * 
 * The values in the students list should refer to the id used in the submission map. 
 * The returned map will have a key of the TA's email address, and a value of a list of 
 * the AutograderSubmissions as defined by configuration. 
 * @author ryans
 */
public class PredefinedPartition implements SubmissionPartitioner{
	
	private Map<String, TAInfo> tas;

	@Inject
	public PredefinedPartition(@Named(Constants.Names.TAS) Map<String, TAInfo> tas) {
		this.tas = tas;
	}

	/**
	 * 
	 * @param submissions - The submission map built in PhaseOne
	 * @return - A map of email address to list of submissions per TA, as defined from configuration
	 */
	@Override
	public Map<String, List<AutograderSubmission>> partition(AutograderSubmissionMap submissions) {
		Map<String, List<AutograderSubmission>> partition = new HashMap<>();
		
		for(Entry<String, TAInfo> entry : tas.entrySet()) {
			TAInfo ta = entry.getValue();
			List<AutograderSubmission> students = new ArrayList<>();

			for(String uid : ta.students) {
				AutograderSubmission submission = submissions.get(uid);
				if(submission != null) {
					students.add(submission);
				}
			}
			partition.put(ta.email, students);
		}
		return partition;
	}
}
