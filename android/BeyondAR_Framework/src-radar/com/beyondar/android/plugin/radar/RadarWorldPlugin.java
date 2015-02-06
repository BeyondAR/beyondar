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

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.beyondar.android.plugin.WorldPlugin;
import com.beyondar.android.sensor.BeyondarSensorListener;
import com.beyondar.android.sensor.BeyondarSensorManager;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

public class RadarWorldPlugin implements WorldPlugin, BeyondarSensorListener {
	public static final String VERSION = "0.9.1";

	private boolean mAttached = false;
	private Context mContex;
	private Display mDisplay;
	private World mWorld;
	private RadarView mRadarView;

	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];
	private float currentDegree = 0;
	private int mRotation;

	private double mMaxDistance = -1;

	private HashMap<Integer, Integer> mColorMaping;
	private HashMap<Integer, Float> mRadiusMaping;
	
	public RadarWorldPlugin(Context context) {
		mColorMaping = new HashMap<Integer, Integer>();
		mRadiusMaping = new HashMap<Integer, Float>();
		mContex = context;
		
		mDisplay = ((WindowManager) mContex.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
	}

	World getWorld() {
		return mWorld;
	}

	@Override
	public void onDetached() {
		BeyondarSensorManager.unregisterSensorListener(this);
		mAttached = false;
	}

	@Override
	public boolean isAttached() {
		return mAttached;
	}

	public void setup(World world) {
		mAttached = true;
		
		mWorld = world;

		addPluginToAllObjects();

		BeyondarSensorManager.registerSensorListener(this);
		
		mRotation = mDisplay.getRotation();
	}

	/**
	 * Set the color for a specific list type. Use the {@link Color} class to
	 * help you with that task.
	 * 
	 * @param listType
	 *            The list type that will have the specified color.
	 * @param color
	 *            The color for the dots.
	 */
	public void setListColor(int listType, int color) {
		mColorMaping.put(listType, color);
	}

	/**
	 * Set the size of the dot's radius in the radars in dp.
	 * 
	 * @param listType
	 *            The list type that will have the specified size
	 * @param size
	 *            The size in dp
	 */
	public void setListDotRadius(int listType, float size) {
		mRadiusMaping.put(listType, size);
	}

	public int getListColor(int listType) {
		if (mColorMaping.containsKey(listType)) {
			return mColorMaping.get(listType);
		}
		return RadarPointPlugin.DEFAULT_COLOR;
	}

	public float getListDotRadius(int listType) {
		if (mRadiusMaping.containsKey(listType)) {
			return mRadiusMaping.get(listType);
		}
		return RadarPointPlugin.DEFAULT_RADIUS_DP;
	}

	private void addPluginToAllObjects() {
		List<BeyondarObjectList> beyondARLists = mWorld.getBeyondarObjectLists();
		for (BeyondarObjectList list : beyondARLists) {
			for (BeyondarObject beyondarObject : list) {
				addRadarPointPlugin(beyondarObject, list.getType());
			}
		}
	}

	/**
	 * This method adds the {@link RadarPointPlugin} to the {@link com.beyondar.android.world.GeoObject GeoObject}
	 * 
	 * @param beyondarObject
	 */
	protected void addRadarPointPlugin(BeyondarObject beyondarObject, int listType) {
		if (beyondarObject instanceof GeoObject) {
			if (!beyondarObject.containsAnyPlugin(RadarPointPlugin.class)) {
				RadarPointPlugin plugin = new RadarPointPlugin(this, beyondarObject);
				if (mColorMaping.containsKey(listType)) {
					plugin.setColor(mColorMaping.get(listType));
				}
				if (mRadiusMaping.containsKey(listType)) {
					plugin.setRaduis(mRadiusMaping.get(listType));
				}
				beyondarObject.addPlugin(plugin);
			}
		}
	}

	public void setRadarView(RadarView radarView) {
		mRadarView = radarView;
		mRadarView.setRadarPlugin(this);
	}

	@Override
	public void onBeyondarObjectAdded(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
		addRadarPointPlugin(beyondarObject, beyondarObjectList.getType());
	}

	@Override
	public void onBeyondarObjectRemoved(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
	}

	@Override
	public void onBeyondarObjectListCreated(BeyondarObjectList beyondarObjectList) {
	}

	@Override
	public void onWorldCleaned() {
		mColorMaping.clear();
		mRadiusMaping.clear();
	}

	@Override
	public void onGeoPositionChanged(double latitude, double longitude, double altitude) {
	}

	@Override
	public void onDefaultImageChanged(String uri) {
	}

	@Override
	public void onSensorChanged(float[] filteredValues, SensorEvent event) {
		if (mRadarView == null)
			return;
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			mLastAccelerometer = filteredValues;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			mLastMagnetometer = filteredValues;
			break;
		}
		if (mLastAccelerometer == null || mLastMagnetometer == null)
			return;

		boolean success = SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
		SensorManager.getOrientation(mR, mOrientation);
		if (success)
			rotateView((float) Math.toDegrees(mOrientation[0]));

	}

	/**
	 * Set the max distance rendered by the view in meters
	 * 
	 * @param maxDistance
	 */
	public void setMaxDistance(double maxDistance) {
		mMaxDistance = maxDistance;
	}

	/**
	 * Get the max distance rendered by the view in meters
	 * 
	 * @return max distance to render the {@link com.beyondar.android.world.GeoObject GeoObject}s
	 */
	public double getMaxDistance() {
		return mMaxDistance;
	}

	private void rotateView(float degree) {
		
		degree += mRotation;
		
		// create a rotation animation (reverse turn degree degrees)
		RotateAnimation animation = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

		// how long the animation will take place
		animation.setDuration(210);

		// set the animation after the end of the reservation status
		animation.setFillAfter(true);

		// Start the animation
		mRadarView.startAnimation(animation);
		currentDegree = -degree;
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onResume() {
		switch (mDisplay.getRotation()) {
		case Surface.ROTATION_0:
			mRotation = 0;
			break;
		case Surface.ROTATION_90:
			mRotation = 90;
			break;
		case Surface.ROTATION_180:
			mRotation = 0;
			break;
		case Surface.ROTATION_270:
			mRotation = -90;
			break;
		}		
	}

}
