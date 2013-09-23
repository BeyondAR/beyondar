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
}
