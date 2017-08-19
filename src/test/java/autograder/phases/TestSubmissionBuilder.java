package autograder.phases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

import autograder.canvas.responses.User;
import autograder.configuration.AssignmentProperties;
import autograder.phases.one.SubmissionBuilder;
import autograder.student.AutograderSubmissionMap;

public class TestSubmissionBuilder {

	SubmissionBuilder mSubmissionBuilder = new SubmissionBuilder();
	
	@Test
	public void setReInit() {
		HashMap<Integer, User> mockUserMap = mockUsers();
		AutograderSubmissionMap studentMap = mSubmissionBuilder.build(mockUserMap, new File("src/test/resources/students"));
		
		assertNotNull(studentMap);
		assertEquals(2, studentMap.listStudents().size());
		AssignmentProperties studentAassignprops = studentMap.get(1).assignProps;
		assertNotNull(studentAassignprops);
		assertEquals(studentAassignprops.partner_uid, "u0000002");
		assertNotNull(studentMap.get(2).assignProps);
	}

	private HashMap<Integer, User> mockUsers() {
		HashMap<Integer, User> mockUserMap = new HashMap<Integer, User>();
		mockUserMap.put(1, buildUser(1, "Student A", "u0000001"));
		mockUserMap.put(2, buildUser(2, "Parterner A", "u0000002"));
		return mockUserMap;
	}

	private User buildUser(int id, String name, String uid) {
		User user = new User();
		user.id = id;
		user.name = name;
		user.sortableName = name;
		user.sis_user_id = uid;
		return user;
	}
}
