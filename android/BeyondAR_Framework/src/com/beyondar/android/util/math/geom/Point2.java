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
 * Represents a 2D point
 * 
 * 
 */
public class Point2 {

	/**
	 * x coordinate
	 */
	public float x;

	/**
	 * y coordinate
	 */
	public float y;

	/**
	 * Constructs a point from its 2D coordinates
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 */
	public Point2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Constructs a point from its 2D coordinates
	 * 
	 */
	public Point2() {
	}

	/**
	 * Constructs a 2D point from a 3D point, giving one coordinate to be
	 * removed from the 3D point
	 * 
	 * @param p
	 *            3D point
	 * @param remove
	 *            coordinate index to be removed. If 0, x will be removed,
	 *            assigning 2D x to 3D y and 2D y to 3D z. If 1, y will be
	 *            removed, and so on... If <em>remove</em> if greater than 2 or
	 *            less than 0, the z coordinate will be removed
	 */
	public Point2(Point3 p, int remove) {
		switch (remove) {
		case 0:
			x = p.y;
			y = p.z;
			break;
		case 1:
			x = p.x;
			y = p.z;
			break;
		default:
			x = p.x;
			y = p.y;
		}
	}

	/**
	 * Adds given x and y to point x and way
	 * 
	 * @param x
	 * @param y
	 */
	public void add(float x, float y) {
		this.x += x;
		this.y += y;
	}

	/**
	 * Adds a point to this one, adding given point x and y coordinates to this
	 * x and y point
	 * 
	 * @param p
	 *            the point
	 */
	public void add(Point2 p) {
		this.add(p.x, p.y);
	}

	/**
	 * Returns an array with the point's coordinates
	 * 
	 * @return an array with the point's coordinates
	 */
	public float[] getCoordinatesArray() {
		return new float[] { x, y };
	}

}
