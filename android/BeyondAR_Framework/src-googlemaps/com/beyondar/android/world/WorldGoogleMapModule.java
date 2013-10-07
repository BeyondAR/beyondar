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
import com.beyondar.android.world.module.GeoObjectGoogleMapModule;
import com.beyondar.android.world.module.WorldModule;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WorldGoogleMapModule implements WorldModule, OnExternalBitmapLoadedCahceListener {

	/** Default icon size for the markers in dips */
	public static final int DEFAULT_ICON_SIZE_MARKER = 40;

	private World mWorld;
	private GoogleMap mMap;

	private BitmapCache mCache;
	private int mIconSize;
	private PendingBitmapsToBeLoaded<GeoObject> mPendingBitmaps;

	private LatLng mLatLng;

	private boolean mAttached;

	private static Handler sHandler = new Handler(Looper.getMainLooper());

	public WorldGoogleMapModule() {
		mPendingBitmaps = new PendingBitmapsToBeLoaded<GeoObject>();
		mAttached = false;
	}

	public WorldGoogleMapModule(GoogleMap map) {
		this();
		mMap = map;
	}

	public WorldGoogleMapModule(int iconSize) {
		this();
		mIconSize = iconSize;
	}

	/**
	 * Set the size of the marker icons in pixels
	 * 
	 * @param iconSize
	 * @return The instance itself
	 */
	public WorldGoogleMapModule setMarkerIconSize(int iconSize) {
		mIconSize = iconSize;
		return this;
	}

	protected BitmapCache createBitmapCache() {
		return mWorld.getBitmapCache().newCache(getClass().getName(), true);
	}

	protected void addGooGleMapModule(BeyondarObject beyondarObject) {
		if (beyondarObject instanceof GeoObject) {
			if (!beyondarObject.containsAnyModule(GeoObjectGoogleMapModuleImpl.class)) {
				GeoObjectGoogleMapModule module = new GeoObjectGoogleMapModuleImpl(this);
				beyondarObject.addModule(module);
				createMarker((GeoObject) beyondarObject, module);
			}
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
	public WorldGoogleMapModule setGoogleMap(GoogleMap map) {
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
				if (beyondarObject instanceof GeoObject) {
					createMarker((GeoObject) beyondarObject);
				}
			}
		}
	}

	protected void createMarker(GeoObject geoObject) {
		createMarker(geoObject,
				(GeoObjectGoogleMapModule) geoObject.getFirstModule(GeoObjectGoogleMapModule.class));
	}

	protected void createMarker(GeoObject geoObject, GeoObjectGoogleMapModule module) {
		if (geoObject == null || module == null) {
			return;
		}
		Marker marker = module.getMarker();
		if (marker != null) {
			marker.remove();
		}

		if (mMap == null) {
			return;
		}
		MarkerOptions markerOptions = createMarkerOptions(geoObject, module);
		if (markerOptions != null) {
			marker = mMap.addMarker(markerOptions);
			module.setMarker(marker);
		}
	}

	protected MarkerOptions createMarkerOptions(GeoObject geoObject, GeoObjectGoogleMapModule module) {
		if (geoObject == null || module == null) {
			return null;
		}
		Bitmap btm = getBitmap(geoObject);

		if (btm == null) {
			// TODO: add somehow the default bitmap from the list
		}
		return module.createMarkerOptions(btm);

	}

	protected MarkerOptions createMarkerOptions(GeoObject geoObject) {
		if (geoObject == null) {
			return null;
		}
		GeoObjectGoogleMapModule module = (GeoObjectGoogleMapModule) geoObject
				.getFirstModule(GeoObjectGoogleMapModule.class);

		return createMarkerOptions(geoObject, module);
	}

	private Bitmap getBitmap(GeoObject geoObject) {
		boolean canRemove = !mPendingBitmaps.existPendingList(geoObject.getBitmapUri());
		if (!mCache.isImageLoaded(geoObject.getBitmapUri())) {
			mPendingBitmaps.addObject(geoObject.getBitmapUri(), geoObject);
		}
		Bitmap btm = mCache.getBitmap(geoObject.getBitmapUri());

		if (btm == null) {
			return null;
		} else if (canRemove) {
			mPendingBitmaps.removePendingList(geoObject.getBitmapUri());
		}

		return resizeBitmap(geoObject.getBitmapUri(), btm);
	}

	public void setMarkerImage(Marker marker, GeoObject geoObject) {
		if (marker == null || geoObject == null) {
			return;
		}
		Bitmap btm = getBitmap(geoObject);
		if (btm == null) {
			// TODO: add somehow the default bitmap from the list
		}
		if (btm != null) {
			marker.setIcon(BitmapDescriptorFactory.fromBitmap(btm));
		}
	}

	protected Bitmap resizeBitmap(String uri, Bitmap btm) {
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
		final Bitmap resizedBtm = resizeBitmap(url, btm);
		ArrayList<GeoObject> list = mPendingBitmaps.getPendingList(url);
		for (int i = 0; i < list.size(); i++) {
			GeoObject gogm = list.get(i);

			final GeoObjectGoogleMapModule module = (GeoObjectGoogleMapModule) gogm
					.getFirstModule(GeoObjectGoogleMapModule.class);
			if (module != null) {
				sHandler.post(new Runnable() {
					@Override
					public void run() {
						if (module.isAttached() && resizedBtm != null) {
							module.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(resizedBtm));
						}
					}
				});
			}
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
		mAttached = true;

		addModuleToAllObjects();

		createMarkers();
	}

	private void addModuleToAllObjects() {
		ArrayList<BeyondarObjectList> beyondARLists = mWorld.getBeyondarObjectLists();
		for (BeyondarObjectList list : beyondARLists) {
			for (BeyondarObject beyondarObject : list) {
				addGooGleMapModule(beyondarObject);
			}
		}
	}

	@Override
	public void onDetached() {
		mAttached = false;
		mCache.clean();
	}

	@Override
	public boolean isAttached() {
		return mAttached;
	}

	@Override
	public void onBeyondarObjectAdded(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
		addGooGleMapModule(beyondarObject);
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
	public void onGeoPositionChanged(double latitude, double longitude, double altitude) {
	}
}
