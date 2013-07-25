package com.beyondar.android.util.tasks;

/**
 * 
 * @author Joan Puig Sanz (joanpuigsanz@gmail.com)
 * 
 *         This interface allows the developer know when a thread stop
 */
public interface IOnThreadFromPoolStop {

	/**
	 * Use this method to know when a thread stops
	 * 
	 * @param thread
	 */
	public void onThreadStops(ThreadFromPool thread);

}
