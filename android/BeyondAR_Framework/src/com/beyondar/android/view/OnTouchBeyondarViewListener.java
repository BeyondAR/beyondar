package com.beyondar.android.view;

import android.view.MotionEvent;

/**
 * On touch listener to detect when a
 * {@link com.beyondar.android.world.BeyondarObject BeyondarObject} has been
 * touched on the {@link com.beyondar.android.view.BeyondarGLSurfaceView
 * BeyondarGLSurfaceView}.
 */
public interface OnTouchBeyondarViewListener {

	/**
	 * Use
	 * {@link BeyondarGLSurfaceView#getBeyondarObjectsOnScreenCoordinates(float, float, java.util.ArrayList)}
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
	 * @param beyondarView
	 */
	public void onTouchBeyondarView(MotionEvent event, BeyondarGLSurfaceView beyondarView);

}