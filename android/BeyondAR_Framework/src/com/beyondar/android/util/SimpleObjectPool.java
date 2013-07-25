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
