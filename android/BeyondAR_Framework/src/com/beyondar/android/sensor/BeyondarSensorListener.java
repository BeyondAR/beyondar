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

import android.hardware.SensorEvent;

/**
 * This interface will help you getting the needed values from the accelerometer
 * and the magnetic field already filtered. <br>
 * Example of usage:
 * 
 * <pre>
 *  {@code
 * public void onSensorChanged(float[] filteredValues, SensorEvent event) {
 * 		if (mRadarView == null)
 * 			return;
 * 		switch (event.sensor.getType()) {
 * 		case Sensor.TYPE_ACCELEROMETER:
 * 			mLastAccelerometer = filteredValues;
 * 			break;
 * 		case Sensor.TYPE_MAGNETIC_FIELD:
 * 			mLastMagnetometer = filteredValues;
 * 			break;
 * 		}
 * 		if (mLastAccelerometer == null || mLastMagnetometer == null)
 * 			return;
 * 		
 * 		boolean success = SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
 * 		SensorManager.getOrientation(mR, mOrientation);
 * 		if (success)
 * 			rotateView((float) Math.toDegrees(mOrientation[0]));
 * 
 * 	}
 * </pre>
 */
public interface BeyondarSensorListener {

	/**
	 * Listener for sensor changes. The filteredValues are already filtered
	 * (using a low pass filter) and ready to be used.
	 * 
	 * @param filteredValues
	 *            The value of the sensor already filtered with a low pass
	 *            filter.
	 * @param event
	 *            The original {@link SensorEvent}.
	 */
	public void onSensorChanged(float[] filteredValues, SensorEvent event);
}
