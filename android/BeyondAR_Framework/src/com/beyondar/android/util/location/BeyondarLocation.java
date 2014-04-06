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
package com.beyondar.android.util.location;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

class BeyondarLocation implements LocationListener {

	private List<GeoObject> mArrayListGeoObject;
	private List<World> mArrayListWorld;
	private List<LocationListener> mArrayLocationListener;
	private Object mLockGeoObject;
	private Object mLockWorld;
	private Object mLockLocationListener;
	private volatile Location mLastBestLocation;
	private volatile Location mLastGPSLocation;

	BeyondarLocation() {
		mLockGeoObject = new Object();
		mLockWorld = new Object();
		mLockLocationListener = new Object();
		mArrayListGeoObject = new ArrayList<GeoObject>();
		mArrayListWorld = new ArrayList<World>();
		mArrayLocationListener = new ArrayList<LocationListener>();
	}

	void addGeoObjectLocationUpdate(GeoObject geoObject) {
		synchronized (mLockGeoObject) {
			if (mLastBestLocation != null) {
				geoObject.setLocation(mLastBestLocation);
			}
			if (!mArrayListGeoObject.contains(geoObject))
				mArrayListGeoObject.add(geoObject);
		}
	}

	void removeGeoObjectLocationUpdate(GeoObject geoObject) {
		synchronized (mLockGeoObject) {
			mArrayListGeoObject.remove(geoObject);
		}
	}

	void addWorldLocationUpdate(World world) {
		synchronized (mLockWorld) {
			if (mLastBestLocation != null) {
				world.setLocation(mLastBestLocation);
			}
			if (!mArrayListWorld.contains(world))
				mArrayListWorld.add(world);
		}
	}

	void removeWorldLocationUpdate(World world) {
		synchronized (mLockWorld) {
			mArrayListWorld.remove(world);
		}
	}

	void removeAllWorldsUpdates() {
		synchronized (mLockWorld) {
			mArrayListWorld.clear();
		}
	}

	void addLocationListener(LocationListener locationListener) {
		synchronized (mLockLocationListener) {
			if (!mArrayLocationListener.contains(locationListener))
				mArrayLocationListener.add(locationListener);
		}
	}

	void removeLocationListener(LocationListener locationListener) {
		synchronized (mLockLocationListener) {
			mArrayLocationListener.remove(locationListener);
		}
	}

	void removeAllLocationListener() {
		synchronized (mLockLocationListener) {
			mArrayLocationListener.clear();
		}
	}

	void removeAllGeoObjectsUpdates() {
		synchronized (mLockGeoObject) {
			mArrayListGeoObject.clear();
		}
	}

	void setLastKnowLocation(Location lastKnowLocation) {
		if (LocationUtils.isBetterLocation(lastKnowLocation, mLastBestLocation)) {
			mLastBestLocation = lastKnowLocation;
			onLocationChanged(mLastBestLocation);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null && location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			mLastGPSLocation = location;
		}
		if (!LocationUtils.isBetterLocation(location, mLastBestLocation)) {
			return;
		}
		mLastBestLocation = location;

		synchronized (mLockGeoObject) {
			for (int i = 0; i < mArrayListGeoObject.size(); i++) {
				mArrayListGeoObject.get(i).setLocation(location);
			}
		}
		synchronized (mLockWorld) {
			for (int i = 0; i < mArrayListWorld.size(); i++) {
				mArrayListWorld.get(i).setLocation(location);
			}
		}
		synchronized (mLockLocationListener) {
			for (LocationListener listener : mArrayLocationListener) {
				listener.onLocationChanged(mLastBestLocation);
			}
		}
	}

	Location getLastGpsLocation() {
		return mLastGPSLocation;
	}

	@Override
	public void onProviderDisabled(String provider) {
		synchronized (mLockLocationListener) {
			for (LocationListener listener : mArrayLocationListener) {
				listener.onProviderDisabled(provider);
			}
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		synchronized (mLockLocationListener) {
			for (LocationListener listener : mArrayLocationListener) {
				listener.onProviderEnabled(provider);
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		synchronized (mLockLocationListener) {
			for (LocationListener listener : mArrayLocationListener) {
				listener.onStatusChanged(provider, status, extras);
			}
		}
	}
}
