package autograder.phases.three.uploaders;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import autograder.configuration.Configuration;
import autograder.filehandling.SubmissionParser;
import autograder.mailer.Mailer;
import autograder.portal.PortalConnection;
import autograder.settingsfiles.MossSettings;
import autograder.testutils.TestUtils;
import it.zielke.moji.SocketClient;

public class MossUploaderTest {
	
	private MossSettings mossSettings;
	private MossUploader uploader;
	private PortalConnection portalConnetion;
	private SubmissionParser parser;
	private Mailer emailer;
	private SocketClient socketClient;
	
	@Before()
	public void setup() {
		Configuration config = TestUtils.loadConfiguration();
		
		try(FileReader reader = new FileReader( new File("src/test/resources/MossSettings.json"))) {
			mossSettings = new Gson().fromJson(reader, MossSettings.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
		portalConnetion = mock(PortalConnection.class);
		parser = mock(SubmissionParser.class);
		emailer = mock(Mailer.class);
		socketClient = mock(SocketClient.class);
		uploader = new MossUploader(mossSettings, config, portalConnetion, parser, emailer, socketClient);
	}

	@Test
	public void testReadingMossSettingsFile() {
		try(FileReader reader = new FileReader( new File("src/test/resources/MossSettings.json"))) {
			MossSettings mossSettings = new Gson().fromJson(reader, MossSettings.class);
			
			Assert.assertEquals("123456789", mossSettings.userId);
			Assert.assertEquals(2, mossSettings.courseToAssignments.size());
			Assert.assertTrue(mossSettings.courseToAssignments.containsKey("cid"));
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void happyPath() {
		
		uploader.upload(null);
	}
}
