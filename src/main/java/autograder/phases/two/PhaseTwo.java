package autograder.phases.two;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

/**
 * This is the main workhorse class. It injects a set of Workers, which will be applied in order to
 * each student in the StudentMap. This returns a partition of the students per the ta.csv file to hand off
 * to the Uploader phase (PhaseThree). 
 * 
 * @author ryansargent
 *
 */
public class PhaseTwo {
	private Provider<Set<Worker>> mWorkers;
	
	private static Logger LOGGER = LoggerFactory.getLogger(PhaseTwo.class);
	@Inject
	public PhaseTwo(Provider<Set<Worker>> workers) {
		mWorkers = workers;
	}
	
	/**
	 * 
	 * @param studentMap - All student submissions we know about. 
	 * @return - Students randomly partitioned amongst the students in 
	 * @throws InterruptedException
	 */
	public AutograderSubmissionMap phaseTwo(AutograderSubmissionMap studentMap) throws InterruptedException {
		ExecutorService threadPool = startWork(studentMap.listStudents());
		threadPool.shutdown();
		threadPool.awaitTermination(8, TimeUnit.HOURS);
		return studentMap;
	}
	
	// Trying out the ExecutorService. Each job is a run through all the workers for each student.
	private ExecutorService startWork(List<AutograderSubmission> queue) {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
		for(AutograderSubmission student : queue) {
			threadPool.submit(() -> {
				LOGGER.info("Starting work on " + student.studentInfo.name);
				try{ 
					for(Worker worker : mWorkers.get()) {
						worker.doWork(student);
					}
				} catch (Throwable t) {
					LOGGER.error("Could not finish work on " + student, t);
				}
			});
		}
		return threadPool;
	}
}