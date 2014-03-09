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
package com.beyondar.android.module;

import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.World;

public interface BeyondarModule extends Module{

	/**
	 * This method is invoked when the module is removed.
	 */
	public void onDetached();

	/**
	 * Check if the module is attached.
	 * 
	 * @return
	 */
	public boolean isAttached();

	/**
	 * Setup the module according to the world
	 * 
	 * @param world
	 *            The world that loads the module
	 * 
	 */
	public void setup(World world);

	/**
	 * This method is invoked when a new {@link BeyondarObject} is added to the
	 * world
	 * 
	 * @param beyondarObject
	 * @param beyondarObjectList
	 * @return
	 */
	public void onBeyondarObjectAdded(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList);

	/**
	 * This method is invoked when a {@link BeyondarObject} is removed from the
	 * world
	 * 
	 * @param beyondarObject
	 * @param beyondarObjectList
	 */
	public void onBeyondarObjectRemoved(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList);

	/**
	 * This method is invoked when a new {@link BeyondarObjectList} is created
	 */
	public void onBeyondarObjectListCreated(BeyondarObjectList beyondarObjectList);

	/**
	 * This method is invoked when the world is cleaned
	 */
	public void onWorldCleaned();

	/**
	 * This method is invoked when the position is changed
	 * 
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void onGeoPositionChanged(double latitude, double longitude, double altitude);

	/**
	 * This method is invoked when the default image for all the
	 * {@link BeyondarObject} is set
	 * 
	 * @param uri
	 */
	public void onDefaultImageChanged(String uri);

	/** Called when the fragment is resumed */
	public void onResume();

	/** Called when the fragment is paused */
	public void onPause();
}
