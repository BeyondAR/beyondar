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
package com.beyondar.example;

import android.annotation.SuppressLint;
import android.content.Context;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

@SuppressLint("SdCardPath")
public class CustomWorldHelper {
	public static final int LIST_TYPE_EXAMPLE_1 = 1;

	public static World sharedWorld;

	public static World generateObjects(Context context) {
		if (sharedWorld != null) {
			return sharedWorld;
		}
		sharedWorld = new World(context);

		// The user can set the default bitmap. This is useful if you are
		// loading images form Internet and the connection get lost
		sharedWorld.setDefaultImage(R.drawable.beyondar_default_unknow_icon);

		// User position (you can change it using the GPS listeners form Android
		// API)
		sharedWorld.setGeoPosition(41.90533734214473d, 2.565848038959814d);

		// Create an object with an image in the app resources.
		GeoObject go1 = new GeoObject(1l);
		go1.setGeoPosition(41.90523339794433d, 2.565036406654116d);
		go1.setImageResource(R.drawable.creature_1);
		go1.setName("Creature 1");

		// Is it also possible to load the image asynchronously form internet
		GeoObject go2 = new GeoObject(2l);
		go2.setGeoPosition(41.90518966360719d, 2.56582424468222d);
		go2.setImageUri("http://beyondar.github.io/beyondar/images/logo_512.png");
		go2.setName("Online image");

		// Also possible to get images from the SDcard
		GeoObject go3 = new GeoObject(3l);
		go3.setGeoPosition(41.90550959641445d, 2.565873388087619d);
		go3.setImageUri("/sdcard/someImageInYourSDcard.jpeg");
		go3.setName("IronMan from sdcard");

		// And the same goes for the app assets
		GeoObject go4 = new GeoObject(4l);
		go4.setGeoPosition(41.90518862002349d, 2.565662767707665d);
		go4.setImageUri("assets://creature_7.png");
		go4.setName("Image from assets");

		GeoObject go5 = new GeoObject(5l);
		go5.setGeoPosition(41.90553066234138d, 2.565777906882577d);
		go5.setImageResource(R.drawable.creature_5);
		go5.setName("Creature 5");

		GeoObject go6 = new GeoObject(6l);
		go6.setGeoPosition(41.90596218466268d, 2.565250806050688d);
		go6.setImageResource(R.drawable.creature_6);
		go6.setName("Creature 6");

		GeoObject go7 = new GeoObject(7l);
		go7.setGeoPosition(41.90581776104766d, 2.565932313852319d);
		go7.setImageResource(R.drawable.creature_2);
		go7.setName("Creature 2");

		GeoObject go8 = new GeoObject(8l);
		go8.setGeoPosition(41.90534261025682d, 2.566164369775198d);
		go8.setImageResource(R.drawable.rectangle);
		go8.setName("Object 8");

		GeoObject go9 = new GeoObject(9l);
		go9.setGeoPosition(41.90530734214473d, 2.565808038959814d);
		go9.setImageResource(R.drawable.creature_4);
		go9.setName("Creature 4");
		
		
		GeoObject go10 = new GeoObject(10l);
		go10.setGeoPosition(42.006667d, 2.705d);
		go10.setImageResource(R.drawable.object_stuff);
		go10.setName("Far away");
		
		// Add the GeoObjects to the world
		sharedWorld.addBeyondarObject(go1);
		sharedWorld.addBeyondarObject(go2, LIST_TYPE_EXAMPLE_1);
		sharedWorld.addBeyondarObject(go3);
		sharedWorld.addBeyondarObject(go4);
		sharedWorld.addBeyondarObject(go5);
		sharedWorld.addBeyondarObject(go6);
		sharedWorld.addBeyondarObject(go7);
		sharedWorld.addBeyondarObject(go8);
		sharedWorld.addBeyondarObject(go9);
		sharedWorld.addBeyondarObject(go10);

		return sharedWorld;
	}

}
