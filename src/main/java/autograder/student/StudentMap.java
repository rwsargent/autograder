package autograder.student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autograder.canvas.responses.User;

public class StudentMap {
	private Map<String, Student> mUIdMap;
	private Map<Integer, Student> mCanvasMap;
	private List<Student> mStudents;
	
	public StudentMap() {
		mUIdMap = new HashMap<>();
		mCanvasMap = new HashMap<>();
		mStudents = new ArrayList<>();
	}
	
	public StudentMap(User[] students) {
		this();
		fillMaps(students);
	}
	
	private void fillMaps(User[] students) {
		for(User user : students) {
			addStudent(user);
		}
	}

	public void addStudent(User user) {
		Student student = new Student(user);
		mUIdMap.put(user.sis_user_id, student);
		mCanvasMap.put(user.id, student);
		mStudents.add(student);
	}

	public Student get(int canvasId) {
		return mCanvasMap.get(canvasId);
	}
	
	public Student get(String uId) {
		return mUIdMap.get(uId);
	}
	
	public List<Student> listStudents() {
		return mStudents;
	}
}
