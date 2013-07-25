package com.beyondar.android.util.math.geom;

/**
 * Represents a geometric ray, compound of a {@link Point3} and a
 * {@link Vector3}
 * 
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
	 * @param v
	 *            the vector
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
