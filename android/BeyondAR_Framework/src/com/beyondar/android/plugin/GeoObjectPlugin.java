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
package com.beyondar.android.plugin;

import com.beyondar.android.world.GeoObject;

/**
 * Extension of the {@link com.beyondar.android.plugin.BeyondarObjectPlugin
 * BeyondarObjectPlugin} for {@link com.beyondar.android.world.GeoObject
 * GeoObject}.
 */
public interface GeoObjectPlugin extends BeyondarObjectPlugin {

	/**
	 * Notified when the geo position changes.
	 */
	public void onGeoPositionChanged(double latitude, double longitude, double altitude);

	/**
	 * Get the {@link com.beyondar.android.world.GeoObject GeoObject} where the plugin is attached.
	 * 
	 * @return
	 */
	public GeoObject getGeoObject();

}
