package autograder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import autograder.configuration.Configuration;

public class TestAutograderSaver {

	Configuration config = new Configuration("src/test/resources/configuration_test.properties");
	AutograderSaver saver;
	
	@Before
	public void setup() {
		saver = new AutograderSaver(config);
	}
	
	@Test
	public void testNewAutograderRun() {
		assertNull(saver.getAssignment());
	}
	
	@Test
	public void testSavingAutograderInformation() {
		config.assignment = "newassignment";
		assertFalse(saver.isCurrentAssignemnt());
		File outfile = new File("tempOutFile.autograder");
		saver.writeSavedFile(outfile);
		assertTrue(outfile.exists());
	}
}
