package autograder.modules;

import java.security.Policy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import autograder.canvas.CanvasConnection;
import autograder.configuration.Configuration;
import autograder.phases.one.BinaryFileDirector;
import autograder.phases.one.DefaultFileDirector;
import autograder.phases.one.FileDirector;
import autograder.phases.one.OnlyAttemptedSubmissions;
import autograder.phases.one.SourceFileDirector;
import autograder.phases.one.StudentDownloader;
import autograder.phases.one.StudentDownloaderImpl;
import autograder.phases.one.SubmissionDownloader;
import autograder.phases.one.SubmissionFilter;
import autograder.phases.one.SubmissionsFromWeb;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.SubmissionUploader;
import autograder.phases.three.uploaders.GradeSubmissionUploader;
import autograder.phases.three.uploaders.WriteResultToDisk;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.InternalJavaCompiler;
import autograder.phases.two.workers.JUnitGrader;
import autograder.portal.PortalConnection;
import autograder.security.AutograderPolicy;
import autograder.security.AutograderSecurityManager;

public class DefaultModule extends AbstractModule {
	private Configuration config;

	public DefaultModule(Configuration configuration) {
		this.config = configuration;
	}

	@Override
	public void configure() {
		bind(Configuration.class).toInstance(this.config);
		
		Multibinder<Worker> workerBinder = Multibinder.newSetBinder(binder(), Worker.class);
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(JUnitGrader.class);

		bind(String.class).annotatedWith(Names.named("configpath")).toInstance(configPathString());
		bind(String.class).annotatedWith(Names.named("assignment")).toInstance(getAssignment());
		
		bind(PortalConnection.class).to(getPortal());
		bind(StudentDownloader.class).to(getStudentDownloader());
		bind(SubmissionDownloader.class).to(getSubmissionDownloader());
		bind(SubmissionFilter.class).to(getSubmissionFilter());
		
		//Security
		bind(SecurityManager.class).to(getSecurityManager());
		bind(Policy.class).to(getPolicy());
		
		Multibinder<SubmissionUploader> submissionUploaders = Multibinder.newSetBinder(binder(), SubmissionUploader.class);
		submissionUploaders.addBinding().to(WriteResultToDisk.class);
//		submissionUploaders.addBinding().to(PostFeedbackCommentsUploader.class);
		submissionUploaders.addBinding().to(GradeSubmissionUploader.class);
		
		Multibinder<AssignmentUploader> assignmentUploaders = Multibinder.newSetBinder(binder(), AssignmentUploader.class);
		assignmentUploaders.addBinding().toInstance(submission -> System.out.println("Assignment uploader size: " + submission.size()));
		
		setFileDirectors();
	}

	private Class<? extends Policy> getPolicy() {
		return AutograderPolicy.class;
	}

	private Class<? extends SecurityManager> getSecurityManager() {
		return AutograderSecurityManager.class;	
	}

	private Class<? extends SubmissionFilter> getSubmissionFilter() {
		return OnlyAttemptedSubmissions.class;
	}

	private Class<? extends PortalConnection> getPortal() {
		return CanvasConnection.class;
	}

	private Class<? extends SubmissionDownloader> getSubmissionDownloader() {
		return SubmissionsFromWeb.class;
	}

	protected Class<? extends StudentDownloader> getStudentDownloader() {
		return StudentDownloaderImpl.class;
	}

	protected String getAssignment() {
		return this.config.assignment;
	}

	protected String configPathString() {
		return "configuration.properties";
	}

	protected void setFileDirectors() {
		MapBinder<String, FileDirector> directorBindings = MapBinder.newMapBinder(binder(), String.class, FileDirector.class);
		directorBindings.addBinding("java").to(SourceFileDirector.class);
		directorBindings.addBinding("class").to(BinaryFileDirector.class);
		directorBindings.addBinding("pdf").to(DefaultFileDirector.class);
		directorBindings.addBinding("zip").to(DefaultFileDirector.class);
		directorBindings.addBinding("txt").to(DefaultFileDirector.class);
		
		
	}
}
