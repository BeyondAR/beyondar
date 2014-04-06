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
package com.beyondar.android.opengl.colision;

import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;

/**
 * A mesh collider is a container for 3D objects used for collision tests. mesh colliders
 * are usually less complicated than the actual 3D object. They are usually
 * cubes or spheres to simplify calculations.
 * 
 */
public interface MeshCollider {

	/**
	 * Returns <b>true</b> if the given point is contained by the mesh collider.
	 * <b>false</b> otherwise
	 * 
	 * @param p
	 *            the point
	 * @return <b>true</b> if the given point is contained by the armature.
	 *         <b>false</b> otherwise
	 */
	public boolean contains(Point3 p);

	/**
	 * Test whether a ray intersects with the mesh collider. If it does, returns the
	 * intersection point. If it doesn't, returns <b>null</b>
	 * 
	 * @param r
	 *            the ray
	 * @return the intersection point. <b>null</b> if there is no intersection
	 */
	public Point3 getIntersectionPoint(Ray r);

	/**
	 * Return if the given ray intersects with the mesh collider
	 * 
	 * @param r
	 *            the ray
	 * @return <b>true</b> if there is intersection. <b>false</b> otherwise
	 */
	public boolean intersects(Ray r);

}
