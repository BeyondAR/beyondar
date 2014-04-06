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

import com.beyondar.android.world.GeoObject;

//http://www.movable-type.co.uk/scripts/latlong.html
/**
 * Class to calculate the distance between two points in arbitrary units
 * 
 */
public class Distance {
	/** Names for the units to use */
//	public final static int KILOMETERS = 0;
//	public final static int STATUTE_MILES = 1;
//	public final static int NAUTICAL_MILES = 2;

	/** Dive the meter to this number to get the geopoints (double) */
	public static final double METERS_TO_GEOPOINT = 107817.51838439942;
	public static final int E6 = 1000000;

	public static final double EARTH_RADIUS_KM = 6384;// km

	/**
	 * This method do an approximation form meters to geopoints. Do not use it
	 * for long distances (> 5 km)
	 * 
	 * @param meters
	 * @return
	 */
	public static double fastConversionMetersToGeoPoints(double meters) {
		return meters / METERS_TO_GEOPOINT;
	}

	/**
	 * This method do an approximation form geopoints to meters. Do not use it
	 * for long distances (> 5 km)
	 * 
	 * @param geoPoints
	 * @return
	 */
	public static double fastConversionGeopointsToMeters(double geoPoints) {
		return geoPoints * METERS_TO_GEOPOINT;
	}

	/**
	 * Calculate the distance using the coordinates. It return a coordinates, no
	 * meters
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double calculateDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
	}

	public static double calculateDistanceMeters(GeoObject objA, GeoObject objB) {
		return calculateDistanceMeters(objA.getLongitude(), objA.getLatitude(), objB.getLongitude(),
				objB.getLatitude());
	}

	public static double calculateDistanceMeters(double aLong, double aLat, double bLong, double bLat) {

		double d2r = (Math.PI / 180);

		double dLat = (bLat - aLat) * d2r;
		double dLon = (bLong - aLong) * d2r;
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(aLat * d2r) * Math.cos(bLat * d2r)
				* Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS_KM * c * 1000;

	}

	public static double calculateAreakm2(double longitude_1, double latitude_1, double longitude_2,
			double latitude_2) {

		double sizeX = Distance.calculateDistanceMeters(longitude_1, latitude_1, longitude_2, latitude_1) / 1000;
		double sizeY = Distance.calculateDistanceMeters(longitude_1, latitude_1, longitude_1, latitude_2) / 1000;

		return sizeX * sizeY;
	}

	public static double calculateDistanceCoordinates(double aLong, double aLat, double bLong, double bLat) {
		return calculateDistance(aLong, aLat, bLong, bLat);
	}

	public static double calculateDistanceCoordinates(double aLong, double aLat, double aAlt, double bLong,
			double bLat, double bAlt) {
		return calculateDistance(aLong, aLat, aAlt, bLong, bLat, bAlt);
	}
}