package autograder.mailer;

import java.io.File;

import org.junit.Test;

import autograder.configuration.Configuration;
import autograder.testutils.TestUtils;

public class TestMailer {

	@Test
	public void test() {
		Configuration configuration = TestUtils.loadConfiguration();
		Mailer mailer = new Mailer(configuration);
		mailer.sendMailWithAttachment("rw.sarge@gmail.com", "Test", "test!", new File("ta.json"));
		
	}

}
