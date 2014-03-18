package com.beyondar.android.module;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.beyondar.android.opengl.renderer.ARRenderer;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.world.World;

public interface GLModule extends Module {

	/**
	 * This method is invoked when the module is removed.
	 */
	public void onDetached();

	/**
	 * Check if the module is attached.
	 * 
	 * @return
	 */
	public boolean isAttached();

	/**
	 * Setup the module according to the world. This method is also call if a
	 * new world is set.
	 * 
	 * @param world
	 *            The {@link World} information
	 * 
	 */
	public void setup(World world, ARRenderer renderer);
	
	public void onCameraPositionChanged(Point3 newCameraPos);
	
	public void onDrawFrame (GL10 gl);
	
	public void onMaxDistanceSizeChanged (float newMaxDistance);
	
	public void onMinDistanceSizeChanged (float newMinDistance);
	
	public void onSurfaceChanged(GL10 gl, int width, int height) ;
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config);
	
	public void loadAdditionalTextures (GL10 gl);

}
