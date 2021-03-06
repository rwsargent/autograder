package autograder.grading.jarring;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;

public class TestJarring {

	AutograderSubmission mStudent;
	private Configuration config;
	@Before
	public void setUp() throws Exception {
		mStudent = new AutograderSubmission(new File("src/test/resources/jar_student"), null);
		mStudent.getSourceDirectory();
		
		config = new Configuration("src/test/resources/configuration_test.properties");
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void happyPath() {
		config.mainClass = "Main";
		Jarrer jarrer = new Jarrer(config);
		
		jarrer.doWork(mStudent);
	}
	
	@Test
	public void canFindIt() {
		config.mainClass = "Find";
		Jarrer jarrer = new Jarrer(config);
		jarrer.doWork(mStudent);
	}
	
	@Test
	public void fullyQualifedPath() {
		config.mainClass = "foo.Hidden";
		Jarrer jarrer = new Jarrer(config);
		jarrer.doWork(mStudent);
	}

}
