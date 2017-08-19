package autograder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

import autograder.phases.one.BinaryFileDirector;
import autograder.phases.one.DefaultFileDirector;
import autograder.phases.one.FileDirector;
import autograder.phases.one.SourceFileDirector;

public class AutograderTestModule extends AbstractModule {

	@Override
	protected void configure() {
		MapBinder<String, FileDirector> directorBindings = MapBinder.newMapBinder(binder(), String.class, FileDirector.class);
		directorBindings.addBinding("java").to(SourceFileDirector.class);
		directorBindings.addBinding("class").to(BinaryFileDirector.class);
		directorBindings.addBinding("pdf").to(DefaultFileDirector.class);
		directorBindings.addBinding("zip").to(DefaultFileDirector.class);
		directorBindings.addBinding("txt").to(DefaultFileDirector.class);
		
		bind(String.class).annotatedWith(Names.named("configpath")).toInstance("src/test/resources/configuration_test.properties");
		bind(String.class).annotatedWith(Names.named("assignment")).toInstance("assignment01");
		
	}
}
