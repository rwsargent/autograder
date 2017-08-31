package autograder.phases.one;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import autograder.AutograderTestModule;
import autograder.canvas.responses.User;
import autograder.student.AutograderSubmission;

/**
 * Test different file directors
 * @author ryans
 */
public class TestFileDirectors {
	
	@Test
	public void testInjection() {
		AbstractModule module = new AutograderTestModule();
		Injector createInjector = Guice.createInjector(module);
		Unzipper unzipper = createInjector.getInstance(Unzipper.class);
		
		try(InputStream fileStream = getClass().getResourceAsStream("project.zip");
				ZipInputStream zipStream = new ZipInputStream(fileStream)) {
			AutograderSubmission submission = new AutograderSubmission(new File("submissions"), createUser());
			unzipper.unzipForSubmission(zipStream, submission);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testUnzipJar() {
		AbstractModule module = new AutograderTestModule();
		Injector createInjector = Guice.createInjector(module);
		Unzipper unzipper = createInjector.getInstance(Unzipper.class);
		try(InputStream fileStream = getClass().getResourceAsStream("project.jar");
				ZipInputStream zipStream = new ZipInputStream(fileStream)) {
			AutograderSubmission submission = new AutograderSubmission(new File("submissions"), createUser());
			unzipper.unzipForSubmission(zipStream, submission);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	private User createUser() {
		User user = new User();
		return user;
	}
}
