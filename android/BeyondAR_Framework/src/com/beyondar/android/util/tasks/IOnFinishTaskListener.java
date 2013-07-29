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
