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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.beyondar.android.plugin.googlemap.GoogleMapWorldPlugin;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapActivity extends FragmentActivity implements OnMarkerClickListener {

	private GoogleMap mMap;
	private GoogleMapWorldPlugin mGoogleMapPlugin;
	private World mWorld;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_google);

		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (mMap == null){
			return;
		}

		// We create the world and fill the world
		mWorld = CustomWorldHelper.generateObjects(this);

		// As we want to use GoogleMaps, we are going to create the plugin and
		// attach it to the World
		mGoogleMapPlugin = new GoogleMapWorldPlugin(this);
		// Then we need to set the map in to the GoogleMapPlugin
		mGoogleMapPlugin.setGoogleMap(mMap);
		// Now that we have the plugin created let's add it to our world.
		// NOTE: It is better to load the plugins before start adding object in to the world.
		mWorld.addPlugin(mGoogleMapPlugin);

		mMap.setOnMarkerClickListener(this);

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mGoogleMapPlugin.getLatLng(), 15));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);
		
		// Lets add the user position
		GeoObject user = new GeoObject(1000l);
		user.setGeoPosition(mWorld.getLatitude(), mWorld.getLongitude());
		user.setImageResource(R.drawable.flag);
		user.setName("User position");
		mWorld.addBeyondarObject(user);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// To get the GeoObject that owns the marker we use the following
		// method:
		GeoObject geoObject = mGoogleMapPlugin.getGeoObjectOwner(marker);
		if (geoObject != null) {
			Toast.makeText(this,
					"Click on a marker owned by a GeoOject with the name: " + geoObject.getName(),
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}
}
