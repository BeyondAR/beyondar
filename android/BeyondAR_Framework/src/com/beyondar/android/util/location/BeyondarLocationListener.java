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
package com.beyondar.android.util.location;

import java.util.ArrayList;

import com.beyondar.android.world.World;
import com.beyondar.android.world.objects.GeoObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class BeyondarLocationListener implements LocationListener {

	private ArrayList<GeoObject> mArrayListGeoObject;
	private ArrayList<World> mArrayListWorld;
	// private ArrayList<LocationListener> mArrayLocationListener;
	private Object mLock;
	private volatile Location mLastBestLocation;
	private volatile Location mLastGPSLocation;

	BeyondarLocationListener() {
		mLock = new Object();
		mArrayListGeoObject = new ArrayList<GeoObject>();
		mArrayListWorld = new ArrayList<World>();
		// mArrayLocationListener = new ArrayList<LocationListener>();
	}

	public void addGeoObjectLocationUpdate(GeoObject geoObject) {
		synchronized (mLock) {
			if (mLastBestLocation != null) {
				geoObject.setLocation(mLastBestLocation);
			}
			mArrayListGeoObject.add(geoObject);
		}
	}

	public void removeGeoObjectLocationUpdate(GeoObject geoObject) {
		synchronized (mLock) {
			mArrayListGeoObject.remove(geoObject);
		}
	}

	// public void addLocationListener(LocationListener locationListener) {
	// synchronized (mLock) {
	// mArrayLocationListener.add(locationListener);
	// }
	// }
	//
	// public void removeLocationListener(LocationListener locationListener) {
	// synchronized (mLock) {
	// mArrayLocationListener.remove(locationListener);
	// }
	// }

	public void addWorldLocationUpdate(World world) {
		synchronized (mLock) {
			if (mLastBestLocation != null) {
				world.setLocation(mLastBestLocation);
			}
			mArrayListWorld.add(world);
		}
	}

	public void removeWorldLocationUpdate(World world) {
		synchronized (mLock) {
			mArrayListWorld.remove(world);
		}
	}

	public void removeAllGeoObjectsUpdates() {
		synchronized (mLock) {
			mArrayListGeoObject.clear();
		}
	}

	public void removeAllWorldsUpdates() {
		synchronized (mLock) {
			mArrayListWorld.clear();
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

		synchronized (mLock) {
			for (int i = 0; i < mArrayListGeoObject.size(); i++) {
				mArrayListGeoObject.get(i).setLocation(location);
			}

			for (int i = 0; i < mArrayListWorld.size(); i++) {
				mArrayListWorld.get(i).setLocation(location);
			}

			// for (int i = 0; i < mArrayLocationListener.size(); i++) {
			// mArrayLocationListener.get(i).onLocationChanged(mLastBestLocation);
			// }
		}
	}

	Location getLastGpsLocation() {
		return mLastGPSLocation;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
