/*
 * Copyright (C) 2013 BeyondAR
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
package com.beyondar.android.world;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import android.content.Context;
import android.location.Location;

import com.beyondar.android.opengl.colision.MeshCollider;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.math.Distance;
import com.beyondar.android.util.math.geom.Plane;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.util.math.geom.Vector3;

public class World {

	protected static final String TAG = "world";

	private static World sWorld;

	/**
	 * This method helps you to create an unique world class
	 * 
	 * @param world
	 */
	public static void setWorld(World world) {
		sWorld = world;
	}

	/**
	 * Get the world
	 * 
	 * @return
	 */
	public static World getWorld() {
		return sWorld;
	}

	/**
	 * The maximum distance that the object will be displayed (meters)
	 */
	public static final int MAX_VIEW_DISTANCE = 1000;

	public static final int LIST_TYPE_0 = 0;
	public static final int LIST_TYPE_1 = 1;
	public static final int LIST_TYPE_2 = 2;
	public static final int LIST_TYPE_3 = 3;
	public static final int LIST_TYPE_4 = 4;
	public static final int LIST_TYPE_5 = 5;
	public static final int LIST_TYPE_6 = 6;
	public static final int LIST_TYPE_7 = 7;
	public static final int LIST_TYPE_8 = 8;
	public static final int LIST_TYPE_9 = 9;
	public static final int LIST_TYPE_10 = 10;
	public static final int LIST_TYPE_11 = 11;
	public static final int LIST_TYPE_DEFAULT = LIST_TYPE_0;

	public static final String URI_PREFIX_DEFAUL_BITMAP = "beyondar_default_Bitmap_BeyondarList_type_";

	private float ZERO = 1e-8f;
	private Object mLock = new Object();
	protected ArrayList<BeyondarObjectList> mBeyondarObjectLists;
	protected double mLongitude, mLatitude, mAltitude;
	private Context mContext;
	private double mViewDistance;
	private BitmapCache mBitmapHolder;
	private String mDefaultBitmap;

	public World(Context context) {
		mContext = context;
		mBitmapHolder = BitmapCache.initialize(mContext.getResources(), getClass().getName(), true);
		createBeyondarObjectListArray();
		mViewDistance = MAX_VIEW_DISTANCE;
	}

	public BitmapCache getBitmapCache() {
		return mBitmapHolder;
	}

	private void createBeyondarObjectListArray() {
		mBeyondarObjectLists = new ArrayList<BeyondarObjectList>();
		mBeyondarObjectLists.add(new BeyondarObjectList(LIST_TYPE_DEFAULT, this));

	}

	/**
	 * Add a {@link BeyondarObject} to the default list in the world.
	 * 
	 * @param beyondarObject
	 */
	public synchronized void addBeyondarObject(BeyondarObject beyondarObject) {
		addBeyondarObject(beyondarObject, LIST_TYPE_DEFAULT);
	}

	/**
	 * Add a {@link BeyondarObject} to the specified list in the world.
	 * 
	 * @param beyondarObject
	 */
	public synchronized void addBeyondarObject(BeyondarObject beyondarObject, int worldListType) {
		if (beyondarObject == null) {
			return;
		}
		synchronized (mLock) {
			BeyondarObjectList listTmp = getBeyondarObjectList(worldListType);
			if (listTmp == null) {
				listTmp = new BeyondarObjectList(worldListType, this);
				mBeyondarObjectLists.add(listTmp);
			}
			beyondarObject.setWorldListType(worldListType);
			listTmp.add(beyondarObject);
		}
	}

	/**
	 * Remove a {@link BeyondarObject} form the World. To do this, the function
	 * <code>getWorldListType</code> is used.
	 * 
	 * @param beyondarObject
	 * @return
	 */
	public synchronized boolean remove(BeyondarObject beyondarObject) {
		synchronized (mLock) {
			BeyondarObjectList listTmp = getBeyondarObjectList(beyondarObject.getWorldListType());

			if (listTmp != null) {
				listTmp.remove(beyondarObject);
				return true;
			}
			return false;
		}
	}

	public synchronized void forceProcessRemoveQueue() {
		if (mBeyondarObjectLists.size() > 0) {
			synchronized (mLock) {
				for (int i = 0; i < mBeyondarObjectLists.size(); i++) {
					mBeyondarObjectLists.get(i).forceRemoveObjectsInQueue();
				}
			}
		}

	}

	protected synchronized void clearWorld() {
		synchronized (mLock) {
			mBeyondarObjectLists.clear();
			mBitmapHolder.clean();
		}
	}

	/**
	 * Get the user's longitude
	 * 
	 * @return
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * Get the user's altitude
	 * 
	 * @return
	 */
	public double getAltitude() {
		return mAltitude;
	}

	/**
	 * Set the user's altitude
	 * 
	 * @param altitude
	 */
	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	/**
	 * Set the user's Longitude
	 * 
	 * @param longitude
	 */
	@Deprecated
	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}

	/**
	 * Get the user's latitude
	 * 
	 * @return
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * Set the user's latitude.
	 * 
	 * @param latitude
	 */
	@Deprecated
	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}
	
	public void setPosition(double latitude, double longitude, double altitude){
		mLatitude = latitude;
		mLongitude = longitude;
		mAltitude = altitude;
	}
	
	public void setPosition(double latitude, double longitude){
		mLatitude = latitude;
		mLongitude = longitude;
	}

	@Deprecated
	public void setLocation(Location location) {

		if (location == null) {
			return;
		}
		setLatitude(location.getLatitude());
		setLongitude(location.getLongitude());
		// We do not set the altitude because it is a big source of issues, the
		// accuracy is too bad
		// setAltitude(location.getAltitude());
	}

	public synchronized void setDefaultBitmap(int defaultBitmap) {
		setDefaultBitmap(BitmapCache.normalizeURI(defaultBitmap));
	}

	public synchronized void setDefaultBitmap(String uri) {
		mDefaultBitmap = uri;
	}

	/**
	 * Set the default bitmap for the specified list type. This bitmap is used
	 * when the there are not any bitmap loaded for a {@link BeyondarObject}
	 * 
	 * @param uri
	 *            The default Bitmap
	 * @param type
	 *            The type of the list to set the bitmap
	 * @return The URI of the bitmap loaded or null if the bit ma has not been
	 *         loaded
	 */
	public synchronized boolean setDefaultBitmap(String uri, int type) {
		BeyondarObjectList list = getBeyondarObjectList(type);
		if (list != null) {
			list.setDefaultBitmapURI(uri);
			return true;
		}
		return false;
	}

	/**
	 * Set the default bitmap for the specified list type. This bitmap is used
	 * when the there are not any bitmap loaded for a {@link BeyondarObject}
	 * 
	 * @param defaultBitmap
	 *            The default Bitmap reference
	 * @param type
	 *            The type of the list to set the bitmap
	 * @return true if the bitmap has been loaded properly, false otherwise
	 */
	public synchronized boolean setDefaultBitmap(int defaultBitmap, int type) {
		return setDefaultBitmap(BitmapCache.normalizeURI(defaultBitmap), type);
	}

	/**
	 * Get the default bitmap URI of the specified list
	 * 
	 * @param type
	 *            the type of the list
	 * @return
	 */
	public synchronized String getDefaultBitmap(int type) {
		BeyondarObjectList list = getBeyondarObjectList(type);
		if (list != null) {
			return list.getDefaultBitmapURI();
		}
		return mDefaultBitmap;
	}

	/**
	 * Get the default bitmap URI of the world
	 * 
	 * @param type
	 *            the type of the list
	 * @return
	 */
	public synchronized String getDefaultBitmap() {
		return mDefaultBitmap;
	}

	public int checkIntersectionPlane(Plane plane, Point3 position, Vector3 direction, double[] lamda,
			Vector3 pNormal) {

		double DotProduct = direction.dotProduct(plane.getNormal());
		double l2;

		// determine if ray parallel to plane
		if ((DotProduct < ZERO) && (DotProduct > -ZERO))
			return 0;

		Vector3 substract = new Vector3(plane.getPoint());
		substract.subtract(position);
		l2 = (plane.getNormal().dotProduct(substract)) / DotProduct;

		if (l2 < -ZERO)
			return 0;

		pNormal.set(plane.getNormal());
		lamda[0] = l2;
		return 1;
	}

	/**
	 * Get the {@link BeyondarObjectList} for the specified type.
	 * 
	 * @return
	 */
	public BeyondarObjectList getBeyondarObjectList(int type) {
		BeyondarObjectList list = null;
		for (int i = 0; i < mBeyondarObjectLists.size(); i++) {
			list = mBeyondarObjectLists.get(i);
			if (list.getType() == type) {
				return list;
			}
		}
		return null;

	}

	/**
	 * Get the container that holds all the {@link BeyondarObjectList} in the {@link World}
	 * 
	 * @return The list of the lists
	 */
	public ArrayList<BeyondarObjectList> getBeyondarObjectLists() {
		return mBeyondarObjectLists;
	}

	/**
	 * Get all the {@link BeyondarObject} that collide with the {@link Ray}.
	 * 
	 * @param ray
	 *            The ray to use for the collision calculus
	 * 
	 * @param beyondarObjectsOutput
	 *            The {@link ArrayList} that will store the objects sorted by
	 *            proximity. This list will be cleaned before.
	 */
	public void getBeyondarObjectsCollideRay(Ray ray, ArrayList<BeyondarObject> beyondarObjectsOutput) {

		beyondarObjectsOutput.clear();

		// int counter = 0;
		// ArrayList<BeyondarObject> beyondarObjects = new
		// ArrayList<BeyondarObject>();
		BeyondarObjectList beyondarList = null;

		try {
			for (int i = 0; i < mBeyondarObjectLists.size(); i++) {
				beyondarList = mBeyondarObjectLists.get(i);
				if (beyondarList != null) {
					for (int j = 0; j < beyondarList.size(); j++) {

						BeyondarObject beyondarObject = beyondarList.get(j);
						if (beyondarObject == null) {
							continue;
						}

						if (beyondarObject instanceof GeoObject) {
							GeoObject go = (GeoObject) beyondarObject;
							double dst = Distance.calculateDistanceMeters(go.getLongitude(),
									go.getLatitude(), getLongitude(), getLatitude());
							if (dst > getViewDistance()) {
								continue;
							}
						}

						MeshCollider colisionDetector = beyondarObject.getMeshCollider();

						Point3 point = colisionDetector.getIntersectionPoint(ray);

						if (point == null) {
							// Log.d(Constants.TAG, "No colision "
							// + mGeoObjectList.get(i).getName());
						} else {
							beyondarObjectsOutput.add(beyondarObject);
							// Log.d(Constants.TAG, "Colision!! " +
							// geoObject.getName());
						}

					}
				}
			}
			if (beyondarObjectsOutput.size() != 0) {
				sortGeoObjectByDistanceFromCenter(beyondarObjectsOutput);
			}
		} catch (ConcurrentModificationException e) {
			getBeyondarObjectsCollideRay(ray, beyondarObjectsOutput);
		}
	}

	/**
	 * Set the distance (in meters) which the app will draw the objects.
	 * 
	 * @param meters
	 */
	public void setViewDistance(double meters) {
		mViewDistance = meters;
	}

	/**
	 * Get the distance (in meters) which the app will draw the objects.
	 * 
	 * @return meters
	 */
	public double getViewDistance() {
		return mViewDistance;
	}

	// TODO: Fix this method. It is not sorting from the user distance
	public void sortGeoObjectByDistanceFromCenter(ArrayList<BeyondarObject> vec) {
		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			for (int i = 0; i < vec.size() - 1; i++) {
				BeyondarObject go1 = vec.get(i);
				BeyondarObject go2 = vec.get(i + 1);
				Vector3 vec1 = new Vector3(go1.getPosition());
				Vector3 vec2 = new Vector3(go2.getPosition());

				if (vec2.module() < vec1.module()) {
					sorted = false;
					vec.set(i, go2);
					vec.set(i + 1, go1);
				}
			}
		}
	}
}
