package autograder.modules;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.filehandling.PredefinedPartition;
import autograder.filehandling.SubmissionPartitioner;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.uploaders.EmailBundleToTasUploader;
import autograder.phases.two.Worker;

public class DistributionModule extends DefaultModule {

	public DistributionModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public void configure() {
		super.configure();
		bind(SubmissionPartitioner.class).to(PredefinedPartition.class);
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
//		workerBinder.addBinding().to(GetExtraAssignmentSubmissions.class);
	}
	
	@Override
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		assignmentUploaders.addBinding().to(EmailBundleToTasUploader.class);
	}

}
