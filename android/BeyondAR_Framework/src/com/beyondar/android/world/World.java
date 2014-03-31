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
import java.util.List;

import android.content.Context;
import android.location.Location;

import com.beyondar.android.opengl.colision.MeshCollider;
import com.beyondar.android.plugin.Plugable;
import com.beyondar.android.plugin.WorldPlugin;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.math.Distance;
import com.beyondar.android.util.math.geom.Plane;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.util.math.geom.Vector3;

public class World implements Plugable<WorldPlugin> {

	protected static final String TAG = "world";

	/**
	 * The maximum distance that the object will be displayed (meters) in the AR
	 * view
	 */
	public static final int MAX_AR_VIEW_DISTANCE = 1000;

	public static final int LIST_TYPE_DEFAULT = 0;

	public static final String URI_PREFIX_DEFAULT_BITMAP = "beyondar_default_Bitmap_BeyondarList_type_";

	private float ZERO = 1e-8f;
	private Object mLock = new Object();
	
	protected List<BeyondarObjectList> beyondarObjectLists;
	protected double longitude, latitude, altitude;
	
	private Context mContext;
	private double mArViewDistance;
	private BitmapCache mBitmapHolder;
	private String mDefaultBitmap;

	protected List<WorldPlugin> plugins;
	protected Object lockplugins = new Object();

	public World(Context context) {
		mContext = context;
		mBitmapHolder = BitmapCache.initialize(mContext.getResources(), getClass().getName(), true);
		createBeyondarObjectListArray();
		mArViewDistance = MAX_AR_VIEW_DISTANCE;
		plugins = new ArrayList<WorldPlugin>(DEFAULT_PLUGINS_CAPACITY);
	}

	protected Context getContext() {
		return mContext;
	}

	public BitmapCache getBitmapCache() {
		return mBitmapHolder;
	}

	private void createBeyondarObjectListArray() {
		beyondarObjectLists = new ArrayList<BeyondarObjectList>();
		beyondarObjectLists.add(new BeyondarObjectList(LIST_TYPE_DEFAULT, this));

	}

	/**
	 * Add a {@link WorldPlugin} to the {@link World}. If the plug-in exist it
	 * will not be added again.
	 */
	public void addPlugin(WorldPlugin plugin) {
		synchronized (lockplugins) {
			if (!plugins.contains(plugin)) {
				plugins.add(plugin);
			}
		}
		plugin.setup(this);
	}

	/**
	 * Remove existing plug-in.
	 * 
	 * @param plugin
	 *            plug-in to be removed
	 * @return True if the plug-in has been removed, false otherwise
	 */
	@Override
	public boolean removePlugin(WorldPlugin plugin) {
		boolean removed = false;
		synchronized (lockplugins) {
			removed = plugins.remove(plugin);
		}
		if (removed) {
			plugin.onDetached();
		}
		return removed;
	}

	@Override
	public void removeAllPlugins() {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				removePlugin(plugin);
			}
		}
	}

	@Override
	public WorldPlugin getFirstPlugin(Class<? extends WorldPlugin> pluginClass) {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					return plugin;
				}
			}
		}
		return null;
	}

	@Override
	public boolean containsAnyPlugin(Class<? extends WorldPlugin> pluginClass) {
		return getFirstPlugin(pluginClass) != null;
	}

	@Override
	public boolean containsPlugin(WorldPlugin plugin) {
		synchronized (lockplugins) {
			return plugins.contains(plugin);
		}
	}

	@Override
	public List<WorldPlugin> getAllPugins(Class<? extends WorldPlugin> pluginClass) {
		ArrayList<WorldPlugin> result = new ArrayList<WorldPlugin>(5);
		return getAllPlugins(pluginClass, result);
	}

	@Override
	public List<WorldPlugin> getAllPlugins(Class<? extends WorldPlugin> pluginClass,
			List<WorldPlugin> result) {
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					result.add(plugin);
				}
			}
		}
		return result;
	}

	@Override
	public List<WorldPlugin> getAllPlugins() {
		synchronized (lockplugins) {
			return new ArrayList<WorldPlugin>(plugins);
		}
	}

	/**
	 * Add a {@link BeyondarObject} to the default list in the world.
	 * 
	 * @param beyondarObject
	 */
	public final synchronized void addBeyondarObject(BeyondarObject beyondarObject) {
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
				beyondarObjectLists.add(listTmp);
				synchronized (lockplugins) {
					for (WorldPlugin plugin : plugins) {
						plugin.onBeyondarObjectListCreated(listTmp);
					}
				}
			}
			beyondarObject.setWorldListType(worldListType);
			listTmp.add(beyondarObject);
			synchronized (lockplugins) {
				for (WorldPlugin plugin : plugins) {
					plugin.onBeyondarObjectAdded(beyondarObject, listTmp);
				}
			}
		}
	}

	/**
	 * Remove a {@link BeyondarObject} form the World. To do this, the function
	 * <code>getWorldListType</code> is used.
	 * 
	 * @param beyondarObject
	 * @return True if the object has been removed, false otherwise
	 */
	public synchronized boolean remove(BeyondarObject beyondarObject) {
		synchronized (mLock) {
			BeyondarObjectList listTmp = getBeyondarObjectList(beyondarObject.getWorldListType());
			if (listTmp != null) {
				listTmp.remove(beyondarObject);
				synchronized (lockplugins) {
					for (WorldPlugin plugin : plugins) {
						plugin.onBeyondarObjectRemoved(beyondarObject, listTmp);
					}
				}
				beyondarObject.onRemoved();
				return true;
			}
			return false;
		}
	}

	public synchronized void forceProcessRemoveQueue() {
		if (beyondarObjectLists.size() > 0) {
			synchronized (mLock) {
				for (int i = 0; i < beyondarObjectLists.size(); i++) {
					beyondarObjectLists.get(i).forceRemoveObjectsInQueue();
				}
			}
		}
	}

	/**
	 * Clean all the data stored in the world object
	 */
	public synchronized void clearWorld() {
		synchronized (mLock) {
			beyondarObjectLists.clear();
			mBitmapHolder.clean();
		}
	}

	/**
	 * Get the user's longitude
	 * 
	 * @return
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Get the user's altitude
	 * 
	 * @return
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Get the user's latitude
	 * 
	 * @return
	 */
	public double getLatitude() {
		return latitude;
	}

	public void setGeoPosition(double latitude, double longitude, double altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				plugin.onGeoPositionChanged(latitude, longitude, altitude);
			}
		}
	}

	public final void setGeoPosition(double latitude, double longitude) {
		setGeoPosition(latitude, longitude, altitude);
	}

	public void setLocation(Location location) {

		if (location == null) {
			return;
		}
		// We do not set the altitude because it is a big source of issues, the
		// accuracy is too bad
		setGeoPosition(location.getLatitude(), location.getLongitude());

	}

	public synchronized void setDefaultBitmap(int defaultBitmap) {
		setDefaultBitmap(BitmapCache.normalizeURI(defaultBitmap));
	}

	public synchronized void setDefaultBitmap(String uri) {
		mDefaultBitmap = uri;
		synchronized (lockplugins) {
			for (WorldPlugin plugin : plugins) {
				plugin.onDefaultImageChanged(uri);
			}
		}
	}

	/**
	 * Set the default bitmap for the specified list type. This bitmap is used
	 * when the there are not any bitmap loaded for a {@link BeyondarObject}
	 * 
	 * @param uri
	 *            The default Bitmap
	 * @param type
	 *            The type of the list to set the bitmap
	 * @return The URI of the bitmap loaded or null if the bitmap has not been
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
		if (list != null && list.getDefaultBitmapURI() != null) {
			return list.getDefaultBitmapURI();
		}
		return mDefaultBitmap;
	}

	/**
	 * Get the default bitmap URI of the world
	 * 
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
		for (int i = 0; i < beyondarObjectLists.size(); i++) {
			list = beyondarObjectLists.get(i);
			if (list.getType() == type) {
				return list;
			}
		}
		return null;

	}

	/**
	 * Get the container that holds all the {@link BeyondarObjectList} in the
	 * {@link World}
	 * 
	 * @return The list of the lists
	 */
	public List<BeyondarObjectList> getBeyondarObjectLists() {
		return beyondarObjectLists;
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
			for (int i = 0; i < beyondarObjectLists.size(); i++) {
				beyondarList = beyondarObjectLists.get(i);
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
							if (dst > getArViewDistance()) {
								continue;
							}
						}

						MeshCollider collisionDetector = beyondarObject.getMeshCollider();

						Point3 point = collisionDetector.getIntersectionPoint(ray);

						if (point != null) {
							beyondarObjectsOutput.add(beyondarObject);
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
	public void setArViewDistance(double meters) {
		mArViewDistance = meters;
	}

	/**
	 * Get the distance (in meters) which the AR view will draw the objects.
	 * 
	 * @return meters
	 */
	public double getArViewDistance() {
		return mArViewDistance;
	}

	public static List<BeyondarObject> sortGeoObjectByDistanceFromCenter(List<BeyondarObject> vec) {
		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			for (int i = 0; i < vec.size() - 1; i++) {
				BeyondarObject go1 = vec.get(i);
				BeyondarObject go2 = vec.get(i + 1);
				if (go2.getDistanceFromUser() < go1.getDistanceFromUser()) {
					sorted = false;
					vec.set(i, go2);
					vec.set(i + 1, go1);
				}
				// Vector3 vec1 = new Vector3(go1.getPosition());
				// Vector3 vec2 = new Vector3(go2.getPosition());
				//
				// if (vec2.module() < vec1.module()) {
				// sorted = false;
				// vec.set(i, go2);
				// vec.set(i + 1, go1);
				// }
			}
		}
		return vec;
	}
}
