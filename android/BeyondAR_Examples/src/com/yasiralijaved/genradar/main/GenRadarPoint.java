package com.yasiralijaved.genradar.main;

/**
 * @author Yasir.Ali <ali.yasir0@gmail.com>
 *
 */

import android.location.Location;

public class GenRadarPoint {

	private String locationName; 
	private double lat;
	private double lng;
	private float x;
	private float y;
	private float raduis;
	private int color;
	private float distance;
	private boolean isVisibleOnRadar = true;
	

	@Override
	public String toString() {
		return "MyRadarPoint [locationName=" + locationName + ", lat=" + lat
				+ ", lng=" + lng + ", x=" + x + ", y=" + y + ", raduis="
				+ raduis + ", color=" + color + ", distance=" + distance + "]";
	}

	public GenRadarPoint(String locationName, double lat, double lng, float x, float y, float raduis,
			int color) {
		super();
		this.locationName = locationName;
		this.lat = lat;
		this.lng = lng;
		this.x = x;
		this.y = y;
		this.raduis = raduis;
		this.color = color;
	}

	public void updateDistanceWithThisLocation(double currLat, double currLng){
		float[] distanceArray = new float[1];
		Location.distanceBetween(currLat, currLng, lat, lng, distanceArray);
        distance = distanceArray[0] * 0.05f;
	}
	
	public boolean isVisibleOnRadar() {
		return isVisibleOnRadar;
	}

	public void setVisibleOnRadar(boolean isVisibleOnRadar) {
		this.isVisibleOnRadar = isVisibleOnRadar;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getRaduis() {
		return raduis;
	}

	public void setRaduis(float raduis) {
		this.raduis = raduis;
	}
	
}
