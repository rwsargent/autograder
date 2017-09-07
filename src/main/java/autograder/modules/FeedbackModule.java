package autograder.modules;

import javax.inject.Inject;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.PostFeedbackCommentsUploader;

public class FeedbackModule extends DefaultModule {

	@Inject
	public FeedbackModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
		super.addSubmissionUploaders(submissionUploaders);
		submissionUploaders.addBinding().to(PostFeedbackCommentsUploader.class);
	}
}
