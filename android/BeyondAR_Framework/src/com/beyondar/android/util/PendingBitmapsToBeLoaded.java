/*
 * Copyright (C) 2014 BeyondAR
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

import java.util.ArrayList;
import java.util.HashMap;

public class PendingBitmapsToBeLoaded<E> {
	private Object mLock;

	private HashMap<String, ArrayList<E>> mHolder;

	public PendingBitmapsToBeLoaded() {
		mHolder = new HashMap<String, ArrayList<E>>();
		mLock = new Object();
	}

	public synchronized void addObject(String uri, E object) {
		if (uri == null || object == null) {
			return;
		}
		synchronized (mLock) {
			ArrayList<E> list = mHolder.get(uri);
			if (list == null) {
				list = new ArrayList<E>();
				mHolder.put(uri, list);
			}
			list.add(object);
		}
	}

	public boolean isAnyPendingBitmap(String uri) {
		return getPendingList(uri) != null;
	}

	public boolean removePendingList(String uri) {
		synchronized (mLock) {
			ArrayList<E> list = mHolder.get(uri);
			if (list != null) {
				list.clear();
				mHolder.remove(uri);
				return true;
			}
		}
		return false;
	}

	public ArrayList<E> getPendingList(String uri) {
		return mHolder.get(uri);
	}

	public boolean existPendingList(String uri) {
		return getPendingList(uri) != null;
	}

	public void clear() {
		mHolder.clear();
	}
}
