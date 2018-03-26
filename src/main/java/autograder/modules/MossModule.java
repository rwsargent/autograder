package autograder.modules;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import autograder.configuration.Configuration;
import autograder.phases.one.FileDirector;
import autograder.phases.one.SourceFileDirector;
import autograder.phases.three.AssignmentUploader;
import autograder.phases.three.uploaders.MossUploader;
import autograder.phases.two.Worker;
import autograder.settingsfiles.MossSettings;

public class MossModule extends DefaultModule {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MossModule.class);

	public MossModule(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public void configure() {
		super.configure();
		bind(MossSettings.class).toInstance(getMossSettings());
	}
	
	@Override
	protected void addPhaseTwoWorkers(Multibinder<Worker> workerBinder) {
		//NO-OP Phase two, the only work that needs to happen is in the UploadPhase
	}
	
	@Override
	protected void setFileDirectors() {
		MapBinder<String, FileDirector> directorBindings = MapBinder.newMapBinder(binder(), String.class, FileDirector.class);
		directorBindings.addBinding("java").to(SourceFileDirector.class);
	}

	@Override
	protected void addAssignmentUploaders(Multibinder<AssignmentUploader> assignmentUploaders) {
		assignmentUploaders.addBinding().to(MossUploader.class);
	}
	
	private MossSettings getMossSettings() {
		try(FileReader read = new FileReader(new File("MossSettings.json"))) {
			return new Gson().fromJson(read, MossSettings.class);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return new MossSettings();
		}
	}
}
