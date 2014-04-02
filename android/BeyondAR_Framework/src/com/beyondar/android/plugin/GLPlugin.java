package com.beyondar.android.plugin;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.beyondar.android.opengl.renderer.ARRenderer;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;

/**
 * Basic interface to create a plugin to extend the OpenGL layer.
 */
public interface GLPlugin extends Plugin {

	/**
	 * This method is invoked when the plugin is removed.
	 */
	public void onDetached();

	/**
	 * Check if the plugin is attached.
	 * 
	 * @return
	 */
	public boolean isAttached();

	/**
	 * Setup the plugin according to the world. This method is also call if a
	 * new world is set.
	 * 
	 * @param world
	 *            The {@link World} information
	 * 
	 */
	public void setup(World world, ARRenderer renderer);

	/**
	 * Called when the camera position has been changed.
	 * 
	 * @param newCameraPos
	 *            new camera position.
	 */
	public void onCameraPositionChanged(Point3 newCameraPos);

	/**
	 * Called before a {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject} is rendered.
	 * 
	 * @param gl
	 * @param beyondarObject
	 *            the {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject} to be rendered.
	 * @param defaultTexture
	 *            The default texture to draw if the
	 *            {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject}'s
	 *            {@link com.beyondar.android.opengl.texture.Texture Texture} is
	 *            not loaded.
	 */
	public void onDrawBeyondaarObject(GL10 gl, BeyondarObject beyondarObject, Texture defaultTexture);

	/**
	 * Called when a frame has been rendered.
	 * 
	 * @param gl
	 */
	public void onFrameRendered(GL10 gl);

	public void onMaxDistanceSizeChanged(float newMaxDistance);

	public void onMinDistanceSizeChanged(float newMinDistance);

	public void onSurfaceChanged(GL10 gl, int width, int height);

	public void onSurfaceCreated(GL10 gl, EGLConfig config);

	/**
	 * This method is called to pre-load any additional textures that may be
	 * used in the plugin.
	 * 
	 * @param gl
	 */
	public void loadAdditionalTextures(GL10 gl);

	/**
	 * Called when the activity has been paused.
	 */
	public void onPause();

	/**
	 * Called when the activity has been resumed.
	 */
	public void onResume();

}
