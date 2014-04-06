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
 * Represents a geometric plane
 * 
 */
public class Plane {
	private Point3 point;
	private Vector3 normal;
	private float d;

	/**
	 * Constructs a plane from a point and its normal
	 * 
	 * @param p
	 *            Point contained by the plane
	 * @param n
	 *            Normal vector for the plane
	 */
	public Plane(Point3 p, Vector3 n) {
		this.point = p;
		this.normal = n;
		d = -(point.x * n.x + point.y * n.y + point.z * n.z);
	}

	/**
	 * Constructs a plane from three points
	 * 
	 * @param p1
	 *            Point 1
	 * @param p2
	 *            Point 2
	 * @param p3
	 *            Point 3
	 */
	public Plane(Point3 p1, Point3 p2, Point3 p3) {
		this(p1, Vector3.normalVector(p1, p2, p3));
	}

	/**
	 * Returns the plane's normal vector
	 * 
	 * @return the plane's normal vector
	 */
	public Vector3 getNormal() {
		return normal;
	}

	/**
	 * Returns the plane's point
	 * 
	 * @return the plane's point
	 */
	public Point3 getPoint() {
		return point;
	}

	/**
	 * Returns the t parameter for the intersection with the given ray. If plane
	 * and ray has no intersection returns <b>-1</b> representing infinite.
	 * Value returned must be used with {@link Ray#getPoint(float)}
	 * 
	 * @param ray
	 *            Ray to be checked
	 * @return the t parameter for the ray in the intersection point. If plane
	 *         and ray has no intersection returns <b>-1</b>
	 */
	public float intersects(Ray ray) {
		float dotProduct = ray.getVector().dotProduct(normal);
		if (Math.abs(dotProduct) > 0.01f) {
			Point3 p = ray.getPoint();
			float t = -(p.x * normal.x + p.y * normal.y + p.z * normal.z + d)
					/ dotProduct;
			return t;
		} else
			return -1;
	}

	private static Plane volatilePlane = new Plane(new Point3(0, 0, 0),
			new Vector3(0, 0, 0));

	/**
	 * Returns a volatile plane from a point and its normal
	 * 
	 * @param point
	 *            Point contained by the plane
	 * @param normal
	 *            Normal vector for the plane
	 */
	public static Plane getVolatilePlane(Point3 point, Vector3 normal) {
		volatilePlane.point = point;
		volatilePlane.normal = normal;
		volatilePlane.d = -(point.x * normal.x + point.y * normal.y + point.z
				* normal.z);
		return volatilePlane;
	}
}
