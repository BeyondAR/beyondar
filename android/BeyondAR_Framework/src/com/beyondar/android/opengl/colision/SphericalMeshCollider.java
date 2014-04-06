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
import com.beyondar.android.util.math.geom.Vector3;

/**
 * A spherical mesh collider. It's created from a point (sphere's center) and a
 * radius (sphere's radius)
 * 
 */
public class SphericalMeshCollider implements MeshCollider {

	private Point3 center;
	private float radius;

	/**
	 * Constructs a spherical collision detector from its center and its radius
	 * 
	 * @param center
	 *            Center point
	 * @param radius
	 *            Sphere radius
	 */
	public SphericalMeshCollider(Point3 center, float radius) {
		this.center = center;
		this.radius = radius;
	}

	@Override
	public boolean contains(Point3 p) {
		if (p != null) {
			float distance = Vector3.getVolatileVector(p, center).module();
			return distance <= radius;
		}
		return false;
	}

	@Override
	public Point3 getIntersectionPoint(Ray r) {
		float t = Plane.getVolatilePlane(center, r.getVector()).intersects(r);
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
		Point3 p = this.getIntersectionPoint(r);
		return this.contains(p);

	}

	public float getRadius() {
		return radius;
	}

}
