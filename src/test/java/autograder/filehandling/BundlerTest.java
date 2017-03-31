package autograder.filehandling;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import autograder.configuration.Configuration;
import autograder.student.Student;
import autograder.student.SubmissionPair;

public class BundlerTest {
	
	Bundler bundler;
	private HashMap<String, Set<SubmissionPair>> studentToTaMap;

	@Before
	public void setUp() throws Exception {
		bundler = new Bundler(new Configuration("src/test/resources/configuration_test.properties"));
		studentToTaMap = new HashMap<>();
		
		Set<SubmissionPair> submissionPairs = new HashSet<>();
		SubmissionPair submissions = new SubmissionPair();
		submissions.submitter= new Student(new File("src/test/resources/students/studentSubmitterA"), null);
		submissions.partner = new Student(new File("src/test/resources/students/studentPartnerA"), null);
		submissionPairs.add(submissions);
		
		studentToTaMap.put("TA", submissionPairs);
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void test() {
		Map<String, File> ret = bundler.bundleStudents(studentToTaMap);
		ret.toString();
	}

}
