package com.beyondar.android.util.pool;

public class FloatArrayPool extends BaseObjectPool<float[]> {

	private int mArraySize;

	public FloatArrayPool(int size) {
		mArraySize = size;
	}

	@Override
	protected float[] createNewObject() {
		return new float[mArraySize];
	}

//	@Override
//	public synchronized float[] borrowObject() {
//		float[] result = super.borrowObject();
//		
//		for(int i =0; i < mArraySize; i++){
//			result[i] = 0;
//		}
//		
//		return result;
//	}
}
