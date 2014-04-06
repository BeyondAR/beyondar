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
package com.beyondar.android.util.math.geom;

/**
 * Represents a geometric ray, compound of a {@link Point3} and a
 * {@link Vector3}
 * 
 */
public class Ray {

	private Point3 point;
	private Vector3 vector;

	/**
	 * Constructs a ray from a point and a vector
	 * 
	 * @param p
	 *            the point
	 * @param v
	 *            the vector
	 */
	public Ray(Point3 p, Vector3 v) {
		this.point = p;
		this.vector = v;
	}

	/**
	 * Constructs a ray from a point and a vector. The point is defined as
	 * (0,0,0)
	 * 
	 * @param v
	 *            the vector
	 */
	public Ray(Vector3 v) {
		this.point = new Point3(0, 0, 0);
		this.vector = v;

	}

	/**
	 * Constructs a ray from a point and a vector. The point is defined as
	 * (0,0,0)
	 * 
	 * @param vec_x
	 *            the vector x value
     * @param vec_y
     *            the vector y value
     * @param vec_z
     *            the vector z value
	 */
	public Ray(float vec_x, float vec_y, float vec_z) {
		this.point = new Point3(0, 0, 0);
		this.vector = new Vector3(vec_x, vec_y, vec_z);

	}

	/**
	 * Returns the point in the ray that corresponds to the given t parameter
	 * 
	 * @param t
	 *            t parameter
	 * @return the corresponding point
	 */
	public Point3 getPoint(float t) {
		Point3 p = new Point3(t * vector.x, t * vector.y, t * vector.z);
		p.add(point);
		return p;
	}

	/**
	 * Returns the starting point for this ray
	 * 
	 * @return the starting point for this ray
	 */
	public Point3 getPoint() {
		return point;
	}

	/**
	 * Returns the vector defining the ray
	 * 
	 * @return the vector defining the ray
	 */
	public Vector3 getVector() {
		return vector;
	}

	public void setVector(float x, float y, float z) {
		this.vector.set(x, y, z);
	}

	public Ray clone() {
		return new Ray(point, vector);
	}
	
	public void copy(Ray ray){
		point.copy(ray.point);
		
		ray.vector.x = vector.x;
		ray.vector.x = vector.x;
		ray.vector.x = vector.x;
	}

	private static Ray r = new Ray(new Point3(0, 0, 0), new Vector3(0, 0, 0));

	public static Ray getVolatileRay(Point3 p, Vector3 v) {
		r.point = p;
		r.vector = v;
		return r;
	}

}
