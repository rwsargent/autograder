package autograder.phases.two;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import com.google.inject.Inject;

import autograder.student.Student;
import autograder.student.StudentMap;
import autograder.student.SubmissionPair;
import autograder.student.SubmissionPairer;
import autograder.student.SubmissionPairer.SubmissionData;
import autograder.tas.PartitionSubmissions;

/**
 * This is the main workhorse class. It injects a set of Workers, which will be applied in order to
 * each student in the StudentMap. This returns a partition of the students per the ta.csv file to hand off
 * to the Uploader phase (PhaseThree). 
 * 
 * @author ryansargent
 *
 */
public class PhaseTwo {
	private SubmissionPairer mPairer;
	private Provider<Set<Worker>> mWorkers;
	private PartitionSubmissions mSubPartitioner;
	
	@Inject
	public PhaseTwo(SubmissionPairer pairer, Provider<Set<Worker>> workers, PartitionSubmissions submissionPartitioner) {
		mPairer = pairer;
		mWorkers = workers;
		mSubPartitioner = submissionPartitioner;
	}
	
	/**
	 * 
	 * @param studentMap - All student submissions we know about. 
	 * @return - Students randomly partitioned amongst the students in 
	 * @throws InterruptedException
	 */
	public HashMap<String, Set<SubmissionPair>> phaseTwo(StudentMap studentMap) throws InterruptedException {
		SubmissionData submissionData = mPairer.pairSubmissions(studentMap);
		List<Student> students = getStudentsToGrade(submissionData);
		ExecutorService threadPool = startWork(students);
		threadPool.shutdown();
		HashMap<String, Set<SubmissionPair>> taParition = mSubPartitioner.partition(submissionData);
		threadPool.awaitTermination(8, TimeUnit.HOURS);
		return taParition;
	}
	
	// Trying out the ExecutorService. Each job is a run through all the workers for each student.
	private ExecutorService startWork(List<Student> queue) {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
		for(Student student : queue) {
			threadPool.submit(() -> {
				System.out.println("Starting work on " + student.studentInfo.name);
				try{ 
					for(Worker worker : mWorkers.get()) {
						worker.doWork(student);
					}
				} catch (Throwable t) {
					System.err.println("Could not finish work on " + student + " - " + t.getMessage());
				}
			});
		}
		return threadPool;
	}

	/**
	 * If a student is paired, I'll only grade one of their assignments. If they're not paired, or if
	 * I can't figure out who submitted the code, I try to grade both of them. 
	 * 
	 * Single students are added at the end. 
	 * @param submissionData
	 * @return
	 */
	private List<Student> getStudentsToGrade(SubmissionData submissionData) {
		Set<SubmissionPair> pairs = submissionData.pairs;
		List<Student> queue = new LinkedList<>();
		
		for(SubmissionPair pair : pairs) {
			queue.add(pair.submitter);
			if(!pair.sorted) {
				queue.add(pair.partner);
			}
		}
		
		for(Student student : submissionData.invalidStudents) {
			queue.add(student);
		}
		
		return queue;
	}
}
