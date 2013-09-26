package com.beyondar.example;

import com.beyondar.android.world.GeoObjectGoogleMaps;
import com.beyondar.android.world.World;

public class WorldHelper {

	public static void generateObjects(World world) {
		// User position (you can change it using the GPS listeners form Android
		// API)
		world.setLongitude(1.925848038959814d);
		world.setLatitude(41.26533734214473d);

		// Create an object with an image in the app resources.
		GeoObjectGoogleMaps go1 = new GeoObjectGoogleMaps(1l);
		go1.setLongitude(1.926036406654116d);
		go1.setLatitude(41.26523339794433d);
		go1.setImageResource(R.drawable.creature_1);
		go1.setName("Creature 1");

		// Is it also possible to load the image dynamically form internet
		GeoObjectGoogleMaps go2 = new GeoObjectGoogleMaps(2l);
		go2.setLongitude(1.92582424468222d);
		go2.setLatitude(41.26518966360719d);
		go2.setImageUri("http://beyondar.com/sites/default/files/logo_reduced.png");
		go2.setName("Online image");

		// Also possible to get images from the SDcard
		GeoObjectGoogleMaps go3 = new GeoObjectGoogleMaps(3l);
		go3.setLongitude(1.925873388087619d);
		go3.setLatitude(41.26550959641445d);
		go3.setImageUri("/sdcard/TheAvengers_IronMan.jpeg");
		go3.setName("IronMan from sdcard");

		// And the same goes for the app assets
		GeoObjectGoogleMaps go4 = new GeoObjectGoogleMaps(4l);
		go4.setLongitude(1.925662767707665d);
		go4.setLatitude(41.26518862002349d);
		go4.setImageUri("assets://creature_7.png");
		go4.setName("Image from assets");

		GeoObjectGoogleMaps go5 = new GeoObjectGoogleMaps(5l);
		go5.setLongitude(1.925777906882577d);
		go5.setLatitude(41.26553066234138d);
		go5.setImageResource(R.drawable.creature_5);
		go5.setName("Creature 5");

		GeoObjectGoogleMaps go6 = new GeoObjectGoogleMaps(6l);
		go6.setLongitude(1.925250806050688d);
		go6.setLatitude(41.26496218466268d);
		go6.setImageResource(R.drawable.creature_6);
		go6.setName("Creature 6");

		GeoObjectGoogleMaps go7 = new GeoObjectGoogleMaps(7l);
		go7.setLongitude(1.925932313852319d);
		go7.setLatitude(41.26581776104766d);
		go7.setImageResource(R.drawable.creature_2);
		go7.setName("Creature 2");

		GeoObjectGoogleMaps go8 = new GeoObjectGoogleMaps(8l);
		go8.setLongitude(1.926164369775198d);
		go8.setLatitude(41.26534261025682d);
		go8.setImageResource(R.drawable.image_test_pow2_small);
		go8.setName("Object 8");

		world.addBeyondarObject(go1);
		world.addBeyondarObject(go2);
		world.addBeyondarObject(go3);
		world.addBeyondarObject(go4);
		world.addBeyondarObject(go5);
		world.addBeyondarObject(go6);
		world.addBeyondarObject(go7);
		world.addBeyondarObject(go8);

		// The user can set the default bitmap. This is useful if you are
		// loading images form Internet and the connection is lots
		world.setDefaultBitmap(R.drawable.beyondar_default_unknow_icon);

	}

}
