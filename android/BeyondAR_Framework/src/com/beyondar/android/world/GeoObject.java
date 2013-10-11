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
package com.beyondar.android.world;

import android.location.Location;

import com.beyondar.android.util.math.Distance;
import com.beyondar.android.world.module.BeyondarObjectModule;
import com.beyondar.android.world.module.GeoObjectModule;

public class GeoObject extends BeyondarObject {

	private double mLongitude;
	private double mLatitude;
	private double mAltitude;

	/**
	 * Create an instance of a {@link GeoObject} with an unique ID
	 * 
	 * @param id
	 *            Unique ID
	 */
	public GeoObject(long id) {
		super(id);
		setVisibile(true);
	}

	public void setGeoPosition(double latitude, double longitude) {
		setGeoPosition(latitude, longitude, mAltitude);
	}

	public void setGeoPosition(double latitude, double longitude, double altitude) {
		mLatitude = latitude;
		mLongitude = longitude;
		mAltitude = altitude;
		synchronized (lockModules) {
			for (BeyondarObjectModule module : modules) {
				if (module instanceof GeoObjectModule) {
					((GeoObjectModule) module).onGeoPositionChanged(latitude, longitude, altitude);
				}
			}
		}
	}

	public double getLongitude() {
		return mLongitude;
	}

	public double getAltitude() {
		return mAltitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLocation(Location location) {

		if (location == null) {
			return;
		}
		setGeoPosition(location.getLatitude(), location.getLongitude());
	}

	public double calculateDistanceMeters(GeoObject geo) {
		return calculateDistanceMeters(geo.getLongitude(), geo.getLatitude());
	}

	public double calculateDistanceMeters(double longitude, double latitude) {
		return Distance.calculateDistanceMeters(getLongitude(), getLatitude(), longitude, latitude);
	}

}