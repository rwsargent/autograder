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
		
		Configuration conf = new Configuration("src/test/resources/configuration_test.properties");
		assertEquals("Tester", conf.graderClassName);
		assertEquals("username_test", conf.smtpUsername);
		assertEquals("password_test", conf.smtpPassword);
		assertEquals("smtp.gmail.com", conf.smtpHost);
	}

}
