package autograder.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import autograder.canvas.CanvasConnection;
import autograder.configuration.Configuration;
import autograder.phases.one.BinaryFileDirector;
import autograder.phases.one.DefaultFileDirector;
import autograder.phases.one.FileDirector;
import autograder.phases.one.SourceFileDirector;
import autograder.phases.one.StudentDownloader;
import autograder.phases.one.StudentDownloaderImpl;
import autograder.phases.one.SubmissionDownloader;
import autograder.phases.one.SubmissionsFromWeb;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.InternalJavaCompiler;
import autograder.portal.PortalConnection;

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

		bind(String.class).annotatedWith(Names.named("configpath")).toInstance(configPathString());
		bind(String.class).annotatedWith(Names.named("assignment")).toInstance(getAssignment());
		
		bind(PortalConnection.class).to(getPortal());
		bind(StudentDownloader.class).to(getStudentDownloader());
		bind(SubmissionDownloader.class).to(getSubmissionDownloader());
		
		
		setFileDirectors();
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
