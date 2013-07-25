package com.beyondar.android.util.tasks;

/**
 * 
 * @author Joan Puig Sanz (jpuigsanz@gmail.com)
 * 
 *         This is a runable for synchronized data. Use setResult() to store the result of the process and getResult() to get it.<br>
 *         NOTE: Thi class is not used inside the TaskExecutor
 * 
 */

public abstract class RunnableWithResults implements Runnable {

	private Object _result;
	
	protected void setResult(Object result){
		_result = result;
	}
	
	public Object getResult(){
		return _result;
	}
}
