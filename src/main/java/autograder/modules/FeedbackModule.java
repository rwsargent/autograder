package autograder.modules;

import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.PostFeedbackCommentsUploader;
import autograder.phases.three.uploaders.SaveResultUploader;
import autograder.phases.three.uploaders.feedback.CommentContentGetter;
import autograder.phases.three.uploaders.feedback.RateLimitedFeedback;

public class FeedbackModule extends DefaultModule {

	public FeedbackModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public void configure() {
		super.configure();
		bind(CommentContentGetter.class).to(RateLimitedFeedback.class);
	}
	
	@Override
	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
		super.addSubmissionUploaders(submissionUploaders);
		submissionUploaders.addBinding().to(PostFeedbackCommentsUploader.class);
		submissionUploaders.addBinding().to(SaveResultUploader.class);
	}
}
