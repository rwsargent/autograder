package autograder.phases.three.uploaders;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import autograder.canvas.responses.Submission;
import autograder.configuration.Configuration;
import autograder.phases.three.uploaders.feedback.RateLimitedFeedback;
import autograder.student.AutograderSubmission;
import autograder.student.GradeCalculator;
import autograder.testutils.TestUtils;
import autograderutils.results.AutograderResult;

public class RateLimitTest {

	
	private GradeCalculator gradeCalculator;
	private RateLimitedFeedback commenter;
	private Configuration loadConfiguration;

	@Before
	public void setup(){
		gradeCalculator = Mockito.mock(GradeCalculator.class);
		loadConfiguration = TestUtils.loadConfiguration();
		loadConfiguration.submissionFeedbackThreshold = 5;
		loadConfiguration.scoreFeedbackThreshold = 35;
		commenter = new RateLimitedFeedback(loadConfiguration, gradeCalculator);
	}
	
	@Test
	public void feedbackShouldBeProduced() {
		AutograderSubmission submission;
		Mockito.when(gradeCalculator.calculateGrade(Matchers.any())).thenReturn(100.0);
		submission = Mockito.mock(AutograderSubmission.class);
		Mockito.when(submission.getResult()).thenReturn(new FakeResult());
		Submission submissionInfo = new Submission();
		submissionInfo.attempt = 1;
		submission.submissionInfo = submissionInfo;
		String feedback = commenter.getComment(submission);
		
		assertTrue(feedback.contains("4 graded submissions"));
	}
	
	@Test
	public void feedbackShouldNotBeProduced() {
		AutograderSubmission submission;
		Mockito.when(gradeCalculator.calculateGrade(Matchers.any())).thenReturn(100.0);
		submission = Mockito.mock(AutograderSubmission.class);
		Mockito.when(submission.getResult()).thenReturn(new FakeResult());
		Submission submissionInfo = new Submission();
		submissionInfo.attempt = 6;
		submission.submissionInfo = submissionInfo;
		String feedback = commenter.getComment(submission);
		
		assertTrue(feedback.contains("Your submission was successfully graded with a score greater than"));
	}
	
	@Test
	public void lastFeedBackToBeProduced() {
		AutograderSubmission submission;
		Mockito.when(gradeCalculator.calculateGrade(Matchers.any())).thenReturn(100.0);
		submission = Mockito.mock(AutograderSubmission.class);
		Mockito.when(submission.getResult()).thenReturn(new FakeResult());
		Submission submissionInfo = new Submission();
		submissionInfo.attempt = 5;
		submission.submissionInfo = submissionInfo;
		String feedback = commenter.getComment(submission);
		
		assertTrue(feedback.contains("0 graded submissions"));
	}
	
	@Test
	public void feedbackGivenWithBadScore() {
		AutograderSubmission submission;
		Mockito.when(gradeCalculator.calculateGrade(Matchers.any())).thenReturn(30.0);
		submission = Mockito.mock(AutograderSubmission.class);
		Mockito.when(submission.getResult()).thenReturn(new FakeResult());
		Submission submissionInfo = new Submission();
		submissionInfo.attempt = 6;
		submission.submissionInfo = submissionInfo;
		String feedback = commenter.getComment(submission);
		
		assertTrue(feedback.contains("feedback"));
	}
	
	private static class FakeResult extends AutograderResult {

		@Override
		public int getScore() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getTotal() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getFeedback() {
			// TODO Auto-generated method stub
			return "feedback";
		}

		@Override
		public String getSummary() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getTestResults() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getNumberOfTests() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
}
