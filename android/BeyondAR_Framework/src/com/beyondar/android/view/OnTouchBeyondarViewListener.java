package com.beyondar.android.view;

import android.view.MotionEvent;

public interface OnTouchBeyondarViewListener {

	static final String __ON_AR_TOUCH_METHOD_NAME__ = "onTouchBeyondarView";

	/**
	 * Use
	 * {@link BeyondarGLSurfaceView#getARObjectOnScreenCoordinates(float, float)}
	 * to get the object touched:<br>
	 * 
	 * <pre>
	 * {@code
	 * float x = event.getX();
	 * float y = event.getY();
	 * ArrayList<BeyondarObject> geoObjects = new ArrayList<BeyondarObject>();
	 * beyondarView.getARObjectOnScreenCoordinates(x, y, geoObjects);
	 * ...
	 * Now we iterate the ArrayList. The first element will be the closest one to the user
	 * ...
	 * }
	 * </pre>
	 * 
	 * @param event
	 * @param BeyondarView
	 */
	public void onTouchBeyondarView(MotionEvent event, BeyondarGLSurfaceView beyondarView);

}