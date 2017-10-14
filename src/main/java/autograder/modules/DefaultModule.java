package autograder.modules;

import static autograder.Constants.Names.ASSIGNMENT;
import static autograder.Constants.Names.CONFIG_PATH;
import static autograder.Constants.Names.TAS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import autograder.canvas.CanvasConnection;
import autograder.configuration.Configuration;
import autograder.metrics.MetricReport;
import autograder.metrics.MissedTestMetric;
import autograder.metrics.ScoreMetric;
import autograder.metrics.SubmissionAttemptMetrics;
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
import autograder.phases.three.uploaders.WriteResultToDisk;
import autograder.phases.two.Worker;
import autograder.phases.two.workers.InternalJavaCompiler;
import autograder.phases.two.workers.JUnitGrader;
import autograder.portal.PortalConnection;
import autograder.security.AutograderSecurityManager;
import autograder.tas.TAInfo;

/**
 * This is the Default configuration for the Autograder.
 * 
 * Any specialized configurations should extend this one, and override the 
 * desired behavior.
 * @author ryansargent
 */
public class DefaultModule extends AbstractModule {
	private Configuration config;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	public DefaultModule(Configuration configuration) {
		this.config = configuration;
	}

	@Override
	public void configure() {
		bind(Configuration.class).toInstance(this.config);
		
		bind(String.class).annotatedWith(Names.named(CONFIG_PATH)).toInstance(configPathString());
		bind(String.class).annotatedWith(Names.named(ASSIGNMENT)).toInstance(getAssignment());
		bind(new TypeLiteral<Map<String, TAInfo>>(){}).annotatedWith(Names.named(TAS)).toProvider(getTaInfo());
		
		bind(PortalConnection.class).to(getPortal());
		bind(StudentDownloader.class).to(getStudentDownloader());
		bind(SubmissionDownloader.class).to(getSubmissionDownloader());
		bind(SubmissionFilter.class).to(getSubmissionFilter());
		
		//Security
		bind(SecurityManager.class).to(getSecurityManager());
//		bind(Policy.class).to(getPolicy());
		
		Multibinder<Worker> workerBinder = Multibinder.newSetBinder(binder(), Worker.class);
		addPhaseTwoWorkers(workerBinder);
		
		Multibinder<MetricReport> metricsBinder = Multibinder.newSetBinder(binder(), MetricReport.class);
		addMetricReports(metricsBinder);
		
		Multibinder<SubmissionUploader> submissionUploaders = Multibinder.newSetBinder(binder(), SubmissionUploader.class);
		addSubmissionUploaders(submissionUploaders);
		
		Multibinder<AssignmentUploader> assignmentUploaders = Multibinder.newSetBinder(binder(), AssignmentUploader.class);
		addAssignmentUploaders(assignmentUploaders);
		
		setFileDirectors();
	}

	private void addMetricReports(Multibinder<MetricReport> metricsBinder) {
		metricsBinder.addBinding().to(ScoreMetric.class);
		metricsBinder.addBinding().to(SubmissionAttemptMetrics.class);
		metricsBinder.addBinding().to(MissedTestMetric.class);
	}

	private Provider<Map<String, TAInfo>> getTaInfo() {
		Gson gson = new Gson();
		try(FileInputStream file = new FileInputStream(new File(config.taFilePath));
				InputStreamReader reader = new InputStreamReader(file)) {
			TypeToken<Map<String, TAInfo>> type = new TypeToken<Map<String, TAInfo>>(){};
			final Map<String, TAInfo> taInfo = gson.fromJson(reader, type.getType());
			return () ->  taInfo;
		} catch (IOException e) {
			LOGGER.error("Could not read TA file.", e);
		};
		return () -> new HashMap<>();
	}
	
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		// no-op
	}

	protected void addSubmissionUploaders(Multibinder<SubmissionUploader> submissionUploaders) {
		submissionUploaders.addBinding().to(WriteResultToDisk.class);
	}

	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		workerBinder.addBinding().to(InternalJavaCompiler.class);
		workerBinder.addBinding().to(JUnitGrader.class);
	}

//	protected Class<? extends Policy> getPolicy() {
//		return AutograderPolicy.class;
//	}

	protected Class<? extends SecurityManager> getSecurityManager() {
		return AutograderSecurityManager.class;	
	}

	protected Class<? extends SubmissionFilter> getSubmissionFilter() {
		return OnlyAttemptedSubmissions.class;
	}

	protected Class<? extends PortalConnection> getPortal() {
		return CanvasConnection.class;
	}

	protected Class<? extends SubmissionDownloader> getSubmissionDownloader() {
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
