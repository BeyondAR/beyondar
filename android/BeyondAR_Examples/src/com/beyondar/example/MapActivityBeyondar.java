package com.beyondar.example;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.beyondar.android.world.WorldGoogleMaps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class MapActivityBeyondar extends FragmentActivity {

	private GoogleMap map;
	private WorldGoogleMaps mWorld;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_google);

		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		// We create the world...
		mWorld = new WorldGoogleMaps(this);
		// ...And fill it
		WorldFactory.generateObjects(mWorld);
		// We also need to set the GoogleMap to create the markers
		mWorld.setGoogleMap(map);

	}
}
