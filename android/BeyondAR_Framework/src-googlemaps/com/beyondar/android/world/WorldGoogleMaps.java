package com.beyondar.android.world;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WorldGoogleMaps extends World {

	private GoogleMap mMap;

	private static World sWorld;

	/**
	 * Get the world
	 * 
	 * @return
	 */
	public static World getWorld() {
		return sWorld;
	}

	/**
	 * This method helps you to create an unique world class
	 * 
	 * @param world
	 */
	public static void setWorld(World world) {
		sWorld = world;
	}

	public WorldGoogleMaps(Context context) {
		super(context);
	}

	@Override
	public synchronized void addBeyondarObject(BeyondarObject beyondarObject) {
		super.addBeyondarObject(beyondarObject);
	}

	@Override
	public synchronized void addBeyondarObject(BeyondarObject beyondarObject, int worldListType) {
		super.addBeyondarObject(beyondarObject, worldListType);
	}

	public void setGoogleMap(GoogleMap map) {
		mMap = map;
		createMarkers();
	}

	public void createMarkers() {
		for (int i = 0; i < getBeyondarObjectLists().size(); i++) {
			BeyondarObjectList list = getBeyondarObjectList(i);
			for (int j = 0; j < list.size(); j++) {
				BeyondarObject beyondarObject = list.get(j);
				if (beyondarObject instanceof GeoObjectGoogleMaps) {
					createMarker((GeoObjectGoogleMaps) beyondarObject);
				}
			}
		}
	}

	protected void createMarker(GeoObjectGoogleMaps geoObjectGoogleMaps) {
		Marker marker = geoObjectGoogleMaps.getMarker();
		if (marker != null) {
			marker.remove();
		}

		marker = mMap.addMarker(generateMarkerOptions(geoObjectGoogleMaps));
		geoObjectGoogleMaps.setMarker(marker);
	}

	protected MarkerOptions generateMarkerOptions(GeoObjectGoogleMaps geoObject) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(geoObject.getName());
		markerOptions.position(geoObject.getLatLng());
		//TODO: improve this part
		if (getBitmapCache().getBitmap(geoObject.getBitmapUri()) != null) {
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapCache().getBitmap(
					geoObject.getBitmapUri())));
		}
		return markerOptions;
	}
}
