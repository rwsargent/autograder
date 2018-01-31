package autograder.modules;

import org.mockito.Mockito;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.filehandling.RandomizedPartitioner;
import autograder.filehandling.SubmissionPartitioner;
import autograder.mailer.Mailer;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.AssignmentMetricsUploader;
import autograder.phases.three.uploaders.CreateStudentToScoreTSV;
import autograder.phases.three.uploaders.SaveResultUploader;
import autograder.phases.three.uploaders.WrapUpGraderRun;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.InternalJavaCompiler;
import autograder.phases.two.workers.JUnitGrader;

public class TestingModule extends DefaultModule {

	public TestingModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public void configure() {
		super.configure();
		bind(SubmissionPartitioner.class).to(RandomizedPartitioner.class);
		bind(Mailer.class).toInstance(Mockito.mock(Mailer.class)); // don't want to mail anything
		bind(WrapUpGraderRun.class).toInstance(Mockito.mock(WrapUpGraderRun.class)); // don't delete anything either
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(JUnitGrader.class);
	}
	
	@Override
	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
//		submissionUploaders.addBinding().to(WriteResultToDisk.class);
//		submissionUploaders.addBinding().toInstance(sub -> {
//			System.out.println(sub + ": " + sub.getResult().getScore());
//		});
		submissionUploaders.addBinding().to(SaveResultUploader.class);
	}
	
	@Override
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		assignmentUploaders.addBinding().to(AssignmentMetricsUploader.class);
	}
}
