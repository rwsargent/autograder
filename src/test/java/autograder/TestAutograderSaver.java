package autograder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
	
	@Test(expected=IllegalStateException.class)
	public void testNewAutograderRun() {
		saver.getAssignment();
	}
	
	@Test
	public void testSavingAutograderInformation() {
		config.assignment = "newassignment";
		assertFalse(saver.isCurrentAssignemnt());
		saver.readFile(new File("src/test/resources/save_test.txt"));
		assertTrue(saver.isCurrentAssignemnt());
	}
	
	@Test
	public void testWritingFile() {
		config.assignment = "writetest";
		File outfile = new File("outfile.test");
		saver.writeSavedFile(outfile);
		
		saver.readFile(outfile);
		assertTrue(saver.isCurrentAssignemnt());
		assertEquals(saver.getAssignment(), "writetest");
		
		outfile.delete();
	}
}
