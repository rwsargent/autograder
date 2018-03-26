package autograder.filehandling;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import autograder.configuration.Configuration;
import autograder.student.AutograderSubmission;
import autograder.student.SubmissionPair;

public class BundlerTest {
	
	Bundler bundler;
	private HashMap<String, List<SubmissionPair>> studentToTaMap;

	@Before
	public void setUp() throws Exception {
		bundler = new Bundler(new Configuration("src/test/resources/configuration_test.properties"));
		studentToTaMap = new HashMap<>();
		
		List<SubmissionPair> submissionPairs = new ArrayList<>();
		SubmissionPair submissions = new SubmissionPair();
		submissions.submitter= new AutograderSubmission(new File("src/test/resources/students/studentSubmitterA"), null);
		submissions.partner = new AutograderSubmission(new File("src/test/resources/students/studentPartnerA"), null);
		submissionPairs.add(submissions);
		
		studentToTaMap.put("TA", submissionPairs);
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void test() {
		Map<String, File> ret = bundler.bundlePair(studentToTaMap);
		ret.toString();
	}

}
