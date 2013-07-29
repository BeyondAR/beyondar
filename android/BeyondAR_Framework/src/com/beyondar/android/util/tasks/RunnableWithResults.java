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
