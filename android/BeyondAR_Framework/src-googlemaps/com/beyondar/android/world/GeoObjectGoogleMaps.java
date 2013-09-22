package com.beyondar.android.world;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeoObjectGoogleMaps extends GeoObject {

	private MarkerOptions mMarkerOptions;
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
		// TODO Auto-generated method stub
		super.setGeoPosition(latitude, longitude, altitude);
	}

	protected void updateMarker() {
		// implementar listener per a que notifiki el marker del google maps
		// http://www.vogella.com/articles/AndroidGoogleMaps/article.html
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

	void setMarker(Marker marker) {
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
