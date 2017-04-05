package autograder.phases.two;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	
	public HashMap<String, Set<SubmissionPair>> phaseTwo(StudentMap studentMap) throws InterruptedException {
		SubmissionData submissionData = mPairer.pairSubmissions(studentMap);
		Queue<Student> queue = buildWorkQueue(submissionData);

		ExecutorService threadPool = startWork(queue);
		threadPool.shutdown();
		HashMap<String, Set<SubmissionPair>> taParition = mSubPartitioner.partition(submissionData);
		
		threadPool.awaitTermination(4, TimeUnit.HOURS);
		return taParition;
	}
	
	private ExecutorService startWork(Queue<Student> queue) {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
		while(!queue.isEmpty()) {
			Student student = queue.poll();
			threadPool.submit(() -> {
				System.out.println("Starting work on " + student.studentInfo.name);
				for(Worker worker : mWorkers.get()) {
					worker.doWork(student);
				}
			});
		}
		return threadPool;

	}

	private Queue<Student> buildWorkQueue(SubmissionData submissionData) {
		Set<SubmissionPair> pairs = submissionData.pairs;
		Queue<Student> queue = new LinkedList<>();
		
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
