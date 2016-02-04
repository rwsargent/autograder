package autograder.student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import autograder.grading.WorkJob;

@Deprecated
public class StudentSubmissionRegistry {
	private static StudentSubmissionRegistry mInstance;
	private Map<Integer, Student> mIdMap;
	private Map<String, Student> mNameMap;
	public static synchronized StudentSubmissionRegistry getInstance() {
//		if(mInstance == null ) {
//			mInstance = new StudentSubmissionRegistry();
//			mInstance.addStudent(new Student("placeholder", -1));
//		}
		return mInstance;
	}
	
	private StudentSubmissionRegistry(){
		mIdMap = new HashMap<>();
		mNameMap = new HashMap<>();
	}; 
	
	public Queue<WorkJob> buildQueueFromSubmissions() {
		Queue<WorkJob> queue = new LinkedBlockingQueue<>(mNameMap.size());
		mNameMap.forEach((k,v)->queue.add(new WorkJob(v)));
		return queue;
	}
	
	public synchronized void createStudentSubmission(String name, int canvasId) {
//		addStudent(new Student(name, canvasId));
	}
	
	public synchronized Student createStudentSubmission(String name, String canvasId) {
//		int id = Integer.parseInt(canvasId);
//		Student student = new Student(name, id); 
//		addStudent(student);
//		return student;
		return null;
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
	
	public List<Student> toList() {
		return new ArrayList<Student>(mIdMap.values());
	}
	
	public void forEach(BiConsumer<String, Student> func) {
		mNameMap.forEach(func);
	}
}