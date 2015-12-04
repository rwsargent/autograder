package autograder.grading;

import java.util.Queue;

public class GraderThread extends Thread {

	Queue<WorkJob> mQueue;
	Grader mGrader;
	public GraderThread(Queue<WorkJob> workQueue) {
		mQueue = workQueue;
		mGrader = new Grader();
	}
	
	@Override
	public void run() {
		WorkJob job = null;
		while((job = mQueue.poll()) != null) {
			job.gradeStudentSubmission(mGrader);
		}
	}
}
