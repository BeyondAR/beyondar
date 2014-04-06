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
/* This code is based on Yasir.Ali <ali.yasir0@gmail.com> work. More on
 *  https://github.com/yasiralijaved/GenRadar
 */
package com.beyondar.android.plugin.radar;

import android.graphics.Color;
import android.location.Location;

import com.beyondar.android.opengl.renderable.Renderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.plugin.GeoObjectPlugin;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;

public class RadarPointPlugin implements GeoObjectPlugin {

	public static final int DEFAULT_COLOR = Color.BLUE;
	public static final float DEFAULT_RADIUS_DP = 1.5f;

	private RadarWorldPlugin mRadarWorldPlugin;
	private GeoObject mGeoObject;
	private boolean mAttached;

	private float mX;
	private float mY;
	private float mRaduis, mRaduisPixels;
	private int mColor;
	private float[] mDistanceArray = new float[1];

	public RadarPointPlugin(RadarWorldPlugin radarWorldPlugin, BeyondarObject beyondarObject) {
		mRadarWorldPlugin = radarWorldPlugin;
		mColor = DEFAULT_COLOR;
		mRaduis = DEFAULT_RADIUS_DP;
		mRaduisPixels = -1;

		setBeyondarObject(beyondarObject);
	}

	public void setBeyondarObject(BeyondarObject beyondarObject) {
		if (beyondarObject instanceof GeoObject) {
			mGeoObject = (GeoObject) beyondarObject;
		}
		mAttached = true;
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
	}

	@Override
	public void onImageUriChanged(String uri) {
	}

	@Override
	public void onDetached() {
		mAttached = false;
	}

	@Override
	public boolean isAttached() {
		return mAttached;
	}

	@Override
	public void onGeoPositionChanged(double latitude, double longitude, double altitude) {
		updateDistanceWithThisLocation();
	}

	@Override
	public GeoObject getGeoObject() {
		return mGeoObject;
	}

	private void updateDistanceWithThisLocation() {
		Location.distanceBetween(mRadarWorldPlugin.getWorld().getLatitude(), mRadarWorldPlugin.getWorld()
				.getLongitude(), mGeoObject.getLatitude(), mGeoObject.getLongitude(), mDistanceArray);
		mDistanceArray[0] = mDistanceArray[0] * 0.05f;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}

	public float getX() {
		return mX;
	}

	public void setX(float x) {
		mX = x;
	}

	public float getY() {
		return mY;
	}

	public void setY(float y) {
		mY = y;
	}

	public float getRaduis() {
		return mRaduis;
	}

	public void setRaduis(float raduis) {
		mRaduis = raduis;
		mRaduisPixels = -1;
	}

	float getRaduisPixels() {
		return mRaduisPixels;
	}

	void setRaduisPixels(float raduis) {
		mRaduisPixels = raduis;
	}

	@Override
	public BeyondarObject getbeyondarObject() {
		return getGeoObject();
	}
}
