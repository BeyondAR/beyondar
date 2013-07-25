package com.beyondar.android.util.tasks;

public interface IOnFinishTaskListener {

	/**
	 * Use this method to define what to do when a task finish the work
	 * 
	 * @param result
	 *            The result of the task
	 * @param task
	 *            The finalized task
	 * @param thread
	 *            The {@link ThreadFromPool} instance that has executed the task
	 */
	public void onFinishTask(TaskResult result, Task task, ThreadFromPool thread);

}
