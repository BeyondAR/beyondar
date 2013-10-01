package com.beyondar.example;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.beyondar.android.world.GoogleMapModule;
import com.beyondar.android.world.World;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapActivity extends FragmentActivity implements OnMarkerClickListener {

	private GoogleMap map;
	private GoogleMapModule mGoogleMapModule;
	private World mWorld;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_google);

		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		// We create the world...
		mWorld = new World(this);
		// ...And fill it
		WorldHelper.generateObjects(mWorld);

		// As we want to use GoogleMaps, we are going to create the module and
		// attach it to the World
		mGoogleMapModule = new GoogleMapModule();
		// Then we need to set the map in to the GoogleMapModule
		mGoogleMapModule.setGoogleMap(map);
		// Now that we have the module created let's add it in to our world
		mWorld.addModule(mGoogleMapModule);
		
		map.setOnMarkerClickListener(this);

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(mGoogleMapModule.getLatLng(), 15));
		map.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		return false;
	}
}
