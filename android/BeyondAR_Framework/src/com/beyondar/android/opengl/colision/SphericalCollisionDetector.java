package com.beyondar.android.opengl.colision;

import com.beyondar.android.util.math.geom.Plane;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.util.math.geom.Vector3;

/**
 * A Spherical Armature. It's created from a point (sphere's center) and a
 * radius (sphere's radius)
 * 
 * 
 */
public class SphericalCollisionDetector implements IMeshCollider {

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
	public SphericalCollisionDetector(Point3 center, float radius) {
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
