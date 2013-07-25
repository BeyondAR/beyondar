package com.beyondar.android.world.objects;

import android.location.Location;

import com.beyondar.android.util.Constants;
import com.beyondar.android.util.math.Distance;
import com.google.android.maps.GeoPoint;

public class GeoObject extends BeyondarObject {

	protected double mLongitude;
	protected double mLatitude;
	protected double mAltitude;

	protected GeoPoint mGeoPoint;

	public GeoObject(long id) {
		super(id);

		setVisibile(true);

	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public double getAltitude() {
		return mAltitude;
	}

	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;

	}

	public int getLatitudeE6() {
		return (int) (mLatitude * Constants.E6);
	}

	public int getLongitudeE6() {
		return (int) (mLongitude * Constants.E6);
	}

	public void setLocation(Location location) {

		if (location == null) {
			return;
		}
		setLatitude(location.getLatitude());
		setLongitude(location.getLongitude());
		setAltitude(location.getAltitude());
	}

	public double calculateDistanceMeters(GeoObject geo) {
		return calculateDistanceMeters(geo.getLongitude(), geo.getLatitude());
	}

	public double calculateDistanceMeters(double longitude, double latitude) {
		return Distance.calculateDistanceMeters(getLongitude(), getLatitude(),
				longitude, latitude);
	}

	public GeoPoint getGeoPoint() {
		if (mGeoPoint == null) {
			mGeoPoint = new GeoPoint(getLatitudeE6(), getLongitudeE6());
			return mGeoPoint;
		}

		if (getLatitudeE6() == mGeoPoint.getLatitudeE6()
				&& getLongitudeE6() == mGeoPoint.getLongitudeE6()) {
			return mGeoPoint;
		}
		mGeoPoint = new GeoPoint(getLatitudeE6(), getLongitudeE6());
		return mGeoPoint;
	}

}