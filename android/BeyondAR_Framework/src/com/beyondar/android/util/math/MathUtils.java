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
package com.beyondar.android.util.math;

import com.beyondar.android.util.math.geom.Point3;

/**
 * Util class for math operations.
 */
public class MathUtils {

	// TODO: Improve calculate the angle for Y
	public static void calcAngleFaceToCamera(Point3 p1, Point3 p2, Point3 out) {

		float x = (float) Math.toDegrees(Math.atan2(p1.y - p2.y, p1.z - p2.z));
		// float y = (float) Math.toDegrees(Math.atan2(p1.x - p2.x, p1.z -
		// p2.z));
		float z = (float) Math.toDegrees(Math.atan2(p1.y - p2.y, p1.x - p2.x));

		x = (x + 270) % 360;
		// y = (x + 270) % 360;
		z = (z + 270) % 360;

		out.z = z;
	}

	/**
	 * Convert the meter units to the equivalent distance in the GL world
	 * 
	 * @param meters
	 * @return
	 */
	public static float metersToGlUnits(float meters) {
		return meters / 2f;
	}

	public static float GLUnitsToMeters(float glUnits) {
		return glUnits * 2f;
	}

	/**
	 * Calculate the point between two points according to a given distance. <br>
	 * Source: http://tech-algorithm.com/articles/linear-interpolation/
	 * 
	 * @param x_start
	 * @param y_start
	 * @param z_start
	 * @param x_end
	 * @param y_end
	 * @param z_end
	 * @param newDistance
	 * @param totalDistanceToMove
	 * @param dstPoint
	 */
	public static void linearInterpolate(float x_start, float y_start, float z_start, float x_end,
			float y_end, float z_end, float newDistance, float totalDistanceToMove, Point3 dstPoint) {

		float x = (x_start + newDistance * (x_end - x_start) / (float) totalDistanceToMove);
		float y = (y_start + newDistance * (y_end - y_start) / (float) totalDistanceToMove);
		float z = (z_start + newDistance * (z_end) / (float) totalDistanceToMove);

		dstPoint.x = x;
		dstPoint.y = y;
		dstPoint.z = z;
	}

	/**
	 * Calculate the point between two points according to a given distance. <br>
	 * Source: http://tech-algorithm.com/articles/linear-interpolation/
	 * 
	 * @param x_start
	 * @param y_start
	 * @param z_start
	 * @param x_end
	 * @param y_end
	 * @param z_end
	 * @param newDistance
	 * @param totalDistanceToMove
	 * @param dstPoint
	 *            Array that will store the result of the calculus. [x, y, z]
	 */
	public static void linearInterpolate(double x_start, double y_start, double z_start, double x_end,
			double y_end, double z_end, double newDistance, double totalDistanceToMove, double[] dstPoint) {

		double x = (x_start + newDistance * (x_end - x_start) / totalDistanceToMove);
		double y = (y_start + newDistance * (y_end - y_start) / totalDistanceToMove);
		double z = (z_start + newDistance * (z_end) / totalDistanceToMove);

		dstPoint[0] = x;
		dstPoint[1] = y;
		dstPoint[2] = z;
	}

	/**
	 * Check a number if it is a power of two
	 * 
	 * @param n
	 *            The number to check
	 * @return
	 */
	public static boolean isPowerOfTwo(int n) {
		return ((n != 0) && (n & (n - 1)) == 0);
	}

}
