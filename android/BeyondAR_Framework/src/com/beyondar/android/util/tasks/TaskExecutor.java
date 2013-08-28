/*
 * Copyright (C) 2013 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.beyondar.android.util.tasks;

import java.util.Vector;

/**
 * 
 *         This class manage the all {@link Task}'s instances to create an
 *         efficient way to do it.
 * 
 */
public class TaskExecutor {

	// private String tag = "TaskExecutor";

	private static TaskExecutor mThis;

	/** Timer queue for asynchronous tasks */
	private Vector<Task> queueAsyncTasks;

	/** FIFO Queue for synchronous tasks */
	private Vector<Task> queueSyncTasks;

	private Vector<TaskResult> taskHistory;

	private PoolThreads pool;

	private Object sharedLock = new Object();

	private CoreThread coreThread;;

	private static Object mLock = new Object();

	/** set this to -1 to wait until a new task arrive */
	private long timeToWait;

	private boolean isBackground;

	private TaskExecutor(int maxThreads, long maxThreadInactiveTime) {
		timeToWait = -1;
		taskHistory = new Vector<TaskResult>();
		queueSyncTasks = new Vector<Task>();
		queueAsyncTasks = new Vector<Task>();
		coreThread = new CoreThread();
		pool = new PoolThreads(maxThreads, maxThreadInactiveTime);
		pool.setOnFinishTaskListener(coreThread);
		isBackground = false;

		coreThread.start();
	}

	/**
	 * Create a custom taskExecutor. This instance will not share any threat
	 * with the may pool
	 * 
	 * @param maxThreads
	 *            The maximum number of threads that the pool will allow
	 * 
	 * @param maxThreadInactiveTime
	 *            When the pool will create a thread, it will uses this time to
	 *            set the max inactive time for a thread before being removed
	 * 
	 * @return The instance with a new TaskExecutor.
	 */
	public static TaskExecutor createNewInstance(int maxThreads, long maxThreadInactiveTime) {

		return new TaskExecutor(maxThreads, maxThreadInactiveTime);
	}

	/**
	 * get the unique instance of this class
	 * 
	 * @return
	 */
	public static TaskExecutor getInstance() {

		if (mThis == null) {
			synchronized (mLock) {
				if (mThis == null) {
					mThis = new TaskExecutor(PoolThreads.DEFAULT_MAX_THREADS,
							PoolThreads.DEFAULT_MAX_THREAD_INACTIVE_TIME);
				}
			}
		}
		return mThis;
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
	public void setMaxThradInactiveTime(long maxTime) {
		pool.setMaxThreadInactiveTime(maxTime);
	}

	/**
	 * Get the maximum time which a thread will be inactive before being removed
	 * 
	 * @return
	 */
	public long getMaxThradInactiveTime() {
		return pool.getMaxThreadInactiveTime();
	}

	/**
	 * Add {@link Task} or {@link TimerTask}. It will be processed depending of
	 * the type
	 * 
	 * @param task
	 */
	public synchronized void addTask(Task task) {
		// LogCat.i(tag, "Adding task id =" + task.getIdTask());
		if (task.getTaskType() == Task.TASK_TYPE_TIMER) {
			if (queueAsyncTasks == null) {
				queueAsyncTasks = new Vector<Task>();
			}
			queueAsyncTasks.addElement(task);
		} else {
			if (queueSyncTasks == null) {
				queueSyncTasks = new Vector<Task>();
			}
			queueSyncTasks.addElement(task);
		}
		if (!task.isWaitingUntilOtherTaskFinishes()) {
			coreThread.processTasks();
		}
	}

	/**
	 * Sleep the taskExecutor, but if there are any task that can run in
	 * background, it will be executed. <br>
	 * Don't forget to call wakeUp() to notify the taskExecutor
	 */
	public void sleep() {
		this.isBackground = true;
	}

	/**
	 * After a sleep, wake up the TaskExecutor
	 */
	public void wakeUp() {
		this.isBackground = false;
		coreThread.processTasks();
	}

	/**
	 * Make all the threads in the pool(including existing threads) temporal ,
	 * but first, all the treads will finish the assigned tasks. If all the
	 * thread are stopped and an other task arrive, a temporal thread will be
	 * created to do the task.
	 */
	public void enableTemporalThreads() {
		pool.TemporalThreads(true);
		pool.stopAllSleepingThreads();
	}

	/**
	 * Make all the threads in the pool(including existing threads) persistent.
	 * It means that if a thread will finish the task, the thread will sleep
	 * until an other task will be assigned to the thread
	 */
	public void disableTemporalThreads() {
		pool.TemporalThreads(false);
	}

	/**
	 * Stop {@link TaskExecutor} and erase all the tasks. The tasks already
	 * assigned to a thread will be processed, but not the others. < br>
	 * 
	 * IMPORTANT!!! All the TaskExecutor configuration saved, like
	 * temporalThreads() ,setMaxThradInactiveTime(), etc, will not be saved.
	 */
	public void stopTaskExecutor() {
		enableTemporalThreads();
		pool.stopAllSleepingThreads();

		coreThread.stopCoreThread();
		removeAllQueuedTask();

		coreThread = null;
		pool = null;
		mThis = null;
		System.gc();
	}

	/**
	 * Remove all Task form the task manager. The task that are already running
	 * will be removed when they will finish the task
	 * 
	 */
	public void removeAllQueuedTask() {
		removeQueuedAsincTask();
		removeQueuedSyncTask();
	}

	/**
	 * Remove all asinctasks ({@link TimerTask}) form the task manager.
	 */
	public void removeQueuedAsincTask() {
		queueAsyncTasks.removeAllElements();

	}

	/**
	 * Remove all sync tasks ({@link Task}) form the task manager
	 */
	public void removeQueuedSyncTask() {
		queueSyncTasks.removeAllElements();
	}

	/**
	 * Erase all the history tasks
	 */
	public void cleanAllHistory() {
		taskHistory.removeAllElements();
	}

	/**
	 * Search the {@link TaskResult} inside the history according the task id
	 * 
	 * @param id
	 *            The task id
	 * @return The {@link TaskResult} of the task, or null if this task has not
	 *         founded
	 */
	public TaskResult searchHistoryTask(long id) {

		for (int i = 0; i < taskHistory.size(); i++) {
			TaskResult result = (TaskResult) taskHistory.elementAt(i);
			if (result.idTask() == id) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Get the hole history with all the {@link TaskResult}
	 * 
	 * @return A list containing all the {@link TaskResult}
	 */
	public Vector<TaskResult> getHistory() {
		return taskHistory;
	}

	/**
	 * Removes the first occurrence of the argument from the history. If the
	 * object is found in the history, each component in the history with an
	 * index greater or equal to the object's index is shifted downward to have
	 * an index one smaller than the value it had previously.
	 * 
	 * @param result
	 *            The {@link TaskResult} to remove
	 * @return true if the argument was a component of the history; false
	 *         otherwise.
	 */
	public boolean cleanHistory(TaskResult result) {
		return taskHistory.removeElement(result);
	}
	private class CoreThread extends Thread implements IOnFinishTaskListener {

		private boolean stop = false;

		private final Object lock;

		/**
		 * Create the core thread, Use the lock to synchronize the wait and
		 * notify method
		 * 
		 * @param lock
		 *            just add a new object
		 */
		private CoreThread() {
			this.lock = sharedLock;
		}

		/**
		 * Stop this thread!
		 */
		private void stopCoreThread() {
			stop = true;
			synchronized (lock) {
				lock.notifyAll();
			}
		}

		/**
		 * Notify the {@link CoreThread} to process the SyncTask and asyncTask
		 * Queues
		 */
		private void processTasks() {

			synchronized (lock) {
				lock.notify();
			}
		}

		public void run() {
			// LogCat.i(
			// tag,
			// "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Starting core thread");

			while (!stop) {
				// //LogCat.i(tag, "== Processing syncQueues");

				if ((queueSyncTasks.size() != 0)) {
					if (executeSyncTasks()) {
						// do stuff??
					} else {
						// do stuff??
					}
				}

				// //LogCat.i(tag, "## Processing asyncQueues");
				if ((queueAsyncTasks.size() != 0)) {
					if (executeAsyncTasks()) {
						// do stuff??
					} else {
						// do stuff??
					}
				}

				calculateTimeToWait();
				if (timeToWait <= 0) {
					synchronized (lock) {
						try {
							// //LogCat.i(tag, "Waiting...");
							lock.wait();
						} catch (InterruptedException e) {
							synchronized (mLock) {
								e.printStackTrace();
							}
						}
					}
				} else {
					synchronized (lock) {
						try {
							// LogCat.i(tag, "Waiting " + timeToWait
							// + " milliseconds ...");
							lock.wait(timeToWait);
						} catch (InterruptedException e) {
							synchronized (mLock) {
								e.printStackTrace();
							}
						}
					}
				}

			}

			// LogCat.i(tag, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   I'm dead");

			super.run();
		}

		/**
		 * Check all the AsynTask and get the shortest time to wait
		 */
		private void calculateTimeToWait() {
			if (queueAsyncTasks.size() == 0) {
				timeToWait = -1;
			}
			for (int i = 0; i < queueAsyncTasks.size(); i++) {
				TimerTask task = (TimerTask) queueAsyncTasks.elementAt(i);
				long last = task.getLastExecutionTime();
				long timer = task.getTimer();
				// if (last == 0) {
				// timeToWait = -1;
				// return;
				// }
				long executeTime = last + timer - System.currentTimeMillis();

				if (timeToWait <= 0 || executeTime < timeToWait) {
					timeToWait = executeTime;
				}

			}
			// ////LogCat.i(tag, "Time to wait=" + getDate(timeToWait));
		}

		/**
		 * Process the task queue (timers) for the defined threads
		 * 
		 * @return if one or more task has been executed, false otherwise
		 */
		private boolean executeAsyncTasks() {
			boolean result = false;
			for (int i = 0; i < queueAsyncTasks.size(); i++) {
				TimerTask task = (TimerTask) queueAsyncTasks.elementAt(i);
				/* check if this task is on time */
				long lastExec = (System.currentTimeMillis() - task.getLastExecutionTime());
				boolean isTime = (lastExec >= task.getTimer()) || task.getLastExecutionTime() == 0;
				if (isTime) {
					// LogCat.i(tag, " IS TIMEEEEEEEEEEEEE: lastExec=" +
					// lastExec
					// + " task.getTimer()=" + task.getTimer()
					// + " Task id=" + task.getIdTask());
				}
				if (task.isKillable()) {
					task.onKillTask(new TaskResult(task.getIdTask(), false, TaskResult.TASK_MESSAGE_REMOVED,
							"Task removed! Reasons: the flag killable has been activated", null));
					queueAsyncTasks.removeElement(task);
				} else if (checkTaskBefoerExecute(task) && isTime
						&& ((isBackground && task.backGroundRunnable()) || !isBackground)) {

					ThreadFromPool freeThread = pool.getFreeThread();
					if (freeThread != null) {
						// LogCat.i(tag, "Running AsincTask. id=" +
						// task.getIdTask());
						if (!freeThread.addTask(task)) {
							i--;
						}
					} else {
						// LogCat.i(tag,
						// "(AsyncTasks)No Threads available, waiting...   id="
						// + task.getIdTask());
						break;

					}
					result = true;
				}
			}
			return result;

		}

		/**
		 * Process the task queue (FIFO) for the defined threads
		 * 
		 * @return if one or more task has been executed, false otherwise
		 */
		private boolean executeSyncTasks() {
			boolean result = false;
			for (int i = 0; i < queueSyncTasks.size(); i++) {
				if (!isBackground) {
					Task task = (Task) queueSyncTasks.elementAt(i);

					if (checkTaskBefoerExecute(task)) {

						ThreadFromPool freeThread = pool.getFreeThread();
						if (freeThread != null) {
							if (freeThread.addTask(task)) {
								queueSyncTasks.removeElement(task);
								// LogCat.i(tag,
								// "# Running task. id=" + task.getIdTask());
							}
							i--;

						} else {
							// LogCat.i(tag,
							// "$ (SyncTasks)No Threads available, waiting...   id="
							// + task.getIdTask());
							break;

						}
						result = true;
					}
				}
			}
			return result;

		}

		/**
		 * Check if a task should be executed
		 * 
		 * @param task
		 * @return
		 */
		private boolean checkTaskBefoerExecute(Task task) {
			if (task.isRunning()) {
				return false;
			}
			if (!task.isWaitingUntilOtherTaskFinishes()) {
				return true;
			}
			TaskResult result = searchHistoryTask(task.getTaskIdToWait());
			if (result != null) {
				return true;
			}

			return false;
		}

		public void onFinishTask(TaskResult result, Task task, ThreadFromPool thread) {

			if (result.msg() == TaskResult.TASK_MESSAGE_WAIT_OTHER_TASK_TO_FINISH
					&& task.getTaskType() != Task.TASK_TYPE_TIMER) {
				addTask(task);
			} else if (result.saveToHistory()) {
				taskHistory.addElement(result);
			}

			// LogCat.i(tag, "The Task (id=" + id +
			// ") has finished. Error code ="
			// + result.error());

			processTasks();
		}

	}

}
