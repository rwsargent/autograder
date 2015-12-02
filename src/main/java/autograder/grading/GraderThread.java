package autograder.grading;

import java.util.Queue;

public class GraderThread extends Thread {

	Queue<WorkJob> mQueue;
	public GraderThread(Queue<WorkJob> workQueue) {
		mQueue = workQueue;
	}
	
	@Override
	public void run() {
		WorkJob job = null;
		while((job = mQueue.poll()) != null) {
			job.doWork();
		}
	}
	
	public class WorkJob {
		public void doWork() {
			
		}
	}
}
