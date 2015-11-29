package autograder.configuration;

import java.util.HashMap;
import java.util.Map;

public class StudentSubmissionRegistry {
	
	private static StudentSubmissionRegistry mInstance;
	private Map<Integer, Student> mIdMap;
	private Map<String, Student> mNameMap;
	public static synchronized StudentSubmissionRegistry getInstance() {
		if(mInstance == null ) {
			mInstance = new StudentSubmissionRegistry();
		}
		return mInstance;
	}
	
	private StudentSubmissionRegistry(){
		mIdMap = new HashMap<>();
		mNameMap = new HashMap<>();
	}; 
	
	public synchronized void createStudentSubmission(String name, int canvasId) {
		addStudent(new Student(name, canvasId));
	}
	
	public synchronized Student createStudentSubmission(String name, String canvasId) {
		int id = Integer.parseInt(canvasId);
		Student student = new Student(name, id); 
		addStudent(student);
		return student;
	}
	
	public synchronized Student getStudentById(int id) {
		return mIdMap.get(id);
	}
	
	public synchronized Student getStudentById(String id) {
		int intId = Integer.parseInt(id);
		return mIdMap.get(intId);
	}
	
	public synchronized Student getStudentByName(String name) {
		return mNameMap.get(name);
	}
	
	private synchronized void addStudent(Student student) {
		mIdMap.put(student.canvasId, student);
		mNameMap.put(student.name, student);
	}
	
}