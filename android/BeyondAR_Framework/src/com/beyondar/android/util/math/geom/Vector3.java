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
 * Represents a 3 dimensions vector
 * 
 */
public class Vector3 extends Point3 {

	/**
	 * Vector module
	 */
	protected float module;

	// Next fields are used to avoid module recalculation every time the method
	// is called. They store the coordinates values that was used to calculate
	// the module

	private float xModule;

	private float yModule;

	private float zModule;

	/**
	 * Constructs a vector from its 3 coordinates
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param z
	 *            z coordinate
	 */
	public Vector3(float x, float y, float z) {
		super(x, y, z);
		updateModule();
	}

	/**
	 * Constructs a vector from its 3 coordinates
	 */
	public Vector3() {
		super(0, 0, 0);
		updateModule();
	}

	/**
	 * Constructs a vector that points from p1 to p2
	 * 
	 * @param p1
	 *            Starting point for the vector
	 * @param p2
	 *            Finish point for the vector
	 */
	public Vector3(Point3 p1, Point3 p2) {
		super(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
	}

	/**
	 * Constructs a vector from another vector. It just clones its coordinates
	 * 
	 * @param v
	 */
	public Vector3(Point3 v) {
		super(v);
	}

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		updateModule();
	}

	/**
	 * Returns the dot product with the given vector
	 * 
	 * @param v
	 *            the vector
	 * @return the dot product
	 */
	public float dotProduct(Vector3 v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * Returns vector module
	 * 
	 * @return vector module
	 */
	public float module() {
		if (!(x == xModule && y == yModule && z == zModule)) {
			updateModule();
		}
		return module;
	}

	/**
	 * Returns module's square, avoiding do the square root
	 * 
	 * @return
	 */
	public float module2() {
		return x * x + y * y + z * z;
	}

	/**
	 * Returns the formed angle with the given vector
	 * 
	 * @param v
	 *            the vector
	 * @return the tiniest angle between the two vectors at the plane formed by
	 *         both
	 */
	public float angle(Vector3 v) {
		float cosAngle = dotProduct(v) / (module() * v.module());
		return (float) Math.acos(cosAngle);
	}

	/**
	 * Returns the cross product with the given vector
	 * 
	 * @param v
	 *            the vector
	 * @return the vector result from the cross product
	 */
	public Vector3 crossProduct(Vector3 v) {
		float newX = y * v.z - z * v.y;
		float newY = z * v.x - x * v.z;
		float newZ = x * v.y - y * v.x;

		return new Vector3(newX, newY, newZ);
	}

	/**
	 * Returns the normal vector to the plane formed by the 3 given points
	 * 
	 * @param p1
	 *            point 1
	 * @param p2
	 *            point 2
	 * @param p3
	 *            point 3
	 * @return the normal vector to the plane formed by the 3 given points
	 */
	public static Vector3 normalVector(Point3 p1, Point3 p2, Point3 p3) {
		Vector3 v1 = new Vector3(p1, p2);
		Vector3 v2 = new Vector3(p1, p3);
		return v1.crossProduct(v2);
	}

	/**
	 * Normalize the vector
	 */
	public void normalize() {
		float module = this.module();
		this.x /= module;
		this.y /= module;
		this.z /= module;
		updateModule();
	}

	/**
	 * Returns the index from the vector's greatest absolute coordinate
	 * 
	 * @return the index from the vector's greatest absolute coordinate. 0 for
	 *         the x, 1 for the y, 2 for the z
	 */
	public int getGreatestComponent() {
		int c = 0;
		if (Math.abs(y) > Math.abs(x) && Math.abs(y) > Math.abs(z))
			c = 1;
		else if (Math.abs(z) > Math.abs(y) && Math.abs(z) > Math.abs(x))
			c = 2;
		return c;
	}

	/**
	 * Adds a vector and then, if normalize is <b>true</b> normalize the vector
	 * 
	 * @param v
	 *            the vector to be added
	 * @param normalize
	 *            if the vector must be normalized after the addition
	 */
	public void add(Vector3 v, boolean normalize) {
		add(v);
		if (normalize)
			normalize();

	}

	/**
	 * Scales the vector
	 * 
	 * @param k
	 *            scale factor
	 */
	public void scale(float k) {
		x *= k;
		y *= k;
		z *= k;
		updateModule();
	}

	/**
	 * Premultiply the vector with a matrix
	 * 
	 * @param m
	 *            the matrix
	 */
	public void preMultiply(float[] m) {
		int i = 0;
		float newX = x * m[i++] + y * m[i++] + z * m[i++];
		float newY = x * m[i++] + y * m[i++] + z * m[i++];
		float newZ = x * m[i++] + y * m[i++] + z * m[i++];
		set(newX, newY, newZ);
	}

	/**
	 * Rotate the vector around x-axis
	 * 
	 * @param rotX
	 *            rotation in radians
	 */
	public void rotateX(float rotX) {
		float rX[] = new float[] { 1, 0, 0, 0, (float) Math.cos(rotX),
				-(float) Math.sin(rotX), 0, (float) Math.sin(rotX),
				(float) Math.cos(rotX) };
		preMultiply(rX);
	}

	/**
	 * Rotate the vector around y-axis
	 * 
	 * @param angle
	 *            rotation in radians
	 */
	public void rotateY(float angle) {
		float ry[] = { (float) Math.cos(angle), 0, (float) Math.sin(angle), 0, 1, 0,
				-(float) Math.sin(angle), 0, (float) Math.cos(angle) };
		preMultiply(ry);

	}

	/**
	 * Rotate the vector around z-axis
	 * 
	 * @param rotZ
	 *            rotation in radians
	 */
	public void rotateZ(float rotZ) {
		float rZ[] = new float[] { (float) Math.cos(rotZ), -(float) Math.sin(rotZ),
				0, (float) Math.sin(rotZ), (float) Math.cos(rotZ), 0, 0, 0, 1 };
		preMultiply(rZ);
	}

	private static Vector3 volatileVector = new Vector3(0, 0, 0);

	/**
	 * Returns a volatile vector pointing from p1 to p2. We use a the volatile
	 * vector when we want to do calculations with vectors, but it's not
	 * necessary to keep the instance
	 */
	public static Vector3 getVolatileVector(Point3 p1, Point3 p2) {
		volatileVector.x = p2.x - p1.x;
		volatileVector.y = p2.y - p1.y;
		volatileVector.z = p2.z - p1.z;
		volatileVector.updateModule();
		return volatileVector;
	}

	public static Vector3 getVolatileVector(float x1, float y1, float z1,
			float x2, float y2, float z2) {
		volatileVector.x = x2 - x1;
		volatileVector.y = y2 - y1;
		volatileVector.z = z2 - z1;
		volatileVector.updateModule();
		return volatileVector;
	}

	private void updateModule() {
		xModule = x;
		yModule = y;
		zModule = z;
		module = (float) Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Creates a vector from point p1 to point p2
	 * 
	 * @param p1
	 *            start point
	 * @param p2
	 *            end point
	 */
	public void set(Point3 p1, Point3 p2) {
		x = p2.x - p1.x;
		y = p2.y - p1.y;
		z = p2.z - p1.z;
		updateModule();

	}

	/**
	 * Creates the vector form point defined by (x1, y1, z1) to point defined by
	 * (x2, y2, z2)
	 * 
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	public void set(float x1, float y1, float z1, float x2, float y2, float z2) {
		x = x2 - x1;
		y = y2 - y1;
		z = z2 - z1;
		updateModule();
	}

	public void subtract(Vector3 u) {
		x -= u.x;
		y -= u.y;
		z -= u.z;
	}

	public void copy(Vector3 out) {
		super.copy(out);
		out.xModule = xModule;
		out.yModule = yModule;
		out.zModule = zModule;
	}

}
