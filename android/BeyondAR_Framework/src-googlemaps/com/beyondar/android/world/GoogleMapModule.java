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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.util.PendingBitmapsToBeLoaded;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.cache.BitmapCache.OnExternalBitmapLoadedCahceListener;
import com.beyondar.android.world.module.WorldModule;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapModule implements WorldModule, OnExternalBitmapLoadedCahceListener {

	/** Default icon size for the markers in dips */
	public static final int DEFAULT_ICON_SIZE_MARKER = 40;

	private World mWorld;
	private GoogleMap mMap;

	private BitmapCache mCache;
	private int mIconSize;
	private PendingBitmapsToBeLoaded<GeoObjectGoogleMaps> mPendingBitmaps;

	private LatLng mLatLng;

	private static Handler sHandler = new Handler(Looper.getMainLooper());

	public GoogleMapModule() {
		mPendingBitmaps = new PendingBitmapsToBeLoaded<GeoObjectGoogleMaps>();
	}

	public GoogleMapModule(GoogleMap map) {
		this();
		mMap = map;
	}

	public GoogleMapModule(int iconSize) {
		this();
		mIconSize = iconSize;
	}

	/**
	 * Set the size of the marker icons in pixels
	 * 
	 * @param iconSize
	 * @return The instance itself
	 */
	public GoogleMapModule setMarkerIconSize(int iconSize) {
		mIconSize = iconSize;
		return this;
	}

	protected BitmapCache createBitmapCache() {
		return mWorld.getBitmapCache().newCache(getClass().getName(), true);
	}

	protected void addMarkerToBeyondarObject(BeyondarObject beyondarObject) {
		if (beyondarObject instanceof GeoObjectGoogleMaps) {
			createMarker((GeoObjectGoogleMaps) beyondarObject);
		}
	}

	public LatLng getLatLng() {
		if (mLatLng == null) {
			mLatLng = new LatLng(mWorld.getLatitude(), mWorld.getLongitude());
			return mLatLng;
		}

		if (mLatLng.latitude == mWorld.getLatitude() && mLatLng.longitude == mWorld.getLongitude()) {
			return mLatLng;
		}

		mLatLng = new LatLng(mWorld.getLatitude(), mWorld.getLongitude());
		return mLatLng;
	}

	/**
	 * Set the {@link GoogleMap} to be able to create the markers for the world
	 * 
	 * @param map
	 * @return The instance of itself
	 */
	public GoogleMapModule setGoogleMap(GoogleMap map) {
		mMap = map;
		createMarkers();
		return this;
	}

	public void createMarkers() {
		if (mWorld == null || mMap == null) {
			return;
		}
		for (int i = 0; i < mWorld.getBeyondarObjectLists().size(); i++) {
			BeyondarObjectList list = mWorld.getBeyondarObjectList(i);
			for (int j = 0; j < list.size(); j++) {
				BeyondarObject beyondarObject = list.get(j);
				if (beyondarObject instanceof GeoObjectGoogleMaps) {
					createMarker((GeoObjectGoogleMaps) beyondarObject);
				}
			}
		}
	}

	protected void createMarker(GeoObjectGoogleMaps geoObjectGoogleMaps) {
		Marker marker = geoObjectGoogleMaps.getMarker();
		if (marker != null) {
			marker.remove();
		}

		if (mMap == null) {
			return;
		}
		marker = mMap.addMarker(generateMarkerOptions(geoObjectGoogleMaps));
		geoObjectGoogleMaps.setMarker(marker);
	}

	protected MarkerOptions generateMarkerOptions(GeoObjectGoogleMaps geoObject) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(geoObject.getName());
		markerOptions.position(geoObject.getLatLng());

		Bitmap btm = getBitmap(geoObject.getBitmapUri());

		if (btm != null) {
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(btm));
		} else {
			mPendingBitmaps.addObject(geoObject.getBitmapUri(), geoObject);
		}
		return markerOptions;
	}

	protected Bitmap getBitmap(String uri) {
		Bitmap btm = mCache.getBitmap(uri);

		if (btm == null || btm.isRecycled()) {
			return null;
		}

		if (btm.getHeight() != mIconSize && btm.getWidth() != mIconSize) {
			Bitmap tmp = ImageUtils.resizeImage(btm, mIconSize, mIconSize);
			mCache.storeBitmap(uri, tmp);
			if (btm != tmp) {
				btm.recycle();
			}
			btm = tmp;
		}

		return btm;
	}

	@Override
	public void onExternalBitmapLoaded(BitmapCache cache, String url, Bitmap btm) {
		ArrayList<GeoObjectGoogleMaps> list = mPendingBitmaps.getPendingList(url);
		for (int i = 0; i < list.size(); i++) {
			final GeoObjectGoogleMaps gogm = list.get(i);
			sHandler.post(new Runnable() {
				@Override
				public void run() {
					Bitmap btm = getBitmap(gogm.getBitmapUri());
					if (btm != null) {
						gogm.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(btm));
					}
				}
			});
		}
	}

	@Override
	public void setup(World world, Context context) {
		mWorld = world;
		if (mIconSize == 0) {
			mIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					DEFAULT_ICON_SIZE_MARKER, context.getResources().getDisplayMetrics());
		}

		mCache = createBitmapCache();
		mCache.addOnExternalBitmapLoadedCahceListener(this);
		createMarkers();
	}

	@Override
	public void onDetached(World world, Context context) {
		mCache.clean();
	}

	@Override
	public void onBeyondarObjectAdded(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
		addMarkerToBeyondarObject(beyondarObject);
	}

	@Override
	public void onBeyondarObjectListCreated(BeyondarObjectList beyondarObjectList) {
	}

	@Override
	public void onBeyondarObjectRemoved(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
	}

	@Override
	public void onWorldCleaned() {
	}

	@Override
	public void onPositionChanged(double latitude, double longitude, double altitude) {
	}
}
