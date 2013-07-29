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
package com.beyondar.android.googlemaps;

import android.content.Context;

import com.beyondar.android.world.World;
import com.google.android.maps.Overlay;

/**
 * @author Joan Puig Sanz (joanpuigsanz@gmail.com)
 * 
 */
public class World_google_maps extends World {

	private double mMapViewDistance;

	/**
	 * In DIP
	 */
	public static final int DEFAULT_SIZE_ICONS_MAP = 35;

	public World_google_maps(Context context) {
		super(context);
		mMapViewDistance = getViewDistance();
	}

	public GeoObjectOberlay getOverlay(int iconSize) {
		return new GeoObjectOberlay(iconSize, this);
	}

	public Overlay getOverlay() {
		return getOverlay(DEFAULT_SIZE_ICONS_MAP);

	}

	/**
	 * Set the distance (in meters) which the app will draw the objects.
	 * 
	 * @param meters
	 */
	public void setMapViewDistance(double meters) {
		mMapViewDistance = meters;
	}

	/**
	 * Get the distance (in meters) which the app will draw the objects.
	 * 
	 * @return meters
	 */
	public double getMapViewDistance() {
		return mMapViewDistance;
	}

}
