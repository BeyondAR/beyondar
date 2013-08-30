package com.beyondar.example;

import com.beyondar.android.world.World;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MapActivityBeyondar extends FragmentActivity {

	private GoogleMap map;
	private World mWorld;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_google);

		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		
		mWorld = World.getWorld();

	}
}
