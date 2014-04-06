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
package com.beyondar.android.sensor;

import java.util.ArrayList;
import java.util.List;

import com.beyondar.android.opengl.util.LowPassFilter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * This class controls and filter the sensor data provided by the magnetic and
 * the accelerometer sensors.
 */
public class BeyondarSensorManager {

	/**
	 * Default sensor delay.
	 */
	public static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;

	private BeyondarSensorManager() {
	}

	static enum BeyondarSensorManagerSingleton implements SensorEventListener {
		INSTANCE;

		private Object mLock;
		private ArrayList<BeyondarSensorListener> mSensorsListener;
		private SensorManager mSensorManager;

		private float mAccelerometerValues[] = new float[3];
		private float mMagneticValues[] = new float[3];

		private boolean isListenerRegistered;

		BeyondarSensorManagerSingleton() {
			mLock = new Object();
			mSensorsListener = new ArrayList<BeyondarSensorListener>();
			isListenerRegistered = false;
		}

		void setSensorManager(SensorManager sensorManager) {
			if (sensorManager == null) {
				return;
			}
			mSensorManager = sensorManager;
		}

		void registerSensor() {
			if (isListenerRegistered || mSensorManager == null)
				return;
			List<Sensor> listSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (listSensors.size() > 0) {
				mSensorManager.registerListener(INSTANCE, listSensors.get(0), SENSOR_DELAY);
			}

			listSensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (listSensors.size() > 0) {
				mSensorManager.registerListener(INSTANCE, listSensors.get(0), SENSOR_DELAY);
			}
		}

		void unregisterSensor() {
			if (!isListenerRegistered)
				return;
			List<Sensor> listSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (listSensors.size() > 0) {
				mSensorManager.unregisterListener(INSTANCE, listSensors.get(0));
			}

			listSensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (listSensors.size() > 0) {
				mSensorManager.unregisterListener(INSTANCE, listSensors.get(0));
			}
		}

		void registerSensorListener(BeyondarSensorListener sensorEventListener) {
			synchronized (mLock) {
				if (mSensorsListener.size() == 0) {
					registerSensor();
				}
				mSensorsListener.add(sensorEventListener);
			}
		}

		void unregisterSensorListener(BeyondarSensorListener sensorEventListener) {
			synchronized (mLock) {
				mSensorsListener.remove(sensorEventListener);
				if (mSensorsListener.size() == 0) {
					unregisterSensor();
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			int type = event.sensor.getType();
			float[] values = null;

			switch (type) {
			case Sensor.TYPE_ACCELEROMETER:
				LowPassFilter.filter(event.values, mAccelerometerValues);
				values = mAccelerometerValues;
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				LowPassFilter.filter(event.values, mMagneticValues);
				values = mMagneticValues;
				break;
			default:
				break;
			}
			if (values == null)
				return;

			synchronized (mLock) {
				for (int i = 0; i < mSensorsListener.size(); i++) {
					mSensorsListener.get(i).onSensorChanged(values, event);
				}
			}
		}
	}

	/**
	 * Add a new {@link BeyondarSensorListener} to the sensor manager.
	 * 
	 * @param beyondarSensorListener
	 */
	public static void registerSensorListener(BeyondarSensorListener beyondarSensorListener) {
		BeyondarSensorManagerSingleton.INSTANCE.registerSensorListener(beyondarSensorListener);
	}

	/**
	 * Remove the existing {@link BeyondarSensorListener} from the sensor
	 * manager.
	 * 
	 * @param sensorEventListener
	 */
	public static void unregisterSensorListener(BeyondarSensorListener sensorEventListener) {
		BeyondarSensorManagerSingleton.INSTANCE.unregisterSensorListener(sensorEventListener);
	}

	/**
	 * This method will unregister the {@link BeyondarSensorManager} form the
	 * android {@link SensorManager}, so all the {@link BeyondarSensorListener}
	 * will stop receiving notifications when the sensor changes. This will also
	 * decrease the battery consumption.
	 * 
	 * @param sensorManager
	 */
	public static void pause(SensorManager sensorManager) {
		BeyondarSensorManagerSingleton.INSTANCE.setSensorManager(sensorManager);
		BeyondarSensorManagerSingleton.INSTANCE.unregisterSensor();
	}

	/**
	 * This method will register the {@link BeyondarSensorManager} in the
	 * android {@link SensorManager}, so when a new value comes it will filter
	 * it and send it to all the {@link BeyondarSensorListener}.
	 * 
	 * @param sensorManager
	 */
	public static void resume(SensorManager sensorManager) {
		BeyondarSensorManagerSingleton.INSTANCE.setSensorManager(sensorManager);
		BeyondarSensorManagerSingleton.INSTANCE.registerSensor();
	}

}
