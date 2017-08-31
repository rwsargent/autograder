package autograder.phases.two.workers;

import org.junit.Assert;
import org.junit.Test;

import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.testutils.TestUtils;
import autograderutils.AutograderResult;

public class TestJUnitGrader {
	
	@Test
	public void test() {
		Configuration config = TestUtils.loadConfiguration();
		config.graderClassName = "assignment01.SimpleAssignmentGrader";
		AutograderSubmission submission = TestUtils.getSubmission("4321_2017-03-25T05:23:30Z");
		
		JUnitGrader grader = new JUnitGrader(config);
		grader.doWork(submission);
		
		AutograderResult result = submission.getResult();
		Assert.assertNotNull(result);
	}
	
}
