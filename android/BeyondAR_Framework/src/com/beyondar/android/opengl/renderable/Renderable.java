/*
 * Copyright (C) 2013 BeyondAR
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
	 * @return True to force to paint the object, false otherwise. If false, the
	 *         {@link ARRenderer} will draw it if it close enough to the camera
	 */
	public boolean update(long time, double distance,
			BeyondarObject beyondarObject);

	/**
	 * This method is called when the renderable is not painted because is too
	 * far
	 */
	public void onNotRendered(double dst);

	public Texture getTexture();

	public Plane getPlane();

	public void setPosition(float x, float y, float z);

	public Point3 getPosition();

	public void setAngle(float x, float y, float z);

	public Point3 getAngle();

	public long getTimeFlag();

	// public void setGeoObject(GeoObject object);

}
