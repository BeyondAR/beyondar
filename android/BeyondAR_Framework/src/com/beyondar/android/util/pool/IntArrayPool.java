package com.beyondar.android.util.pool;

public class IntArrayPool extends BaseObjectPool<int[]> {

	private int mArraySize;
	
	public IntArrayPool(int size){
		mArraySize = size;
	}
	
	@Override
	protected int[] createNewObject() {
		return new int[mArraySize];
	}
	
//	@Override
//	public synchronized int[] borrowObject() {
//		int[] result = super.borrowObject();
//		
//		for(int i =0; i < mArraySize; i++){
//			result[i] = 0;
//		}
//		
//		return result;
//	}

}
