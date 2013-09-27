package com.beyondar.example;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.beyondar.android.world.WorldGoogleMaps;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapActivity extends FragmentActivity implements OnMarkerClickListener {

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
		WorldHelper.generateObjects(mWorld);
		// We also need to set the GoogleMap to create the markers (And that's it)
		mWorld.setGoogleMap(map);
		
		map.setOnMarkerClickListener(this);
		
		// Move the camera instantly to hamburg with a zoom of 15.
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(mWorld.getLatLng(), 15));

	    // Zoom in, animating the camera.
	    map.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		return false;
	}
}
