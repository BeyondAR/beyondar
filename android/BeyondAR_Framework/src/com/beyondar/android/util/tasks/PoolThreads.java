/**
 * 
 */
package com.beyondar.android.util.tasks;

import java.util.ArrayList;

/**
 * @author Joan Puig Sanz (joanpuigsanz@gmail.com)
 * 
 *         This class is used to define a pool of threads to establish the
 *         maximum allowable threads running at the same time.<br>
 *         This class is used by the {@link TaskExecutor}
 * 
 */
public class PoolThreads implements IOnFinishTaskListener, IOnThreadFromPoolStop {

	/** Max number of threads in the pool by default */
	public static final int DEFAULT_MAX_THREADS = 4;

	/**
	 * Max default time (in milliseconds) that a thread will wait without any
	 * task assigned before being removed
	 */
	public static final int DEFAULT_MAX_THREAD_INACTIVE_TIME = 5000;

	private int maxThreads;

	private int threadIdGen;

	private int threadCounter;

	/** The vector with the free threads */
	private ArrayList<ThreadFromPool> poolTherad_free;

	private long maxThreadInactiveTime;

	// /** The vector with the busy threads */
	// private Vector poolTherad_busy;

	// private int threadsInUse;

	private boolean killThreads;

	private IOnFinishTaskListener onFinishTaskListener;

	// private String tag = "PoolThreads";

	/**
	 * Define the maximum number of threads in the pool
	 * 
	 * @param maxThreads
	 *            The maximum number of threads that the pool will allow
	 */
	public PoolThreads(int maxThreads) {
		this.maxThreads = maxThreads;
		doConstructor(DEFAULT_MAX_THREAD_INACTIVE_TIME);

	}

	/**
	 * The maximum number of threads is {@link #DEFAULT_MAX_THREADS}=
	 * {@value #DEFAULT_MAX_THREADS}
	 * 
	 */
	public PoolThreads() {
		this.maxThreads = DEFAULT_MAX_THREADS;
		doConstructor(DEFAULT_MAX_THREAD_INACTIVE_TIME);

	}

	/**
	 * Define the maximum number of threads in the pool
	 * 
	 * @param maxThreads
	 *            The maximum number of threads that the pool will allow
	 * @param maxThreadInactiveTime
	 *            When the pool will create a thread, it will uses this time to
	 *            set the max inactive time for a thread before being removed
	 */
	public PoolThreads(int maxThreads, long maxThreadInactiveTime) {
		this.maxThreads = maxThreads;
		doConstructor(maxThreadInactiveTime);
		this.maxThreadInactiveTime = maxThreadInactiveTime;
	}

	/**
	 * When the pool will create a thread, it will uses this time to set the max
	 * inactive time for a thread before being removed. Using this method, the
	 * system will remove all the existing threads from the pool (The current
	 * task, if there are any task being executed, will be finished as expected)
	 * 
	 * @param maxThreadInactiveTime
	 *            The new time in milliseconds
	 */
	public void setMaxThreadInactiveTime(long maxThreadInactiveTime) {
		this.maxThreadInactiveTime = maxThreadInactiveTime;
	}

	/**
	 * Get the maximum time which a thread will be inactive before being removed
	 * 
	 * @return
	 */
	public long getMaxThreadInactiveTime() {
		return this.maxThreadInactiveTime;
	}

	/**
	 * Specify if you want all the threads as a temporal threads. Its means that
	 * when the thread will finish the task, it will be destroyed, and if a new
	 * task arrive, a new temporal thread will be created
	 */
	public void TemporalThreads(boolean temporal) {
		killThreads = temporal;

		for (int i = 0; i < poolTherad_free.size(); i++) {
			ThreadFromPool thread = (ThreadFromPool) poolTherad_free.get(i);
			thread.stopTask();
		}

	}

	private void doConstructor(long maxThreadInactiveTime) {
		threadCounter = 0;
		threadIdGen = 0;
		killThreads = false;
		this.maxThreadInactiveTime = maxThreadInactiveTime;

		poolTherad_free = new ArrayList<ThreadFromPool>(maxThreads);
		// poolTherad_busy = new Vector(maxThreads, 1);

		// threadsInUse = 0;
	}

	// /**
	// * To control the number of threads in use
	// *
	// * @param value
	// * set +1 or -1
	// */
	// public synchronized void threadsInUse(int value) {
	//
	// }

	// public synchronized void notifyThreadRelease(ThreadFromPool thread) {
	//
	// poolTherad_free.addElement(thread);
	// // poolTherad_busy.addElement(thread);
	// threadsInUse = threadsInUse - 1;
	// }

	/**
	 * Stop all the sleeping threads
	 */
	public void stopAllSleepingThreads() {
		for (int i = 0; i < poolTherad_free.size(); i++) {
			ThreadFromPool thread = (ThreadFromPool) poolTherad_free.get(i);
			thread.stopTask();
		}
	}

	/**
	 * Get the maxim number of concurrent Tasks. the default value is 6
	 * 
	 * @return
	 */
	public int getMaxConcurrentTasks() {
		return maxThreads;
	}

	/**
	 * Set the maxim number of concurrent Tasks. the default value is 6
	 * 
	 * @param max
	 */
	public void setMaxConcurrentTasks(int max) {
		maxThreads = max;
	}

	/**
	 * Get the maximum number of threads available in the pool
	 * 
	 * @return maximum number of threads.
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * Get a free thread to execute the task
	 * 
	 */
	public synchronized ThreadFromPool getFreeThread() {
		ThreadFromPool thread = null;
		if (poolTherad_free.size() > 0) {
			thread = (ThreadFromPool) poolTherad_free.get(0);
			poolTherad_free.remove(0);
		} else if (threadCounter < maxThreads) {
			thread = new ThreadFromPool(threadIdGen, this, this,
					maxThreadInactiveTime);
			thread.start();
			threadCounter++;
			threadIdGen++;
		}

		return thread;
	}

	/**
	 * Set the listener to execute when a task is finished
	 * 
	 * @param onFinishTaskListener
	 */
	public void setOnFinishTaskListener(
			IOnFinishTaskListener onFinishTaskListener) {
		this.onFinishTaskListener = onFinishTaskListener;
	}

	public void onFinishTask(TaskResult result, Task task, ThreadFromPool thread) {
		if (killThreads) {
			thread.stopTask();
		} else {

			thread.setMaxThreadInactiveTime(maxThreadInactiveTime);
			// poolTherad_busy.removeElement(thread);
			poolTherad_free.add(thread);
			// LogCat.i(tag, "Adding thread from task id=" + task.getIdTask());
		}
		if (onFinishTaskListener != null && task != null) {
			onFinishTaskListener.onFinishTask(result, task, null);
		}
	}

	public void onThreadStops(ThreadFromPool thread) {
		removeThread(thread);
		// LogCat.i(tag, "-- id Thread=" + thread.getIdTask() +
		// " has been stopped");

	}

	/**
	 * This method notify the pool that the specified thread has stopped
	 * 
	 * @param thread
	 */
	private synchronized void removeThread(ThreadFromPool thread) {
		threadCounter--;
	}

}
