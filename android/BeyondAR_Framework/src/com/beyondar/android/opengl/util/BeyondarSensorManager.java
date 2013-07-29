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
package com.beyondar.android.opengl.util;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class BeyondarSensorManager {

	public static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;

	private static enum BeyondarSensorManagerSingleton implements SensorEventListener {
		INSTANCE;

		private Object mLock;
		private ArrayList<SensorEventListener> mSensorsListener;
		private SensorManager mSensorManager;

		private BeyondarSensorManagerSingleton() {
			mLock = new Object();
			mSensorsListener = new ArrayList<SensorEventListener>();
		}

		private void setSensorManager(SensorManager sensorManager) {
			mSensorManager = sensorManager;
			if (mSensorManager == null) {
				return;
			}

		}

		private void registerSensor() {
			List<Sensor> listSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (listSensors.size() > 0) {
				mSensorManager.registerListener(INSTANCE, listSensors.get(0), SENSOR_DELAY);
			}

			listSensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (listSensors.size() > 0) {
				mSensorManager.registerListener(INSTANCE, listSensors.get(0), SENSOR_DELAY);
			}
		}

		private void unregisterSensor() {
			List<Sensor> listSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (listSensors.size() > 0) {
				mSensorManager.unregisterListener(INSTANCE, listSensors.get(0));
			}

			listSensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (listSensors.size() > 0) {
				mSensorManager.unregisterListener(INSTANCE, listSensors.get(0));
			}
		}

		private void registerSensorListener(SensorEventListener sensorEventListener) {
			synchronized (mLock) {
				if (mSensorsListener.size() == 0) {
					registerSensor();
				}
				mSensorsListener.add(sensorEventListener);
			}
		}

		private void unregisterSensorListener(SensorEventListener sensorEventListener) {
			synchronized (mLock) {
				mSensorsListener.remove(sensorEventListener);
				if (mSensorsListener.size() == 0) {
					unregisterSensor();
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			synchronized (mLock) {
				for (int i = 0; i < mSensorsListener.size(); i++) {
					mSensorsListener.get(i).onAccuracyChanged(sensor, accuracy);
				}
			}
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			synchronized (mLock) {
				for (int i = 0; i < mSensorsListener.size(); i++) {
					mSensorsListener.get(i).onSensorChanged(event);
				}
			}
		}
	}

	public static void initializeSensors(SensorManager sensorManager) {
		BeyondarSensorManagerSingleton.INSTANCE.setSensorManager(sensorManager);
	}

	public static void registerSensorListener(SensorEventListener sensorEventListener) {
		BeyondarSensorManagerSingleton.INSTANCE.registerSensorListener(sensorEventListener);
	}

	public static void unregisterSensorListener(SensorEventListener sensorEventListener) {
		BeyondarSensorManagerSingleton.INSTANCE.unregisterSensorListener(sensorEventListener);
	}

}
