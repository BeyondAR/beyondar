package com.yasiralijaved.genradar.utils;
/**
 * 
 */


/**
 * @author Yasir.Ali
 *
 */
public class LowPassFilter {
	
	/*
	 * time smoothing constant for low-pass filter
	 * 0 ≤ α ≤ 1 ; a smaller value basically means more smoothing
	 * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
	 */
	static final float ALPHA = 0.05f;
	
	/**
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://developer.android.com/reference/android/hardware/Sensor.html#TYPE_ACCELEROMETER
	 */
	public static float[] filter( float[] input, float[] output ) {
		if ( output == null ) return input;

		for ( int i=0; i<input.length; i++ ) {
			output[i] = output[i] + ALPHA * (input[i] - output[i]);
		}
		return output;
	}
}
