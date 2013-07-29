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
package com.beyondar.android.util;

import java.util.LinkedList;

public abstract class SimpleObjectPool<T> {

	private LinkedList<T> mFreeObjects;
	// private ArrayList<T> mBorrowedObjects;

	private Object mLockObject;

	public SimpleObjectPool() {
		mFreeObjects = new LinkedList<T>();
		// mBorrowedObjects = new ArrayList<T>();
		mLockObject = new Object();
	}

	public synchronized T borowObject() {
		synchronized (mLockObject) {
			T object;

			if (mFreeObjects.size() > 0) {
				object = mFreeObjects.removeFirst();
			} else {
				object = createNewObject();
			}

			// mBorrowedObjects.add(object);
			return object;
		}
	}

	public synchronized void returnObject(T object) {
		synchronized (mLockObject) {
			// mBorrowedObjects.remove(object);
			mFreeObjects.push(object);
		}
	}

	public int size() {
		return mFreeObjects.size();
	}

	protected abstract T createNewObject();
}
