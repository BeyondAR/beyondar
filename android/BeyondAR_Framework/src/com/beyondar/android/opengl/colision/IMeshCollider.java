package com.beyondar.android.opengl.colision;

import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;

/**
 * An armature is a container for 3D objects. It will be used for collision
 * tests. Armature are usually less complicated than the actual 3D object. They
 * are usually cubes or spheres, to simplify calculations.
 * 
 */
public interface IMeshCollider {

	/**
	 * Returns <b>true</b> if the given point is contained by the armature.
	 * <b>false</b> otherwise
	 * 
	 * @param p
	 *            the point
	 * @return <b>true</b> if the given point is contained by the armature.
	 *         <b>false</b> otherwise
	 */
	public boolean contains(Point3 p);

	/**
	 * Test whether a ray intersects with the armature. If it does, returns the
	 * intersection point. If it doesn't, returns <b>null</b>
	 * 
	 * @param r
	 *            the ray
	 * @return the intersection point. <b>null</b> if there is no intersection
	 */
	public Point3 getIntersectionPoint(Ray r);

	/**
	 * Return if the given ray intersects with the armature
	 * 
	 * @param r
	 *            the ray
	 * @return <b>true</b> if there is intersection. <b>false</b> otherwise
	 */
	public boolean intersects(Ray r);

}
