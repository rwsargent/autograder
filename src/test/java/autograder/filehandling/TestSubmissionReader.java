package autograder.filehandling;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import autograder.Constants;

public class TestSubmissionReader {

	@Before
	public void setup() {
		new File(Constants.SUBMISSIONS).mkdir();
	}
	
	@After
	public void tearDown() {
		File subDir = new File(Constants.SUBMISSIONS);
		try {
			FileUtils.cleanDirectory(subDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
