/*
 * Copyright (C) 2013 BeyondAR
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

import com.beyondar.android.world.objects.GeoObject;

//http://www.movable-type.co.uk/scripts/latlong.html
/**
 * Class to calculate the distance between two points in arbitrary units
 * 
 */
public class Distance {
	/** Names for the units to use */
	public final static int KILOMETERS = 0;
	public final static int STATUTE_MILES = 1;
	public final static int NAUTICAL_MILES = 2;

	/** Dive the meter to this number to get the geopoints (double) */
	public static final double METERS_TO_GEOPOINT = 107817.51838439942;
	public static final int E6 = 1000000;

	public static final double EARTH_RADIUS_KM = 6384;// km

	
	/**
	 * This method do an approximation form meters to geopoints. Do not use it for long distances (> 5 km)
	 * @param meters
	 * @return
	 */
	public static double fastConversionMetersToGeoPoints(double meters){
		return meters / METERS_TO_GEOPOINT;
	}
	
	/**
	 * This method do an approximation form geopoints to meters. Do not use it for long distances (> 5 km)
	 * @param meters
	 * @return
	 */
	public static double fastConversionGeopointsToMeters(double geoPoints){
		return geoPoints * METERS_TO_GEOPOINT;
	}
	
	
	/**
	 * Calculate the distance using the coordinates. It return a coordinates, no
	 * meters
	 * 
	 * @param aLong
	 * @param aLat
	 * @param bLong
	 * @param bLat
	 * @return
	 */
	public static double calculateDistance(double aLong, double aLat,
			double bLong, double bLat) {
		return calculateDistanceCoordinates(aLong, aLat, bLong, bLat);
	}

	// public static double[] calculate2(double lon1, double lat1, double angle,
	// double d) {
	// double out[] = new double[2];
	//
	// out[1] = Math.asin(Math.sin(lat1) * Math.cos(d / EARTH_RADIUS_KM)
	// + Math.cos(lat1) * Math.sin(d / EARTH_RADIUS_KM)
	// * Math.cos(angle));
	// out[0] = lon1
	// + Math.atan2(
	// Math.sin(angle) * Math.sin(d / EARTH_RADIUS_KM)
	// * Math.cos(lat1),
	// Math.cos(d / EARTH_RADIUS_KM) - Math.sin(lat1)
	// * Math.sin(out[1]));
	//
	// // double d_a =Math.sqrt(Math.pow(lon2 - lon1,2 ) + Math.pow(lat2 -
	// // lat1,2 )) ;
	// // System.out.println("alpha= " + (Math.toDegrees(angle)% 360) +
	// // "    = " + angle);
	// // System.out.println("x_a= " +lon2 + "  y_a= " +lat2 +" dst= " + d_a);
	// return out;
	//
	// }

	public static double calculateDistanceMeters(GeoObject objA, GeoObject objB) {
		return calculateDistanceMeters(objA.getLongitude(), objA.getLatitude(),
				objB.getLongitude(), objB.getLatitude());
	}

	public static double calculateDistanceMeters(double aLong, double aLat,
			double bLong, double bLat) {

		double d2r = (Math.PI / 180);

		double dLat = (bLat - aLat) * d2r;
		double dLon = (bLong - aLong) * d2r;
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(aLat * d2r) * Math.cos(bLat * d2r)
				* Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS_KM * c * 1000;

	}

	public static double calculateAreakm2(double longitude_1,
			double latitude_1, double longitude_2, double latitude_2) {

		double sizeX = Distance.calculateDistanceMeters(longitude_1,
				latitude_1, longitude_2, latitude_1) / 1000;
		double sizeY = Distance.calculateDistanceMeters(longitude_1,
				latitude_1, longitude_1, latitude_2) / 1000;

		// Log.d(Beyondar.TAG, "dst1=" + sizeX + " dst2=" + sizeY);
		// Log.d(Beyondar.TAG, "area="+sizeX * sizeY);

		return sizeX * sizeY;
	}

	public static double calculateDistanceCoordinates(double x1, double y1,
			double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public static double calculateDistanceCoordinates(double x1, double y1,
			double z1, double x2, double y2, double z2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)
				+ Math.pow(z1 - z2, 2));
	}

	// //////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////
	// //////////////////// NOT IMPLEMENTED!!! //////////////////////
	// //////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////
	//
	// /** Radius of the Earth in the units above */
	// private final static double EARTHS_RADIUS[] = { 6378.1, // Kilometers
	// 3963.1676, // Statue miles
	// 3443.89849 // Nautical miles
	// };
	//
	// /** Conversion factor to convert from degrees to radians */
	// private static final double DEGREES_TO_RADIANS = (double) (180 /
	// Math.PI);
	//
	// /**
	// * Calculates the "length" of an arc between two points on a sphere given
	// * the latitude & longitude of those points.
	// *
	// * @param aLat
	// * Latitude of point A
	// * @param aLong
	// * Longitude of point A
	// * @param bLat
	// * Latitude of point B
	// * @param bLong
	// * Longitude of point B
	// * @return
	// */
	// private static double calclateArc(double aLong, double aLat, double
	// bLong,
	// double bLat) {
	// /*
	// * Convert location a and b's lattitude and longitude from degrees to
	// * radians
	// */
	// double aLatRad = aLat / DEGREES_TO_RADIANS;
	// double aLongRad = aLong / DEGREES_TO_RADIANS;
	// double bLatRad = bLat / DEGREES_TO_RADIANS;
	// double bLongRad = bLong / DEGREES_TO_RADIANS;
	//
	// // Calculate the length of the arc that subtends point a and b
	// double t1 = Math.cos(aLatRad) * Math.cos(aLongRad) * Math.cos(bLatRad)
	// * Math.cos(bLongRad);
	// double t2 = Math.cos(aLatRad) * Math.sin(aLongRad) * Math.cos(bLatRad)
	// * Math.sin(bLongRad);
	// double t3 = Math.sin(aLatRad) * Math.sin(bLatRad);
	// double tt = Math.acos(t1 + t2 + t3);
	//
	// // Return a "naked" length for the calculated arc
	// return tt;
	// }
	//
	// /**
	// * Calculates the "length" of an arc between two points on a sphere given
	// * the latitude & longitude of those points.
	// *
	// * @param aLat
	// * Latitude of point A
	// * @param aLong
	// * Longitude of point A
	// * @param bLat
	// * Latitude of point B
	// * @param bLong
	// * Longitude of point B
	// * @param units
	// * The units to get KILOMETERS, STATUTE_MILES or NAUTICAL_MILES
	// * @return
	// */
	// private static double calclateArc(double aLong, double aLat, double
	// bLong,
	// double bLat, int units) {
	// return calclateArc(aLong, aLat, bLong, bLat) * EARTHS_RADIUS[units];
	// }
	//
	// /**
	// * Calculates the distance between two addresses
	// *
	// * @param pointA
	// * Address of point A
	// * @param pointB
	// * Address of point B
	// * @param units
	// * Desired units
	// * @return Distance between the two points in the desired units
	// */
	// private static double calculateDistance(Address pointA, Address pointB,
	// int units) {
	// return calclateArc(pointA.getLongitude(), pointA.getLatitude(),
	// pointB.getLongitude(), pointB.getLatitude())
	// * EARTHS_RADIUS[units];
	// }
	//
	// /**
	// * Calculates the distance between two locations
	// *
	// * @param pointA
	// * Location of point A
	// * @param pointB
	// * Location of point B
	// * @param units
	// * Desired units
	// * @return Distance between the two points in the desired units
	// */
	// private static double calculateDistance(Location pointA, Location pointB,
	// int units) {
	// return calclateArc(pointA.getLongitude(), pointA.getLatitude(),
	// pointB.getLongitude(), pointB.getLatitude())
	// * EARTHS_RADIUS[units];
	// }
	//
	// private static double distFrom(double lng1, double lat1, double lng2,
	// double lat2) {
	// double earthRadius = 3958.75;
	// double dLat = Math.toRadians(lat2 - lat1);
	// double dLng = Math.toRadians(lng2 - lng1);
	// double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
	// + Math.cos(Math.toRadians(lat1))
	// * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
	// * Math.sin(dLng / 2);
	// double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	// double dist = earthRadius * c;
	//
	// int meterConversion = 1609;
	//
	// return dist * meterConversion;
	// }

}