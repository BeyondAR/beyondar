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

import com.beyondar.android.util.math.geom.Plane;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.util.math.geom.Triangle;

/**
 * A square mesh collider. It's created from four points.
 * 
 */
public class SquareMeshCollider implements MeshCollider {

	private Triangle t1, t2;

	private Plane p;

	/**
	 * Constructs a square mesh collider from 4 points. This 4 points must be
	 * contained for the same plane. If not, weird behavior will happen
	 * 
	 * @param topLeft
	 * @param bottomLeft
	 * @param bottomRight
	 * @param topRight
	 */
	public SquareMeshCollider(Point3 topLeft, Point3 bottomLeft, Point3 bottomRight, Point3 topRight) {
		t1 = new Triangle(topLeft, bottomLeft, bottomRight);
		t2 = new Triangle(topLeft, topRight, bottomRight);
		p = t1.getPlane();
	}

	@Override
	public boolean contains(Point3 p) {
		return t1.contains(p) || t2.contains(p);
	}

	@Override
	public Point3 getIntersectionPoint(Ray r) {
		float t = p.intersects(r);
		if (t >= 0) {
			Point3 p = r.getPoint(t);
			if (this.contains(p)) {
				return p;
			} else
				return null;
		} else
			return null;
	}

	@Override
	public boolean intersects(Ray r) {
		return getIntersectionPoint(r) != null;
	}

}
