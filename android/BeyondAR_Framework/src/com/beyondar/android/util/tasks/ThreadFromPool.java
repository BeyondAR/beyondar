/**
 * 
 */
package com.beyondar.android.util.tasks;

import java.util.Vector;

/**
 * @author Joan Puig Sanz (joanpuigsanz@gmail.com)
 * 
 */
public class ThreadFromPool extends Thread {

	private final Object lock = new Object();

	private long id;

	private boolean stop;
	private Vector<Task> taskList;
	private IOnThreadFromPoolStop onThreadFromPoolStop;
	private IOnFinishTaskListener taskListener;

	//private String tag = "ThreadFromPool";

	private long maxSleepingTime;
	private long lastTime;

	/**
	 * 
	 * @param id
	 *            the thread id
	 * @param onFinishTaskListener
	 *            Listener to know when a thread finish its job (and is waiting)
	 * @param onThreadFromPoolStop
	 *            Listener to notify when a thread stop
	 * @param maxInactiveTime
	 *            This is the time in milliseconds which this thread will wait
	 *            until end if is inactive. So after maxInactiveTime if any task
	 *            is assigned, this thread will be removed. If this value is 0,
	 *            this thread allays will be alive until stop it.
	 */
	public ThreadFromPool(int id, IOnFinishTaskListener onFinishTaskListener,
			IOnThreadFromPoolStop onThreadFromPoolStop, long maxInactiveTime) {
		this.taskListener = onFinishTaskListener;
		this.id = id;
		this.onThreadFromPoolStop = onThreadFromPoolStop;
		taskList = new Vector<Task>(1, 1);
		stop = false;
		maxSleepingTime = maxInactiveTime;
	}

	/**
	 * Define the new thread maxInactiveTime. This is the time in milliseconds
	 * which this thread will wait until end if is inactive. So after
	 * maxInactiveTime if any task is assigned, this thread will be removed. If
	 * this value is 0, this thread allays will be alive until stop it.
	 * 
	 * @param maxSleepingTime
	 */
	public void setMaxThreadInactiveTime(long maxSleepingTime) {
		if (this.maxSleepingTime != maxSleepingTime) {
			this.maxSleepingTime = maxSleepingTime;
			wakeUp();
		}
	}

	/**
	 * Get the id of this thread
	 * 
	 * @return
	 */
	public long getIdTask() {
		return id;
	}

	/**
	 * Stop this thread. But first it will try to do all the task in the queue
	 */
	public void stopTask() {

		taskList.removeAllElements();
		if (onThreadFromPoolStop != null) {
			onThreadFromPoolStop.onThreadStops(this);
		}

		synchronized (lock) {
			stop = true;
			lock.notify();
		}
		
		//stop();

	}

	public void interrupt() {
		stopTask();
		super.interrupt();
	}

	/**
	 * Add the next task to process, if an other task is executing, the new task
	 * will be added to the queue
	 * 
	 * @param task
	 *            next task
	 * @return Return true if the task has been added, false otherwise.
	 */
	public synchronized boolean addTask(Task task) {
		if (taskList.size() > 1) {
			// LogCat.i(tag,
			// "WARNINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGg  "
			// + task.getIdTask());
		}

		synchronized (lock) {
			if (stop) {
				return false;
			}
			lock.notify();
			taskList.addElement(task);
			// LogCat.i(tag, "====thead id=" + id + "  Task id=" +
			// task.getIdTask());
		}
		return true;
	}

	/**
	 * Force the thread to check for new tasks
	 */
	public void wakeUp() {
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * Finalize the task and do the last job (notify the
	 * {@link IOnFinishTaskListener})
	 * 
	 * @param task
	 *            The finalized task
	 * @param result
	 *            The result of this task
	 */
	private void finalizeTask(Task task, TaskResult result) {
		if (taskListener != null) {
			taskListener.onFinishTask(result, task, this);
		}
	}

	public void run() {
		while (!stop) {

			for (int i = 0; i < taskList.size(); i++) {

				Task task = (Task) taskList.elementAt(i);

				// LogCat.i(tag, "###Running task " + task.getIdTask());
				TaskResult result = task.executeTask();

				finalizeTask(task, result);

				synchronized (lock) {
					if (taskList.size() > 0) {
						taskList.removeElementAt(i);
						i--;
					}
				}
			}

			synchronized (lock) {
				if (!stop && taskList.size() == 0) {
					try {
						lastTime = System.currentTimeMillis();
						lock.wait(maxSleepingTime);
						//long timeT = (System.currentTimeMillis() - lastTime);
						// LogCat.i(tag, "*************  timeT=" + timeT
						// + " Max Sleepingtime=" + maxSleepingTime);
						if ((System.currentTimeMillis() - lastTime) > maxSleepingTime
								&& taskList.size() == 0) {
							stop = true;
							// LogCat.i(tag,
							// "Thread Killed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   id="
							// + getIdTask());
							if (onThreadFromPoolStop != null) {
								onThreadFromPoolStop.onThreadStops(this);
							}
							return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}

	}

}
