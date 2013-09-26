package com.beyondar.android.world;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.util.PendingBitmapsToBeLoaded;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.cache.BitmapCache.OnExternalBitmapLoadedCahceListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WorldGoogleMaps extends World implements OnExternalBitmapLoadedCahceListener {

	/** Default icon size for the markers in dips */
	public static final int DEFAULT_ICON_SIZE_MARKER = 40;
	private GoogleMap mMap;

	private BitmapCache mCache;
	private int mIconSize;
	private PendingBitmapsToBeLoaded<GeoObjectGoogleMaps> mPendingBitmaps;

	private static World sWorld;

	private LatLng mLatLng;

	private static Handler sHandler = new Handler(Looper.getMainLooper());

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
		mPendingBitmaps = new PendingBitmapsToBeLoaded<GeoObjectGoogleMaps>();
		mCache = createBitmapCache();
		mCache.addOnExternalBitmapLoadedCahceListener(this);

		mIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ICON_SIZE_MARKER,
				context.getResources().getDisplayMetrics());
	}

	public WorldGoogleMaps(Context context, int iconSize) {
		this(context);
		mIconSize = iconSize;
	}

	/**
	 * Set the size of the marker icons in pixels
	 * 
	 * @param iconSize
	 */
	public void seetMarkerIconSize(int iconSize) {
		mIconSize = iconSize;
	}

	protected BitmapCache createBitmapCache() {
		return getBitmapCache().newCache(getClass().getName(), true);
	}

	@Override
	public synchronized void addBeyondarObject(BeyondarObject beyondarObject) {
		super.addBeyondarObject(beyondarObject);
		addMarkerToBeyondarObject(beyondarObject);
	}

	@Override
	public synchronized void addBeyondarObject(BeyondarObject beyondarObject, int worldListType) {
		super.addBeyondarObject(beyondarObject, worldListType);
		addMarkerToBeyondarObject(beyondarObject);
	}

	private void addMarkerToBeyondarObject(BeyondarObject beyondarObject) {
		if (beyondarObject instanceof GeoObjectGoogleMaps) {
			createMarker((GeoObjectGoogleMaps) beyondarObject);
		}

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

		if (mMap == null) {
			return;
		}
		marker = mMap.addMarker(generateMarkerOptions(geoObjectGoogleMaps));
		geoObjectGoogleMaps.setMarker(marker);
	}

	protected MarkerOptions generateMarkerOptions(GeoObjectGoogleMaps geoObject) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(geoObject.getName());
		markerOptions.position(geoObject.getLatLng());

		Bitmap btm = getBitmap(geoObject.getBitmapUri());

		if (btm != null) {
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(btm));
		} else {
			mPendingBitmaps.addObject(geoObject.getBitmapUri(), geoObject);
		}
		return markerOptions;
	}
	
	protected Bitmap getBitmap(String uri) {
		Bitmap btm = mCache.getBitmap(uri);

		if (btm == null || btm.isRecycled()) {
			return null;
		}

		if (btm.getHeight() != mIconSize && btm.getWidth() != mIconSize) {
			Bitmap tmp = ImageUtils.resizeImage(btm, mIconSize, mIconSize);
			mCache.storeBitmap(uri, tmp);
			if (btm != tmp) {
				btm.recycle();
			}
			btm = tmp;
		}

		return btm;
	}

	@Override
	public void onExternalBitmapLoaded(BitmapCache cache, String url, Bitmap btm) {
		ArrayList<GeoObjectGoogleMaps> list = mPendingBitmaps.getPendingList(url);
		for (int i = 0; i < list.size(); i++) {
			final GeoObjectGoogleMaps gogm = list.get(i);
			sHandler.post(new Runnable() {
				@Override
				public void run() {
					Bitmap btm = getBitmap(gogm.getBitmapUri());
					if (btm != null) {
						gogm.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(btm));
					}
				}
			});
		}
	}
}
