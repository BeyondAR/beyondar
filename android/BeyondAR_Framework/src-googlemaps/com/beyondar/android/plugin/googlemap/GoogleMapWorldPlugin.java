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
package com.beyondar.android.plugin.googlemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

import com.beyondar.android.plugin.GeoObjectPlugin;
import com.beyondar.android.plugin.WorldPlugin;
import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.util.PendingBitmapsToBeLoaded;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapWorldPlugin implements WorldPlugin, BitmapCache.OnExternalBitmapLoadedCacheListener {

	/** Default icon size for the markers in dips */
	public static final int DEFAULT_ICON_SIZE_MARKER = 40;

	private World mWorld;
	private GoogleMap mMap;

	private BitmapCache mCache;
	private int mIconSize;
	private PendingBitmapsToBeLoaded<GeoObject> mPendingBitmaps;

	private HashMap<Marker, GoogleMapGeoObjectPlugin> mMarkerHashMap;

	private LatLng mLatLng;

	private boolean mAttached;

	private static Handler sHandler = new Handler(Looper.getMainLooper());

	private Context mContext;

	public GoogleMapWorldPlugin(Context context) {
		mMarkerHashMap = new HashMap<Marker, GoogleMapGeoObjectPlugin>();
		mPendingBitmaps = new PendingBitmapsToBeLoaded<GeoObject>();
		mAttached = false;
		mContext = context;
	}

	public GoogleMapWorldPlugin(Context context, GoogleMap map) {
		this(context);
		mMap = map;
	}

	/**
	 * Set the size of the marker icons in pixels
	 * 
	 * @param iconSize
	 * @return The instance itself
	 */
	public GoogleMapWorldPlugin setMarkerIconSize(int iconSize) {
		mIconSize = iconSize;
		return this;
	}

	protected BitmapCache createBitmapCache() {
		return mWorld.getBitmapCache().newCache(getClass().getName(), true);
	}

	/**
	 * This method adds the {@link GoogleMapGeoObjectPlugin} to the
	 * {@link com.beyondar.android.world.GeoObject GeoObject}
	 * 
	 * @param beyondarObject
	 */
	protected void addGooGleMapPlugin(BeyondarObject beyondarObject) {
		if (beyondarObject instanceof GeoObject) {
			if (!beyondarObject.containsAnyPlugin(GoogleMapGeoObjectPlugin.class)) {
				GoogleMapGeoObjectPlugin plugin = new GoogleMapGeoObjectPlugin(this, beyondarObject);
				beyondarObject.addPlugin(plugin);
				createMarker((GeoObject) beyondarObject, plugin);
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
	public GoogleMapWorldPlugin setGoogleMap(GoogleMap map) {
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
				(GoogleMapGeoObjectPlugin) geoObject.getFirstPlugin(GoogleMapGeoObjectPlugin.class));
	}

	protected void createMarker(GeoObject geoObject, GoogleMapGeoObjectPlugin plugin) {
		if (geoObject == null || plugin == null) {
			return;
		}
		Marker marker = plugin.getMarker();
		if (marker != null) {
			marker.remove();
		}

		if (mMap == null) {
			return;
		}
		MarkerOptions markerOptions = createMarkerOptions(geoObject, plugin);
		if (markerOptions != null) {
			marker = mMap.addMarker(markerOptions);
			plugin.setMarker(marker);
		}
	}

	public void registerMarker(Marker marker, GoogleMapGeoObjectPlugin plugin) {
		mMarkerHashMap.put(marker, plugin);
	}

	protected MarkerOptions createMarkerOptions(GeoObject geoObject, GoogleMapGeoObjectPlugin plugin) {
		if (geoObject == null || plugin == null) {
			return null;
		}
		Bitmap btm = getBitmapFromGeoObject(geoObject);

		return plugin.createMarkerOptions(btm);

	}

	protected MarkerOptions createMarkerOptions(GeoObject geoObject) {
		if (geoObject == null) {
			return null;
		}
		GoogleMapGeoObjectPlugin plugin = (GoogleMapGeoObjectPlugin) geoObject
				.getFirstPlugin(GoogleMapGeoObjectPlugin.class);

		return createMarkerOptions(geoObject, plugin);
	}

	private Bitmap getBitmapFromGeoObject(GeoObject geoObject) {
		if (geoObject.getImageUri() == null) {
			return null;
		}
		boolean canRemove = !mPendingBitmaps.existPendingList(geoObject.getImageUri());
		if (!mCache.isImageLoaded(geoObject.getImageUri())) {
			mPendingBitmaps.addObject(geoObject.getImageUri(), geoObject);
		}
		Bitmap btm = mCache.getBitmap(geoObject.getImageUri());

		if (btm == null) {
			String uri = mWorld.getDefaultImage(geoObject.getWorldListType());
			btm = mCache.getBitmap(uri);
		} else if (canRemove) {
			mPendingBitmaps.removePendingList(geoObject.getImageUri());
		}

		return resizeBitmap(geoObject.getImageUri(), btm);
	}

	public void setMarkerImage(Marker marker, GeoObject geoObject) {
		if (marker == null || geoObject == null) {
			return;
		}
		Bitmap btm = getBitmapFromGeoObject(geoObject);
		if (btm != null) {
			marker.setIcon(BitmapDescriptorFactory.fromBitmap(btm));
		}
	}

	protected Bitmap resizeBitmap(String uri, Bitmap btm) {
		if (btm == null || uri == null) {
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
		final Bitmap resizedBtm = resizeBitmap(url, btm);
		ArrayList<GeoObject> list = mPendingBitmaps.getPendingList(url);
		for (int i = 0; i < list.size(); i++) {
			GeoObject gogm = list.get(i);

			final GoogleMapGeoObjectPlugin plugin = (GoogleMapGeoObjectPlugin) gogm
					.getFirstPlugin(GoogleMapGeoObjectPlugin.class);
			if (plugin != null) {
				sHandler.post(new Runnable() {
					@Override
					public void run() {
						if (plugin.isAttached() && resizedBtm != null) {
							plugin.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(resizedBtm));
						}
					}
				});
			}
		}
	}

	@Override
	public void setup(World world) {
		mWorld = world;
		if (mIconSize == 0) {
			mIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					DEFAULT_ICON_SIZE_MARKER, mContext.getResources().getDisplayMetrics());
		}

		mCache = createBitmapCache();
		mCache.addOnExternalBitmapLoadedCahceListener(this);
		createMarkers();
		mAttached = true;

		addPluginToAllObjects();

		createMarkers();
	}

	private void addPluginToAllObjects() {
		List<BeyondarObjectList> beyondARLists = mWorld.getBeyondarObjectLists();
		for (BeyondarObjectList list : beyondARLists) {
			for (BeyondarObject beyondarObject : list) {
				addGooGleMapPlugin(beyondarObject);
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
		addGooGleMapPlugin(beyondarObject);
	}

	@Override
	public void onBeyondarObjectListCreated(BeyondarObjectList beyondarObjectList) {
	}

	@Override
	public void onBeyondarObjectRemoved(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
		if (beyondarObject instanceof GeoObject) {
			GeoObject geoObject = (GeoObject) beyondarObject;
			GoogleMapGeoObjectPlugin gogmMod = (GoogleMapGeoObjectPlugin) geoObject
					.getFirstPlugin(GoogleMapGeoObjectPlugin.class);
			if (gogmMod != null) {
				if (gogmMod.getMarker() != null) {
					mMarkerHashMap.remove(gogmMod.getMarker());
				}
			}
		}
	}

	/**
	 * Retrieve the {@link com.beyondar.android.world.GeoObject GeoObject} that
	 * owns an specific {@link Marker}
	 * 
	 * @param marker
	 *            The Marker that whant's to be checked
	 * @return The {@link com.beyondar.android.world.GeoObject GeoObject} owner
	 *         or null if there is no owner
	 */
	public GeoObject getGeoObjectOwner(Marker marker) {
		GeoObjectPlugin geoObjectPlugin = mMarkerHashMap.get(marker);
		if (geoObjectPlugin != null) {
			return geoObjectPlugin.getGeoObject();
		}
		return null;
	}

	@Override
	public void onWorldCleaned() {
		mMarkerHashMap.clear();
		mPendingBitmaps.clear();
	}

	@Override
	public void onGeoPositionChanged(double latitude, double longitude, double altitude) {
	}

	@Override
	public void onDefaultImageChanged(String uri) {
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onResume() {
	}
}
