package autograder.mailer;

import java.io.File;

import org.junit.Test;

public class TestMailer {

	@Test
	public void test() {
		Mailer mailer = new Mailer();
		mailer.sendMailWithAttachment("rw.sarge@gmail.com", "Test", "test!", new File("ta.csv"));
		
	}

}
