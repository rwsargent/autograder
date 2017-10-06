package autograder.phases;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.configuration.Configuration;
import autograder.phases.two.Worker;
import autograder.student.AutograderSubmission;
import autograder.student.AutograderSubmissionMap;

/**
 * This is the main workhorse class. It injects a set of Workers, which will be applied in order to
 * each student in the StudentMap. This returns a partition of the students per the ta.csv file to hand off
 * to the Uploader phase (PhaseThree). 
 * 
 * <h4> Plugin Points </h4>
 * 
 * Workers 
 * 	- This set of {@link Worker} object will be executed one after another per submission. While the 
 * 	workers are synchronized for each submission, submissions are run in parallel. 
 * 
 * {@link SecurityManager}
 * 	- There's a chance that code will be run that didn't originate from the Autograder during the worker 
 *  process. The SecurityManager will help sandbox the environment. 
 * @author ryansargent 2017
 *
 */
public class PhaseTwo {
	private Provider<Set<Worker>> mWorkers;

	private SecurityManager securityManager;

	private Configuration config;
	private static Logger LOGGER = LoggerFactory.getLogger(PhaseTwo.class);
	
	@Inject
	public PhaseTwo(Provider<Set<Worker>> workers, SecurityManager securityManager, Configuration configuration) {
		mWorkers = workers;
		this.securityManager = securityManager;
		this.config = configuration;
	}
	
	/**
	 * During phase two, each student submissions queued as a job on a threadPool 
	 * (as an {@link ExecutorService}). Submissions will be run in parallel to each other, 
	 * however each worker will be run synchronized per worker (one after another). 
	 * 
	 * The executor service will forcibly shut down after a configurable
	 * amount of time (in minutes), defaulted to 10. 
	 * 
	 * @param studentMap - All submissions for this autograder run 
	 * @return - the same studentMap supplied. 
	 * @throws InterruptedException
	 *  	- If an interruption occurs during the multithreading
	 */
	public AutograderSubmissionMap phaseTwo(AutograderSubmissionMap studentMap) throws InterruptedException {
		LOGGER.info("Phase two starting...");
		System.setSecurityManager(securityManager);
		ExecutorService threadPool = startWork(studentMap.listStudents());
		LOGGER.info("Jobs are queued");
		threadPool.shutdown();
		if(!threadPool.awaitTermination(getTimeout(), TimeUnit.MINUTES)) {
			LOGGER.warn("Threadpool had to be forcibly terminated while grading.");
		} else {
			LOGGER.info("Jobs finished cleanly");
		}
		
		System.setSecurityManager(null);
		
		// kill running threads?
		killInfiniteThreadsGeneratedByJUnit();
		
		LOGGER.info("Phase two finished");
		return studentMap;
	}

	// Yeah yeah yeah, it's a hack. So sue me. Teach students 
    // to stop writing infinite loops, and I won't have to do this.
	@SuppressWarnings("deprecation")
	private void killInfiniteThreadsGeneratedByJUnit() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for(Thread t : threadSet) {
			if(t.getName().contains("Thread-"))  {
				LOGGER.info("Killing " + t.getName());
				t.stop();
			}
		}
	}
	
	private long getTimeout() {
		return config.phaseTwoTimeout <= 0 ? 10 : config.phaseTwoTimeout;
	}
	
	// Trying out the ExecutorService. Each job is a run through all the workers for each student.
	private ExecutorService startWork(List<AutograderSubmission> queue) {
		
		ExecutorService threadPool = Executors.newFixedThreadPool(getThreadPoolCount());
		for(AutograderSubmission student : queue) {
			threadPool.submit(() -> {
				LOGGER.info("Starting work on " + student);
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

	private int getThreadPoolCount() {
		int threadCount = config.threadCount == 0 ? Runtime.getRuntime().availableProcessors() : config.threadCount;
		if(threadCount < 1 ) {
			threadCount = 1;
		}
		return threadCount;
	}
}