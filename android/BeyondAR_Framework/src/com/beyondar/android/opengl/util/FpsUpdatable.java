package com.beyondar.android.opengl.util;

public interface FpsUpdatable {
	/**
	 * This method will get the frames per second rendered by the
	 * {@link ARRenderer}
	 * 
	 * @param fps
	 *            The Frames per second rendered
	 */
	public void onFpsUpdate(float fps);
}
