import java.util.concurrent.*;

/**
 * This program finds the number, within a specified range of integers from 1 to
 * whatever positive integer greater than one is chosen, with the most divisors.
 * The user can choose how many threads will divide the task. This program makes
 * use of a thread pool that can access a ConcurrentLinkedQueue for tasks and it
 * makes use of a LinkedBlockingQueue for threads to safely retrieve divisors
 * results for comparison in finding the most divisors and its number.
 * 
 * @author toddnordquist September 19, 2018
 *
 */

public class MostDivisorsWithQueues {

	// ---------------- Global Variables --------------------

	private static WorkerThread[] worker;

	private static ConcurrentLinkedQueue<Runnable> taskQueue;
	// holds individual tasks.

	private static LinkedBlockingQueue<CountDivisorsResult> resultQueue
	= new LinkedBlockingQueue<MostDivisorsWithQueues.CountDivisorsResult>();
	// holds individual results from tasks

	/**
	 * The starting and ending point for the range of integers that are tested
	 * for number of divisors.
	 *
	 */

	private static final int START = 0;
	private static final int END = 10000;

	private static volatile int maxDivisors; // Maximum number of divisors
	// seen so

	// far.
	private static volatile int numWithMax = 0; // A value of N that had the
												// given number of divisors.

	private static MostDivisorsWithQueues theP;

	// End Global Variables

	/**
	 * This method compares the greatest number of divisors and its number up to
	 * this point in the program with each thread's greatest number of divisors
	 * and its number. This method is synchronized because it's possible for two
	 * or more threads to call this method at relatively the same time.
	 */
	synchronized private static void updateDivisor(
			CountDivisorsResult result) {

		resultQueue.add(result);
		CountDivisorsResult aResult = null;
		try {

			aResult = resultQueue.take();

		} catch (InterruptedException e) {

		}

		if (aResult.numDivisors > maxDivisors) {
			maxDivisors = aResult.numDivisors;
			numWithMax = aResult.theNum;
		}

	}

	/**
	 * A Thread belonging to this class will count primes in a specified range
	 * of integers. The range is from min to max, inclusive, where min and max
	 * are given as parameters to the constructor. After counting, the thread
	 * outputs a message about the number of primes that it has found, and it
	 * adds its count to the overall total by calling the addToTotal(int)
	 * method.
	 */
	private class CountDivisorsTask implements Runnable {
		int min, max;
		int count = 0;
		int N = 0;
		int divisorsForGivenNum = 0;

		public CountDivisorsTask(int N) {
			this.N = N;
		}

		public void run() {
			divisorsForGivenNum = countDivisors(N);
			CountDivisorsResult result;
			result = new CountDivisorsResult(divisorsForGivenNum,
					N);

			updateDivisor(result);
		}
	} // end class CountDivisorsTask

	/**
	 * Count the divisors between min and max, inclusive for a given integer.
	 * 
	 * @param num
	 *            the integer to be tested for its number of divisors.
	 * @return an int that is the number of divisors for the passed number.
	 */

	private int countDivisors(int num) {

		// int N; // One of the integers whose divisors we have to count.
		// N = num;
		int divisorCount;
		/*
		 * Process all the remaining values of N from 2 to 100000, and update
		 * the values of maxDivisors and numWithMax whenever we find a value of
		 * N that has more divisors than the current value of maxDivisors.
		 */

		int D; // A number to be tested to see if it's a divisor of N.
		divisorCount = 0; // Number of divisors of N.
		for (D = 1; D <= num; D++) { // Count the divisors of N.
			if (num % D == 0)
				divisorCount++;

		}
		return divisorCount;
	}

	private class CountDivisorsResult {

		int numDivisors; // the number of divisors for a given number
		int theNum; // the given number that has a certain number of
					// divisors.

		public CountDivisorsResult(int numDivisors, int theNum) {
			this.numDivisors = numDivisors;
			this.theNum = theNum;
		}
	}

	/**
	 * Counts the number of divisors in the range from (start+1) to (END), using
	 * a specified number of threads. The total elapsed time is printed. Note
	 * that
	 * 
	 * @param numberOfThreads
	 */
	private static void startQueOfTasks(int numberOfThreads) {
		// int increment = END / numberOfThreads;
		System.out.println("\nCounting divisors between "
				+ (START + 1) + " and " + (END) + " using "
				+ numberOfThreads + " threads...\n");
		long startTime = System.currentTimeMillis();

		taskQueue = new ConcurrentLinkedQueue<Runnable>();

		for (int N = 1; N <= END; N++) {
			CountDivisorsTask task = theP.new CountDivisorsTask(N);
			taskQueue.add(task);
		}

		worker = new WorkerThread[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++)
			worker[i] = new WorkerThread();
		maxDivisors = 0;
		for (int i = 0; i < numberOfThreads; i++)
			worker[i].start();
		for (int i = 0; i < numberOfThreads; i++) {
			while (worker[i].isAlive()) {
				try {
					worker[i].join();
				} catch (InterruptedException e) {

				}
			}
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Among integers between 1 and " + END
				+ ",");
		System.out.println("The maximum number of divisors is "
				+ maxDivisors);
		System.out.println("A number with " + maxDivisors
				+ " divisors is " + numWithMax);
		System.out.println("\nTotal elapsed time:  "
				+ (elapsedTime / 1000.0) + " seconds.\n");
	} // end startQueOfTasks

	public static void main(String[] args) {

		theP = new MostDivisorsWithQueues();

		int processors = Runtime.getRuntime()
				.availableProcessors();
		if (processors == 1)
			System.out
					.println("Your computer has only 1 available processor.\n");
		else
			System.out.println("Your computer has " + processors
					+ " available processors.\n");

		int numberOfThreads = 0;
		while (numberOfThreads < 1 || numberOfThreads > 50) {
			System.out
					.print("How many threads do you want to use  (from 1 to 50 ) ?  ");
			numberOfThreads = com.eck.ch12.ex1.TextIO.getlnInt();
			if (numberOfThreads < 1 || numberOfThreads > 50)
				System.out
						.println("Please choose a number 1 through "
								+ processors + "!");
		}
		startQueOfTasks(numberOfThreads);
	} // end main()

	/**
	 * This class defines the worker threads that carry out the tasks. A
	 * WorkerThread runs in a loop in which it retrieves a task from the
	 * taskQueue and calls the run() method in that task. The thread terminates
	 * when the queue is empty. (Note that for this to work properly, all the
	 * tasks must be placed into the queue before the thread is started. If the
	 * queue is empty when the thread starts, the thread will simply exit
	 * immediately.)
	 */
	private static class WorkerThread extends Thread {
		public void run() {
			while (true) {
				Runnable task = taskQueue.poll();
				if (task == null)
					break;
				task.run();

			}
		}
	} // end class WorkerThread

} // end class MostDivisorsWithQueues
