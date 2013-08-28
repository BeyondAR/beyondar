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

import com.beyondar.android.world.World;
import com.beyondar.android.world.objects.GeoObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.location.GpsStatus;

public class BeyondarLocationHelper {

	public final static int MAX_TIME_GPS_FIX = 20000;

	private static enum BeyondarLocationManagerSingleton {
		INSTANCE;

		private LocationManager mLocationManager;
		private BeyondarLocationListener mLocationListener;
		private BeyondarGpsListener mGpsListener;
		private boolean mIsEnabled;
		private boolean mGpsFix;

		private BeyondarLocationManagerSingleton() {
			mLocationListener = new BeyondarLocationListener();
			mGpsListener = new BeyondarGpsListener();
			mIsEnabled = false;
			mGpsFix = false;
		}

		public void setLocationManager(LocationManager locationManager) {
			mLocationManager = locationManager;
		}

		public void enable() {
			if (mLocationManager == null) {
				mIsEnabled = false;
				return;
			}
			if (!mIsEnabled) {
				Location lastGpsLocation = mLocationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				Location lastNetworkLocation = mLocationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

				if (LocationUtils.isBetterLocation(lastNetworkLocation,
						lastGpsLocation)) {
					mLocationListener.setLastKnowLocation(lastNetworkLocation);
				} else {
					mLocationListener.setLastKnowLocation(lastGpsLocation);
				}
			}
			registerLocationListener(mLocationListener, mGpsListener);

			mIsEnabled = true;

		}

		public void registerLocationListener(LocationListener locationListener,
				GpsStatus.Listener gpsListener) {
			if (mLocationManager.getAllProviders().contains(
					LocationManager.GPS_PROVIDER)) {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, locationListener);
				mLocationManager.addGpsStatusListener(gpsListener);
			}
			if (mLocationManager.getAllProviders().contains(
					LocationManager.NETWORK_PROVIDER)) {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0,
						locationListener);
			}

		}

		public void disable() {
			mIsEnabled = false;
			mGpsFix = false;
			if (mLocationManager != null) {
				mLocationManager.removeUpdates(mLocationListener);
			}
		}

		public boolean isEnabled() {
			return mIsEnabled;
		}

		public boolean gpsFix() {
			return mGpsFix;
		}

		private class BeyondarGpsListener implements GpsStatus.Listener {
			public void onGpsStatusChanged(int event) {
				switch (event) {
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					if (mLocationListener.getLastGpsLocation() != null) {
						if ((System.currentTimeMillis() - mLocationListener
								.getLastGpsLocation().getTime()) < MAX_TIME_GPS_FIX) {
							if (!mGpsFix)
								mGpsFix = true;
						} else {
							if (mGpsFix) {
								enable();
							}
							mGpsFix = false;
						}
					}
					break;
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					mGpsFix = true;
					break;
				case GpsStatus.GPS_EVENT_STARTED:
					break;
				case GpsStatus.GPS_EVENT_STOPPED:
					mGpsFix = false;
					break;
				}
			}
		}
	}

	public static void setLocationManager(LocationManager locationManager) {
		BeyondarLocationManagerSingleton.INSTANCE
				.setLocationManager(locationManager);
	}

	public static void addGeoObjectLocationUpdate(GeoObject geoObject) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener
				.addGeoObjectLocationUpdate(geoObject);
	}

	public static void removeGeoObjectLocationUpdate(GeoObject geoObject) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener
				.removeGeoObjectLocationUpdate(geoObject);
	}

	public static void removeWorldLocationUpdate(World world) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener
				.removeWorldLocationUpdate(world);
	}

	public static void addWorldLocationUpdate(World world) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener
				.addWorldLocationUpdate(world);
	}

	public static void removeAllGeoObjectsUpdates() {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener
				.removeAllGeoObjectsUpdates();
	}

	public static void removeAllWorldsUpdates() {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener
				.removeAllWorldsUpdates();
	}

	public static void enable() {
		BeyondarLocationManagerSingleton.INSTANCE.enable();
	}

	public static void disable() {
		BeyondarLocationManagerSingleton.INSTANCE.disable();
	}

	public static boolean isEnabled() {
		return BeyondarLocationManagerSingleton.INSTANCE.isEnabled();
	}

	public static boolean gpsFix() {
		return BeyondarLocationManagerSingleton.INSTANCE.gpsFix();
	}
}
