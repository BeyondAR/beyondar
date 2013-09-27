package com.beyondar.android.world;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptionsCreator;

public class GeoObjectGoogleMaps extends GeoObject {

	private Marker mMarker;
	private LatLng mLatLng;

	public GeoObjectGoogleMaps(long id) {
		super(id);
	}

	@Override
	public void setGeoPosition(double latitude, double longitude) {
		super.setGeoPosition(latitude, longitude);
		updateMarker();

	}

	@Override
	public void setGeoPosition(double latitude, double longitude, double altitude) {
		super.setGeoPosition(latitude, longitude, altitude);
		updateMarker();
	}
	
//	@Override
//	public void setVisibile(boolean visible) {
//		super.setVisibile(visible);
//		if (mMarker == null){
//			return;
//		}
//		mMarker.setVisible(visible);
//	}

	protected void updateMarker() {
		if (mMarker == null){
			return;
		}
		mMarker.setPosition(getLatLng());
	}

	public LatLng getLatLng() {
		if (mLatLng == null) {
			mLatLng = new LatLng(getLatitude(), getLongitude());
			return mLatLng;
		}

		if (mLatLng.latitude == getLatitude() && mLatLng.longitude == getLongitude()) {
			return mLatLng;
		}

		mLatLng = new LatLng(getLatitude(), getLongitude());
		return mLatLng;
	}

	public void setMarker(Marker marker) {
		mMarker = marker;
	}

	/**
	 * Get the marker for the google maps API.
	 * 
	 * @return The marker used for this {@link GeoObject}, null if there is no
	 *         marker defined
	 */
	public Marker getMarker() {
		return mMarker;
	}
}
