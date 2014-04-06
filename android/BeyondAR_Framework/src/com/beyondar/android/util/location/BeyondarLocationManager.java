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

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

/**
 * This class provides a helper to get the best location. To do that the
 * application needs to add the following permissions in the manifest: <br>
 * 
 * <pre>
 *     android.permission.ACCESS_FINE_LOCATION
 *     android.permission.ACCESS_COARSE_LOCATION
 * </pre>
 * 
 * Remember that you also can use the Location utility in the Google Services. <br>
 * <br>
 * Here is a small example how to use {@link BeyondarLocationManager}: <br>
 * 
 * <pre>
 * <code>
 * void onCreate(Bundle savedInstanceState){
 * 	BeyondarLocationManager.setLocationManager((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
 * 	BeyondarLocationManager.addGeoObjectLocationUpdate(beyondarObject);
 * 	// You also can register a World or a LocationListener
 * 	// Don't forget to remove the object that you register
 * }
 * void onResume(){
 * 	BeyondarLocationManager.enable();
 * }
 * 
 * void onPause(){
 * 	BeyondarLocationManager.disable();
 * }
 * </code>
 * </pre>
 */
public class BeyondarLocationManager {

	public final static int MAX_TIME_GPS_FIX = 20000;

	private BeyondarLocationManager() {
	}

	private static enum BeyondarLocationManagerSingleton {
		INSTANCE;

		private LocationManager mLocationManager;
		private BeyondarLocation mLocationListener;
		private BeyondarGpsListener mGpsListener;
		private boolean mIsEnabled;
		private boolean mGpsFix;

		private BeyondarLocationManagerSingleton() {
			mLocationListener = new BeyondarLocation();
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

				if (LocationUtils.isBetterLocation(lastNetworkLocation, lastGpsLocation)) {
					mLocationListener.setLastKnowLocation(lastNetworkLocation);
				} else {
					mLocationListener.setLastKnowLocation(lastGpsLocation);
				}
			}
			registerLocationListener(mLocationListener, mGpsListener);

			mIsEnabled = true;

		}

		public void registerLocationListener(LocationListener locationListener, GpsStatus.Listener gpsListener) {
			if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
				mLocationManager.addGpsStatusListener(gpsListener);
			}
			if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
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
						if ((System.currentTimeMillis() - mLocationListener.getLastGpsLocation().getTime()) < MAX_TIME_GPS_FIX) {
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

	/**
	 * Set the {@link LocationManager} needed by the helper to be able to take
	 * care of the location.
	 * 
	 * @param locationManager
	 */
	public static void setLocationManager(LocationManager locationManager) {
		// TODO: Check what happens if an other locationManager is set
		BeyondarLocationManagerSingleton.INSTANCE.setLocationManager(locationManager);
	}

	/**
	 * Add a {@link com.beyondar.android.world.GeoObject GeoObject} that will be
	 * updated with the user location.
	 * 
	 * @param geoObject
	 */
	public static void addGeoObjectLocationUpdate(GeoObject geoObject) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.addGeoObjectLocationUpdate(geoObject);
	}

	/**
	 * Remove the specified {@link com.beyondar.android.world.GeoObject
	 * GeoObject} to don't get any update about the user location.
	 * 
	 * @param geoObject
	 */
	public static void removeGeoObjectLocationUpdate(GeoObject geoObject) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.removeGeoObjectLocationUpdate(geoObject);
	}

	/**
	 * Remove all the {@link com.beyondar.android.world.GeoObject GeoObject} to
	 * get the location updates.
	 */
	public static void removeAllGeoObjectsUpdates() {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.removeAllGeoObjectsUpdates();
	}

	/**
	 * Add a {@link World} object that will be updated with the user location.
	 * 
	 * @param world
	 */
	public static void addWorldLocationUpdate(World world) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.addWorldLocationUpdate(world);
	}

	/**
	 * Remove the specified {@link World} to don't get any update about the user
	 * location.
	 * 
	 * @param world
	 */
	public static void removeWorldLocationUpdate(World world) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.removeWorldLocationUpdate(world);
	}

	/**
	 * Remove all the {@link World} to get the location updates.
	 */
	public static void removeAllWorldsUpdates() {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.removeAllWorldsUpdates();
	}

	/**
	 * Add a {@link LocationListener} object that will be updated with the user
	 * location.
	 * 
	 * @param locationListener
	 */
	public static void addLocationListener(LocationListener locationListener) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.addLocationListener(locationListener);
	}

	/**
	 * Remove the specified {@link LocationListener} to don't get any update
	 * about the user location.
	 * 
	 * @param locationListener
	 */
	public static void removeLocationListener(LocationListener locationListener) {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.removeLocationListener(locationListener);
	}

	/**
	 * Remove all the {@link LocationListener} to get the location updates.
	 */
	public static void removeAllLocationListener() {
		BeyondarLocationManagerSingleton.INSTANCE.mLocationListener.removeAllLocationListener();
	}

	/**
	 * Enable the location services for the helper. Once it will be enabled it
	 * will start consuming battery.
	 */
	public static void enable() {
		BeyondarLocationManagerSingleton.INSTANCE.enable();
	}

	/**
	 * Disable the location services for the helper. If the location is not
	 * needed use this method in order to save battery.
	 */
	public static void disable() {
		BeyondarLocationManagerSingleton.INSTANCE.disable();
	}

	/**
	 * Check if location is enabled or not.
	 * 
	 * @return
	 */
	public static boolean isEnabled() {
		return BeyondarLocationManagerSingleton.INSTANCE.isEnabled();
	}

	/**
	 * Check if the satellites are giving a correct location.
	 * 
	 * @return
	 */
	public static boolean gpsFix() {
		return BeyondarLocationManagerSingleton.INSTANCE.gpsFix();
	}
}
