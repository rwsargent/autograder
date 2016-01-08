package autograder.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {

	@Before
	public void setup() {
		
	}
	
	@Test
	public void configurationTest() {
//		System.getProperties().put("smtpHost", "smtp.gmail.com");
//		System.getProperties().put("smtpPort", "587");
		
		Configuration conf = Configuration.getConfiguration("configuration.properties");
		assertEquals("Tester", conf.graderClassName);
		assertEquals("username_test", conf.smtpUsername);
		assertEquals("password_test", conf.smtpPassword);
		assertEquals("smtp.gmail.com", conf.smtpHost);
	}

}
