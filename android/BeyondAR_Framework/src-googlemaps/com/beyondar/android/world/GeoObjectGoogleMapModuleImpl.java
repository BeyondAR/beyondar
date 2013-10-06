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

import android.graphics.Bitmap;

import com.beyondar.android.opengl.renderable.Renderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.world.module.GeoObjectGoogleMapModule;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeoObjectGoogleMapModuleImpl implements GeoObjectGoogleMapModule {

	private Marker mMarker;
	private LatLng mLatLng;
	private GeoObject mGeoObject;
	private boolean mAttached;

	public GeoObjectGoogleMapModuleImpl() {
		mAttached = false;
	}

	@Override
	public void setup(BeyondarObject beyondarObject) {
		if (beyondarObject instanceof GeoObject) {
			mGeoObject = (GeoObject) beyondarObject;
		}
		mAttached = true;
	}

	@Override
	public void onGeoPositionChanged(double latitude, double longitude, double altitude) {
		if (mMarker == null || mGeoObject == null) {
			return;
		}
		mMarker.setPosition(getLatLng());
	}

	public LatLng getLatLng() {
		if (mGeoObject == null){
			return null;
		}
		if (mLatLng == null) {
			mLatLng = new LatLng(mGeoObject.getLatitude(), mGeoObject.getLongitude());
			return mLatLng;
		}

		if (mLatLng.latitude == mGeoObject.getLatitude() && mLatLng.longitude == mGeoObject.getLongitude()) {
			return mLatLng;
		}

		mLatLng = new LatLng(mGeoObject.getLatitude(), mGeoObject.getLongitude());
		return mLatLng;
	}

	public void setMarker(Marker marker) {
		mMarker = marker;
	}

	public Marker getMarker() {
		return mMarker;
	}
	
	@Override
	public MarkerOptions createMarkerOptions(Bitmap bitmap) {
		if (mGeoObject == null){
			return null;
		}
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(mGeoObject.getName());
		markerOptions.position(getLatLng());

		if (bitmap != null) {
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		}
		return markerOptions;
	}
	
	@Override
	public void onAngleChanged(Point3 angle) {
	}

	@Override
	public void onPositionChanged(Point3 position) {
	}

	@Override
	public void onTextureChanged(Texture texture) {
	}

	@Override
	public void onRenderableChanged(Renderable openglObject) {
	}

	@Override
	public void onFaceToCameraChanged(boolean faceToCamera) {
	}

	@Override
	public void onVisibilityChanged(boolean visible) {
	}

	@Override
	public void onNameChanged(String name) {
		if (mMarker == null || mGeoObject == null) {
			return;
		}
		mMarker.setTitle(name);
	}

	@Override
	public void onImageUriChanged(String uri) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDetached() {
		mAttached = false;
		if (mMarker == null || mGeoObject == null) {
			return;
		}
		mMarker.remove();
	}
	@Override
	public boolean isAttached() {
		return mAttached;
	}
}
