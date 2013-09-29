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

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class GeoObjectGoogleMaps extends GeoObject {

	private Marker mMarker;
	private LatLng mLatLng;

	public GeoObjectGoogleMaps(long id) {
		super(id);
	}

	@Override
	public void setGeoPosition(double latitude, double longitude) {
		super.setGeoPosition(latitude, longitude);
		updateMarker();

	}

	@Override
	public void setGeoPosition(double latitude, double longitude, double altitude) {
		super.setGeoPosition(latitude, longitude, altitude);
		updateMarker();
	}
	
	protected void updateMarker() {
		if (mMarker == null){
			return;
		}
		mMarker.setPosition(getLatLng());
	}

	public LatLng getLatLng() {
		if (mLatLng == null) {
			mLatLng = new LatLng(getLatitude(), getLongitude());
			return mLatLng;
		}

		if (mLatLng.latitude == getLatitude() && mLatLng.longitude == getLongitude()) {
			return mLatLng;
		}

		mLatLng = new LatLng(getLatitude(), getLongitude());
		return mLatLng;
	}

	public void setMarker(Marker marker) {
		mMarker = marker;
	}

	/**
	 * Get the marker for the google maps API.
	 * 
	 * @return The marker used for this {@link GeoObject}, null if there is no
	 *         marker defined
	 */
	public Marker getMarker() {
		return mMarker;
	}
}
