/**
 * 
 */
package com.beyondar.android.util.tasks;

/**
 * @author Joan Puig Sanz (joanpuigsanz@gmail.com)
 * 
 */
public abstract class TimerTask extends Task {

	private long lastExecution;
	private long timer;
	private boolean kill;
	private boolean runInBackGround;

	/**
	 * @param id
	 *            The id of this task
	 * @param timer
	 *            The the sleeping time between for this task in milliseconds
	 */
	public TimerTask(long id, long timer) {
		super(id);

		lastExecution = 0;
		this.timer = timer;
		runInBackGround = false;
	}

	/**
	 * @param id
	 *            The id of this task
	 * @param timer
	 *            The the sleeping time between for this task in milliseconds
	 * @param runInBackground
	 *            Set true if you want to execute this task if the app is
	 *            running in background
	 */
	public TimerTask(long id, long timer, boolean runInBackground) {
		super(id);

		lastExecution = 0;
		this.timer = timer;
		this.runInBackGround = runInBackground;
	}

	public int getTaskType() {
		return TASK_TYPE_TIMER;
	}

	/**
	 * Get if this tasks can be executed in background
	 * 
	 * @return True if this task can run in background, false otherwise.
	 */
	public boolean backGroundRunnable() {
		return runInBackGround;
	}

	public TaskResult executeTask() {
		TaskResult out = super.executeTask();
		if (!isWaitingUntilOtherTaskFinishes()) {
			lastExecution = System.currentTimeMillis();
		}
		return out;
	}

	/**
	 * Get the sleeping time before execute
	 * 
	 * @return sleeping time in milliseconds
	 */
	public long getTimer() {
		return timer;
	}

	/**
	 * Get when was the last time that the task manager execute this task
	 * 
	 * @return last time in milliseconds
	 */
	public final long getLastExecutionTime() {
		return lastExecution;
	}

	/**
	 * Define if the task should not be executed anymore
	 * 
	 */
	public void killTask() {
		kill = true;
	}

	/**
	 * Get if the task should be killed by the {@link TaskExecutor}
	 * 
	 * @return true to kill it, false otherwise
	 */
	public boolean isKillable() {
		return kill;

	}

	public abstract TaskResult runTask();

}
