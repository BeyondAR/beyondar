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

/**
 * This is the result generate by all the tasks.
 * 
 */
public class TaskResult {

	/** Unknown output */
	public static final int TASK_MESSAGE_UNKNOW = 25623;
	/** Everything is ok */
	public static final int TASK_MESSAGE_OK = TASK_MESSAGE_UNKNOW + 1;
	/** Use this message for testing */
	public static final int TASK_MESSAGE_TESTING = TASK_MESSAGE_UNKNOW + 2;
	// public static final int TASK_INTERRUPTED_PREPROCESSOR =
	// TASK_MESSAGE_UNKNOW+3;
	/** Error checking dependencies */
	public static final int TASK_MESSAGE_ERROR_CHEKING_DEPENDENCIES = TASK_MESSAGE_UNKNOW + 4;
	/** Error in the main task */
	public static final int TASK_MESSAGE_ERROR_MAIN_TASK = TASK_MESSAGE_UNKNOW + 5;
	/**
	 * Use this to specify that this task has to wait an other before being
	 * executed
	 */
	public static final int TASK_MESSAGE_ERROR_EXCEPTION = TASK_MESSAGE_UNKNOW + 6;

	/** Check the String message */
	public static final int TASK_MESSAGE_CHECK_STRING_MESSAGE = TASK_MESSAGE_UNKNOW + 100;
	/** Check the Object message */
	public static final int TASK_MESSAGE_CHECK_OBJECT_MESSAGE = TASK_MESSAGE_UNKNOW + 101;
	/** This task has been removed */
	public static final int TASK_MESSAGE_REMOVED = TASK_MESSAGE_UNKNOW + 102;
	/**
	 * Use this to specify that this task has to wait an other before being
	 * executed
	 */
	public static final int TASK_MESSAGE_WAIT_OTHER_TASK_TO_FINISH = TASK_MESSAGE_UNKNOW + 103;

	private final long _id;
	private final Object _result;
	private final String _strData;
	private final int _msg;
	private final boolean _error;
	private final boolean _saveToHistory;

	/**
	 * Generate the task result to handle it with the tasks. With this
	 * constructor, the flag to save the result in the history is activated. If
	 * you don't want to save this TaskResult inside the history, use the other
	 * constructor and set false to the history flag
	 * 
	 * @param id
	 *            The task id.
	 * @param error
	 *            true if there are any error. False otherwise
	 * @param msg
	 *            An int with a simple output message.
	 * @param strData
	 *            The String with information about this result
	 * @param result
	 *            An Object to attach to the result
	 */
	public TaskResult(long id, boolean error, int msg, String strData,
			Object result) {
		this(id, error, msg, strData, result, true);
	}

	/**
	 * Generate the task result to handle it with the tasks. With this
	 * constructor, the flag to save the result in the history is activated. If
	 * you don't want to save this TaskResult inside the history, use the other
	 * constructor and set false to the history flag
	 * 
	 * @param id
	 *            The task id.
	 * @param error
	 *            true if there are any error. False otherwise
	 * @param msg
	 *            An int with a simple output message.
	 */
	public TaskResult(long id, boolean error, int msg) {
		this(id, error, msg, null, null, true);
	}

	/**
	 * Generate a generic error task with an exception. The task will be stored
	 * in the history. The message value for this task is
	 * <code> TASK_MESSAGE_ERROR_EXCEPTION</code>
	 * 
	 * @param id
	 *            The task id.
	 * @param e
	 *            The exception to store.
	 */
	public TaskResult(long id, Exception e) {
		this(id, true, TASK_MESSAGE_ERROR_EXCEPTION, null, e, true);
	}

	/**
	 * Generate a generic error task with an exception. The task will be stored
	 * in the history. The message value for this task is
	 * <code> TASK_MESSAGE_ERROR_EXCEPTION</code>
	 * 
	 * @param id
	 *            The task id.
	 * @param strData
	 *            A personalized message.
	 * @param e
	 *            The exception to store.
	 */
	public TaskResult(long id, String strData, Exception e) {
		this(id, true, TASK_MESSAGE_ERROR_EXCEPTION, strData, e, true);
	}

	/**
	 * Generate the task result to handle it with the tasks
	 * 
	 * @param id
	 *            The task id.
	 * @param error
	 *            true if there are any error. False otherwise
	 * @param msg
	 *            An int with a simple output message.
	 * @param strData
	 *            The String with information about this result
	 * @param result
	 *            An Object to attach to the result
	 * @param saveToHistory
	 *            Use this flag to indicate if this TaskResult should be saved
	 *            in the history
	 */
	public TaskResult(long id, boolean error, int msg, String strData,
			Object result, boolean saveToHistory) {
		_error = error;
		_msg = msg;
		_strData = strData;
		_result = result;
		_id = id;
		_saveToHistory = saveToHistory;
	}

	/**
	 * Get if this task result should be saved in the history
	 * 
	 * @return true to save it, false otherwise
	 */
	public boolean saveToHistory() {
		return _saveToHistory;
	}

	/**
	 * Get the task id that has produced this {@link TaskResult}
	 */
	public long idTask() {
		return _id;
	}

	/**
	 * If there is an error or not.
	 * 
	 * @return true if there was an error, false otherwise
	 */
	public boolean error() {
		return _error;
	}

	/**
	 * Get the main message.
	 * 
	 * @return
	 */
	public int msg() {
		return _msg;
	}

	/**
	 * A personalized String message
	 * 
	 * @return
	 */
	public String stringMsg() {
		return _strData;
	}

	/**
	 * A personalized object message
	 * 
	 * @return
	 */
	public Object objectMsg() {
		return _result;
	}

}
