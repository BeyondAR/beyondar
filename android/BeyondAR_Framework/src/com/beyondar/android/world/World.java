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

import com.beyondar.android.module.Modulable;
import com.beyondar.android.module.WorldModule;
import com.beyondar.android.opengl.colision.MeshCollider;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.math.Distance;
import com.beyondar.android.util.math.geom.Plane;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.util.math.geom.Vector3;

public class World implements Modulable<WorldModule> {

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

	protected List<WorldModule> modules;
	protected Object lockModules = new Object();

	public World(Context context) {
		mContext = context;
		mBitmapHolder = BitmapCache.initialize(mContext.getResources(), getClass().getName(), true);
		createBeyondarObjectListArray();
		mArViewDistance = MAX_AR_VIEW_DISTANCE;
		modules = new ArrayList<WorldModule>(DEFAULT_INIT_MODULES_CAPACITY);
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
	 * Add a {@link WorldModule} to the {@link World}. If the module exist it
	 * will not be added again.
	 */
	public void addModule(WorldModule module) {
		synchronized (lockModules) {
			if (!modules.contains(module)) {
				modules.add(module);
			}
		}
		module.setup(this);
	}

	/**
	 * Remove existing module.
	 * 
	 * @param module
	 *            module to be removed
	 * @return True if the module has been removed, false otherwise
	 */
	@Override
	public boolean removeModule(WorldModule module) {
		boolean removed = false;
		synchronized (lockModules) {
			removed = modules.remove(module);
		}
		if (removed) {
			module.onDetached();
		}
		return removed;
	}

	@Override
	public void cleanModules() {
		synchronized (lockModules) {
			for (WorldModule module : modules) {
				removeModule(module);
			}
		}
	}

	@Override
	public WorldModule getFirstModule(Class<? extends WorldModule> moduleClass) {
		synchronized (lockModules) {
			for (WorldModule module : modules) {
				if (moduleClass.isInstance(module)) {
					return module;
				}
			}
		}
		return null;
	}

	@Override
	public boolean containsAnyModule(Class<? extends WorldModule> moduleClass) {
		return getFirstModule(moduleClass) != null;
	}

	@Override
	public boolean containsModule(WorldModule module) {
		synchronized (lockModules) {
			return modules.contains(module);
		}
	}

	@Override
	public List<WorldModule> getAllModules(Class<? extends WorldModule> moduleClass) {
		ArrayList<WorldModule> result = new ArrayList<WorldModule>(5);
		return getAllModules(moduleClass, result);
	}

	@Override
	public List<WorldModule> getAllModules(Class<? extends WorldModule> moduleClass,
			List<WorldModule> result) {
		synchronized (lockModules) {
			for (WorldModule module : modules) {
				if (moduleClass.isInstance(module)) {
					result.add(module);
				}
			}
		}
		return result;
	}

	@Override
	public List<WorldModule> getAllModules() {
		synchronized (lockModules) {
			return new ArrayList<WorldModule>(modules);
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
				synchronized (lockModules) {
					for (WorldModule module : modules) {
						module.onBeyondarObjectListCreated(listTmp);
					}
				}
			}
			beyondarObject.setWorldListType(worldListType);
			listTmp.add(beyondarObject);
			synchronized (lockModules) {
				for (WorldModule module : modules) {
					module.onBeyondarObjectAdded(beyondarObject, listTmp);
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
				synchronized (lockModules) {
					for (WorldModule module : modules) {
						module.onBeyondarObjectRemoved(beyondarObject, listTmp);
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
		synchronized (lockModules) {
			for (WorldModule module : modules) {
				module.onGeoPositionChanged(latitude, longitude, altitude);
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
		synchronized (lockModules) {
			for (WorldModule module : modules) {
				module.onDefaultImageChanged(uri);
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
