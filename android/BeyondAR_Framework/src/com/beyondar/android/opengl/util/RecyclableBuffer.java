package com.beyondar.android.opengl.util;

import java.util.LinkedList;

import android.database.Observable;

public class RecyclableBuffer<T> extends Observable<T>{
	
	private LinkedList<T> list;

	
	public RecyclableBuffer() {
		list = new LinkedList<T>();
	}
	
	public void recycle(T t){
		list.add(t);
	}
	
	public T getLast(){
		return list.poll();
	}
	

}
