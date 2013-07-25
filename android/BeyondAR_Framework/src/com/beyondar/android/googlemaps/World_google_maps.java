/**
 * 
 */
package com.beyondar.android.googlemaps;

import android.content.Context;

import com.beyondar.android.world.World;
import com.google.android.maps.Overlay;

/**
 * @author Joan Puig Sanz (joanpuigsanz@gmail.com)
 * 
 */
public class World_google_maps extends World {

	private double mMapViewDistance;

	/**
	 * In DIP
	 */
	public static final int DEFAULT_SIZE_ICONS_MAP = 35;

	public World_google_maps(Context context) {
		super(context);
		mMapViewDistance = getViewDistance();
	}

	public GeoObjectOberlay getOverlay(int iconSize) {
		return new GeoObjectOberlay(iconSize, this);
	}

	public Overlay getOverlay() {
		return getOverlay(DEFAULT_SIZE_ICONS_MAP);

	}

	/**
	 * Set the distance (in meters) which the app will draw the objects.
	 * 
	 * @param meters
	 */
	public void setMapViewDistance(double meters) {
		mMapViewDistance = meters;
	}

	/**
	 * Get the distance (in meters) which the app will draw the objects.
	 * 
	 * @return meters
	 */
	public double getMapViewDistance() {
		return mMapViewDistance;
	}

}
