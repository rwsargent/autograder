package autograder.mailer;

import java.io.File;

import org.junit.Test;

import autograder.configuration.Configuration;

public class TestMailer {

	@Test
	public void test() {
		Configuration configuration = null;
		Mailer mailer = new Mailer(configuration);
		mailer.sendMailWithAttachment("rw.sarge@gmail.com", "Test", "test!", new File("ta.csv"));
		
	}

}
