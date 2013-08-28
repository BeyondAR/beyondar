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

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.beyondar.android.util.Constants;
import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.objects.GeoObject;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GeoObjectOverlay extends Overlay {

	private int mIconSize;

	protected ArrayList<BeyondarObjectList> mListsToDisplay;
	private BitmapCache mCache;
	// private HashMap<String, Bitmap> mOverlayBitmap;

	private double mLongitude, mLatitude;
	private double mRadiusSize;

	private World_google_maps mWorld;

	public GeoObjectOverlay(int iconSize, World_google_maps world) {
		mIconSize = iconSize;
		mWorld = world;
		mCache = createBitmapCache();
		// mOverlayBitmap = new HashMap<String, Bitmap>();
		mListsToDisplay = new ArrayList<BeyondarObjectList>();
	}

	protected BitmapCache createBitmapCache() {
		return mWorld.getBitmapCache().newCache(getClass().getName(), true);
	}

	protected World_google_maps getWorld() {
		return mWorld;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// Debug.startMethodTracing("calc");
		setRadius(mWorld.getMapViewDistance(), mWorld.getLongitude(), mWorld.getLatitude());
		for (int i = 0; i < mListsToDisplay.size(); i++) {
			drawList(canvas, mapView, shadow, mListsToDisplay.get(i), mRadiusSize);
		}
		// Debug.stopMethodTracing();
	}

	private void setRadius(double size, double longitude, double latitude) {
		mLongitude = longitude;
		mLatitude = latitude;
		mRadiusSize = size;
	}

	public void addList(int type) {
		BeyondarObjectList list = mWorld.getBeyondarObjectList(type);
		addList(list);
	}

	public void addList(BeyondarObjectList list) {
		if (list != null) {
			mListsToDisplay.add(list);
		}
	}

	public void removeList(int type) {
		BeyondarObjectList list = mWorld.getBeyondarObjectList(type);
		removeList(list);
	}

	public void removeList(BeyondarObjectList list) {
		mListsToDisplay.remove(list);
	}

	protected void drawList(Canvas canvas, MapView mapView, boolean shadow,
			BeyondarObjectList beyondarList, double radius) {
		if (beyondarList == null) {
			return;
		}

		for (int i = 0; i < beyondarList.size(); i++) {
			if (beyondarList.get(i).isVisible()) {
				if (!(beyondarList.get(i) instanceof GeoObject)) {
					continue;
				}

				GeoObject go = (GeoObject) beyondarList.get(i);

				double dst = go.calculateDistanceMeters(mLongitude, mLatitude);
				if (radius <= 0 || dst < radius) {
					GeoObject geoObject = go;

					Bitmap btm = getBitmap(geoObject, beyondarList.getDefaultBitmapURI());

					if (null != btm) {
						if (btm.isRecycled()) {
							Log.e(Constants.TAG,
									"Trying to use a recicled bitmap for the default list bitmap on the GeoObjectOverlay: "
											+ beyondarList.getDefaultBitmapURI());
						}
						drawGeoObject(canvas, mapView, geoObject, btm);
					}
				}
			}
		}
	}

	private Point screenCoords = new Point();

	protected void drawGeoObject(Canvas canvas, MapView mapView, GeoObject geooBject, Bitmap btm) {

		mapView.getProjection().toPixels(geooBject.getGeoPoint(), screenCoords);
		canvas.drawBitmap(btm, screenCoords.x - btm.getWidth() / 2,
				screenCoords.y - btm.getHeight() / 2, null);
	}

	protected Bitmap getBitmap(GeoObject geoObject, String uriDefaultBitmap) {
		Bitmap btm = getBitmap(geoObject.getBitmapUri());

		if (btm != null) {
			return btm;
		}

		btm = getBitmap(uriDefaultBitmap);
		return btm;
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

	protected BitmapCache getBitmapCache() {
		return mCache;
	}

};