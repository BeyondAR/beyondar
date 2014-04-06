/*
 * Copyright (C) 2014 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.beyondar.android.opengl.renderable;

import javax.microedition.khronos.opengles.GL10;

import com.beyondar.android.opengl.renderer.ARRenderer;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.util.math.geom.Plane;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.world.BeyondarObject;

/**
 * Interface that provides the needed methods that are called from the
 * {@link com.beyondar.android.opengl.renderer.ARRenderer ARRenderer}.
 * 
 */
public interface Renderable {

	/** The draw method to be used by OpenGL */
	public void draw(GL10 gl, Texture defaultTexture);

	/**
	 * Update the renderer before the draw method is called.
	 * 
	 * @param time
	 *            The time mark.
	 * @param distance
	 *            The distance form the camera in meters.
	 * @param beyondarObject
	 *            The {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject} represented by the Renderable.
	 * @return True to force to paint the object, false otherwise. If false, the
	 *         {@link ARRenderer} will draw it if it close enough to the camera
	 */
	public boolean update(long time, double distance, BeyondarObject beyondarObject);

	/**
	 * This method is called when the {@link com.beyondar.android.opengl.renderable.Renderable Renderable} is not rendered, for
	 * example because is too far
	 */
	public void onNotRendered(double dst);

	/**
	 * Get the texture object for the {@link com.beyondar.android.opengl.renderable.Renderable Renderable}
	 * 
	 * @return
	 */
	public Texture getTexture();

	/**
	 * Get the plane that represents the {@link com.beyondar.android.opengl.renderable.Renderable Renderable}. Used for collision
	 * detection.
	 * 
	 * @return
	 */
	public Plane getPlane();

	/**
	 * Set the position where the {@link com.beyondar.android.opengl.renderable.Renderable Renderable} needs to be rendered.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setPosition(float x, float y, float z);

	/**
	 * Get the position where the {@link com.beyondar.android.opengl.renderable.Renderable Renderable} will be rendered.
	 * 
	 * @return
	 */
	public Point3 getPosition();

	/**
	 * Set the angle of the {@link com.beyondar.android.opengl.renderable.Renderable Renderable}.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setAngle(float x, float y, float z);

	/**
	 * Get the angle of the {@link com.beyondar.android.opengl.renderable.Renderable Renderable}.
	 * 
	 * @return
	 */
	public Point3 getAngle();

	/**
	 * Get the time mark.
	 * 
	 * @return
	 */
	public long getTimeMark();

}
