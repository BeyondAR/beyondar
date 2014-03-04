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
package com.beyondar.android.module.googlemap;

import android.graphics.Bitmap;

import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.module.GeoObjectModule;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public interface GoogleMapGeoObjectModule extends GeoObjectModule {

	/**
	 * Setup the module according to the BeyondarObject
	 * 
	 * @param BeyondarObject
	 */
	public void setup(BeyondarObject beyondarObject);

	/**
	 * Get the {@link LatLng} instance that represents the {@link GeoObject}. It
	 * will try to recycle the {@link LatLng} object if it is possible
	 * 
	 * @return
	 */
	public LatLng getLatLng();

	/**
	 * Set the {@link Marker} that belongs to the {@link GeoObject}
	 * 
	 * @param marker
	 */
	public void setMarker(Marker marker);

	/**
	 * Get the marker that belongs to the {@link GeoObject}
	 * 
	 * @return
	 */
	public Marker getMarker();

	/**
	 * Create the marker options in order to create the Marker.
	 * 
	 * @param bitmap The bitmap to use for representing the {@link Marker}
	 * @return
	 */
	public MarkerOptions createMarkerOptions(Bitmap bitmap);

}
