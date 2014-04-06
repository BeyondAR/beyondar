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
package com.beyondar.android.world;

import android.location.Location;

import com.beyondar.android.plugin.BeyondarObjectPlugin;
import com.beyondar.android.plugin.GeoObjectPlugin;
import com.beyondar.android.util.math.Distance;

/**
 * Extension of the {@link com.beyondar.android.world.BeyondarObject BeyondarObject} to make easier the usage of geo
 * cordinates.
 * 
 */
public class GeoObject extends BeyondarObject {

	private double mLongitude;
	private double mLatitude;
	private double mAltitude;

	/**
	 * Create an instance of a {@link com.beyondar.android.world.GeoObject
	 * GeoObject} with an unique ID.
	 * 
	 * @param id
	 *            Unique ID.
	 */
	public GeoObject(long id) {
		super(id);
		setVisible(true);
	}

	/**
	 * Create an instance of a {@link com.beyondar.android.world.GeoObject
	 * GeoObject} with an unique ID. The hash of the object will be used as the
	 * {@link com.beyondar.android.world.GeoObject GeoObject} unique id.
	 */
	public GeoObject() {
		super();
		setVisible(true);
	}

	/**
	 * Set the position of the {@link com.beyondar.android.world.GeoObject GeoObject}.
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public void setGeoPosition(double latitude, double longitude) {
		setGeoPosition(latitude, longitude, mAltitude);
	}

	/**
	 * Set the position of the {@link com.beyondar.android.world.GeoObject GeoObject}.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setGeoPosition(double latitude, double longitude, double altitude) {
		mLatitude = latitude;
		mLongitude = longitude;
		mAltitude = altitude;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				if (plugin instanceof GeoObjectPlugin) {
					((GeoObjectPlugin) plugin).onGeoPositionChanged(latitude, longitude, altitude);
				}
			}
		}
	}

	/**
	 * Get the longitude.
	 * 
	 * @return longitude.
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * Get the altitude.
	 * 
	 * @return altitude.
	 */
	public double getAltitude() {
		return mAltitude;
	}

	/**
	 * Get the latitude.
	 * 
	 * @return latitude.
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * Set the location.
	 * 
	 * @param location
	 *            New location.
	 */
	public void setLocation(Location location) {
		if (location == null) {
			return;
		}
		setGeoPosition(location.getLatitude(), location.getLongitude());
	}

	/**
	 * Calculate the distance of this {@link com.beyondar.android.world.GeoObject GeoObject} from an other
	 * {@link com.beyondar.android.world.GeoObject GeoObject}.
	 * 
	 * @param geo
	 *            The other {@link com.beyondar.android.world.GeoObject GeoObject} to calculate the distance.
	 * @return The distance in meters.
	 */
	public double calculateDistanceMeters(GeoObject geo) {
		return calculateDistanceMeters(geo.getLongitude(), geo.getLatitude());
	}

	/**
	 * Calculate the distance of this {@link com.beyondar.android.world.GeoObject GeoObject} from an other
	 * {@link com.beyondar.android.world.GeoObject GeoObject}.
	 * 
	 * @param longitude
	 *            The other {@link com.beyondar.android.world.GeoObject GeoObject} longitude to calculate the
	 *            distance.
	 * @param latitude
	 *            The other {@link com.beyondar.android.world.GeoObject GeoObject} latitude to calculate the
	 *            distance.
	 * @return The distance in meters.
	 */
	public double calculateDistanceMeters(double longitude, double latitude) {
		return Distance.calculateDistanceMeters(getLongitude(), getLatitude(), longitude, latitude);
	}
}