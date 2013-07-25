package com.beyondar.android.opengl.views;

import android.view.MotionEvent;

public interface IOnARTouchListener {

	/**
	 * Use
	 * {@link BeyondarGLSurfaceView#getARObjectOnScreenCoordinates(float, float)}
	 * to get the object touched
	 * 
	 * @param event
	 * @param BeyondarView
	 */
	public void onTouchARView(MotionEvent event, BeyondarGLSurfaceView beyondarView);

}
