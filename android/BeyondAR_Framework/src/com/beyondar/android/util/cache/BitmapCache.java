/*
 * Copyright (C) 2014 BeyondAR
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
package com.beyondar.android.util.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.beyondar.android.util.DebugBitmap;
import com.beyondar.android.util.Logger;

public class BitmapCache {

	public static final int IMAGE_NOT_LOADED = 0;
	public static final int IMAGE_LOADED = 1;
	public static final int IMAGE_IN_PROGRESS = 2;
	public static final int ERROR_LOADING_IMAGE = 3;
	
	private static final int MAXIMUM_POOL_SIZE = 10;
	private static final int CORE_POOL_SIZE = 4;
	private static final int KEEP_ALIVE_THREAD = 5000;

	private static final String TAG = "bitmapHolder";

	/** Default cache size. 6 MB */
	public static final int DEFAULT_CACHE_SIZE = 6 * 1024 * 1024;

	public static final int DEFAULT_TIME_OUT = 30000;

	public static final int DEFAULT_MAX_WIDTH = 256;
	public static final int DEFAULT_MAX_HEIGHT = 256;

	public static final String HEADER_HTTP = "http://";
	public static final String HEADER_HTTPS = "https://";
	public static final String HEADER_FILE = "file://";
	public static final String HEADER_FILE_ = "/";
	public static final String HEADER_RESOURCE = "res://";
	public static final String HEADER_ASSETS = "assets://";

	private static final boolean DEBUG_CACHE = false;

	private String mIdCache;

	private int mMaxWidth = DEFAULT_MAX_WIDTH;
	private int mMaxHeight = DEFAULT_MAX_HEIGHT;

	private Resources mRes;

	private Hashtable<String, Integer> mLoadingResources;

	private BitmapCacheContainer mBitmapContainer;

	private ArrayList<OnExternalBitmapLoadedCacheListener> mOnLoadBitmapListener;
	
	private ThreadPoolExecutor mThreadPool;
	private BlockingQueue<Runnable> mBlockingQueue;


	private class BitmapCacheContainer extends LruCache<String, Bitmap> {

		private boolean purge;
		private boolean alwaysPurge;

		/**
		 * Initialize the cache object. The maximum size of this cache is needed
		 * (in Bytes). <br>
		 * The flag alwaysPurge is needed to specify the cache if it is needed
		 * to recycle the Bitmap when this is removed form the cache. If the
		 * bitmap is not removed, the bitmap will get lost in the memory and it
		 * could cause some memory issues: (java.lang.OutOfMemoryError: bitmap
		 * size exceeds VM budget)
		 * 
		 * @param maxSize
		 * @param alwaysPurge
		 */
		public BitmapCacheContainer(int maxSize, boolean alwaysPurge) {
			super(maxSize);
			purge = false;
			this.alwaysPurge = alwaysPurge;
		}

		@Override
		protected void entryEvicted(String key, Bitmap value) {
			if (DEBUG_CACHE) {
				cacheLogD(mIdCache + " ____ Removing bitmap form cahce (out of cache memory): "
						+ key);
			}
			mLoadingResources.remove(key);
			if (value != null && (alwaysPurge || purge)) {
				value.recycle();
				cacheLogD(mIdCache + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Purge: " + key);
			}
		}

		@Override
		protected void entryRemoved(String key, Bitmap value) {
			if (DEBUG_CACHE) {
				cacheLogD(mIdCache + " ____ Removing bitmap form cahce (method remove() called): "
						+ key);
			}
			mLoadingResources.remove(key);
			if (value != null && (alwaysPurge || purge)) {
				value.recycle();
				cacheLogD(mIdCache + "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Purge: " + key);
			}
		}

		private synchronized void purge() {
			purge = true;
			evictAll();
			purge = false;
		}

		private ArrayList<String> keysToClean = new ArrayList<String>();

		private synchronized int cleanRecycledBitmaps() {
			int counter = 0;

			for (Map.Entry<String, Bitmap> entry : map.entrySet()) {
				if (entry.getValue().isRecycled()) {
					keysToClean.add(entry.getKey());
				}
			}

			// A second loop to avoid the concurrent modification exception
			// while removing the elements
			for (int i = 0; i < keysToClean.size(); i++) {
				remove(keysToClean.get(i));
				counter++;
			}

			keysToClean.clear();

			if (DEBUG_CACHE) {
				cacheLogD(mIdCache + "  --- Recycled bitmaps cleaned: " + counter + "/"
						+ (map.size() + counter));
			}
			return counter;
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			if (value != null) {
				return value.getRowBytes() * value.getHeight();
			}
			return super.sizeOf(key, value);
		}

		@Override
		protected void trimToSize(int maxSize) {
			// Before removing the non recycled bitmaps, first we remove all the
			// Recycled Bitmaps
			if (size > maxSize) {
				evictionCount = evictionCount + cleanRecycledBitmaps();
			}
			super.trimToSize(maxSize);
		}

	}

	private BitmapCache(Resources resources, int maxWidth, int maxHeight, int cacheSize, String id,
			boolean alwaysPurge) {

		if (null == resources) {
			throw new NullPointerException(
					"Resources can not be null. initialize the BitmapHolder with a valid Resource");
		}

		mIdCache = id;
		mMaxHeight = maxHeight;
		mMaxWidth = maxWidth;

		mRes = resources;
		mBitmapContainer = new BitmapCacheContainer(cacheSize, alwaysPurge);
		mLoadingResources = new Hashtable<String, Integer>();
		
		mBlockingQueue = new ArrayBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE);
		mThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_THREAD, TimeUnit.MILLISECONDS, mBlockingQueue);
	}

	public BitmapFactory.Options getOptimizedBitmapOption() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (mMaxHeight > 0 && mMaxWidth > 0) {
			options.inJustDecodeBounds = true;
		}
		return options;
	}

	public static BitmapCache initialize(Resources resources, String id, boolean alwaysPurge) {
		return initialize(resources, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, id, alwaysPurge);
	}

	public static BitmapCache initialize(Resources resources, int maxWidth, int maxHeight,
			String id, boolean alwaysPurge) {
		return initialize(resources, maxWidth, maxHeight, DEFAULT_CACHE_SIZE, id, alwaysPurge);
	}

	public static BitmapCache initialize(Resources resources, int maxWidth, int maxHeight,
			int cacheSize, String id, boolean alwaysPurge) {
		return new BitmapCache(resources, maxWidth, maxHeight, cacheSize, id, alwaysPurge);
	}

	public BitmapCache newCache(String id, boolean alwaysPurge) {
		return newCache(DEFAULT_CACHE_SIZE, id, alwaysPurge);
	}

	public BitmapCache newCache(int cacheSize, String id, boolean alwaysPurge) {
		return new BitmapCache(mRes, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, cacheSize, id,
				alwaysPurge);
	}

	/**
	 * Clean all the Bitmaps stored.
	 */
	public void clean() {
		if (DEBUG_CACHE) {
			cacheLogD(mIdCache + "  --- Cleaning the images");
		}
		mBitmapContainer.evictAll();
		mLoadingResources.clear();
		mBlockingQueue.clear();
		System.gc();
	}

	/**
	 * Clean the recycled Bitmaps stored in the cache.
	 * 
	 * @return Return the number of recycled imaged founded in the cache.
	 */
	public int cleanRecylcedBitmaps() {
		return mBitmapContainer.cleanRecycledBitmaps();
	}

	/**
	 * This method will clean the cache and will recycle all the bitmaps
	 */
	public void purge() {
		if (DEBUG_CACHE) {
			cacheLogD(mIdCache + "  --- Purging all the images");
		}
		mBitmapContainer.purge();
		mLoadingResources.clear();
		mBlockingQueue.clear();
		System.gc();
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
			int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	private Bitmap loadImageFromFile(String file) {
		Options options = getOptimizedBitmapOption();
		if (options.inJustDecodeBounds) {
			BitmapFactory.decodeFile(file, options);
			options.inSampleSize = calculateInSampleSize(options, mMaxWidth, mMaxHeight);
			options.inJustDecodeBounds = false;
		}

		Bitmap btm = DebugBitmap.decodeFile(file, options);
		return btm;
	}

	/**
	 * Store a bitmap with the name of the URI
	 * 
	 * @param btm
	 * @param uri
	 * @return The bitmap stored
	 */
	public Bitmap storeBitmap(String uri, Bitmap btm) {
		if (null != btm && null != uri) {
			if (mBitmapContainer.get(uri) != null) {
				if (DEBUG_CACHE) {
					Logger.w(TAG, mIdCache + "  = The image with the URI=" + uri
							+ " already exist. Overwriting...");
				}
				// return btm;
			}

			mLoadingResources.put(uri, IMAGE_LOADED);
			mBitmapContainer.put(uri, btm);
			if (DEBUG_CACHE) {
				cacheLogD(mIdCache + "  + Saving Bitmap: loadBitmap(ExBitmap btm): " + uri);
			}
		} else {
			if (DEBUG_CACHE) {
				cacheLogE(mIdCache + "  = Trying to store a null bitmap for uri=" + uri);
			}
		}
		return btm;
	}

	public synchronized void removeBitmap(String uri) {
		mBitmapContainer.remove(uri);
		mLoadingResources.remove(uri);
	}

	// TODO: Comment it in a properly way
	/**
	 * Load the bitmap according form the specified URI.
	 * 
	 * @param uri
	 * @return
	 */
	private Bitmap loadBitmap(String uri) {
		if (null == uri) {
			return null;
		}

		if (DEBUG_CACHE) {
			cacheLogD(mIdCache + "  +++ loading new bitmap: " + uri);
		}

		Bitmap btm = null;
		if (uri.startsWith(HEADER_RESOURCE)) {
			String sRes = uri.replaceFirst(HEADER_RESOURCE, "");
			int res;
			try {
				res = Integer.parseInt(sRes);
				return loadBitmap(res);
			} catch (NumberFormatException e) {
				btm = null;
			}
		} else if (uri.startsWith(HEADER_ASSETS)) {
			String path = uri.replace(HEADER_ASSETS, "");
			AssetManager assets = mRes.getAssets();
			try {
				InputStream is = assets.open(path);

				Options options = getOptimizedBitmapOption();
				if (options.inJustDecodeBounds) {
					DebugBitmap.decodeStream(is, null, options, path);
					options.inSampleSize = calculateInSampleSize(options, mMaxWidth, mMaxHeight);
					options.inJustDecodeBounds = false;
				}

				is.close();
				is = assets.open(path);
				btm = DebugBitmap.decodeStream(is, null, options, path);

			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (uri.startsWith(HEADER_HTTP) || uri.startsWith(HEADER_HTTPS)) {
			btm = requestDownloadBitmap(uri);
		} else if (uri.startsWith(HEADER_FILE) || uri.startsWith(HEADER_FILE_)) {
			String sRes = uri.replaceFirst(HEADER_FILE, "");
			btm = loadImageFromFile(sRes);
		}

		if (null == btm) {
			cacheLogE(mIdCache + " Error loading the resource: " + uri);
			// btm = _defaulResource;
		} else {
			storeBitmap(uri, btm);
		}

		return btm;
	}

	private Bitmap loadBitmap(int id) {
		String key = normalizeURI(id);
		Bitmap btm = mBitmapContainer.get(key);
		if (null != btm) {
			return (btm);
		}

		Options options = getOptimizedBitmapOption();
		if (options.inJustDecodeBounds) {
			BitmapFactory.decodeResource(mRes, id, options);
			options.inSampleSize = calculateInSampleSize(options, mMaxWidth, mMaxHeight);
			options.inJustDecodeBounds = false;
		}

		btm = DebugBitmap.decodeResource(mRes, id, options);

		return storeBitmap(key, btm);
	}

	public int getImageState(String uri) {
		Integer value = mLoadingResources.get(uri);
		if (value == null) {
			return IMAGE_NOT_LOADED;
		}
		return value.intValue();
	}

	/**
	 * Generate the the String URI according to the res id
	 * 
	 * @param res
	 * @return The String that represents the image
	 */
	public static String generateUri(int res) {
		return HEADER_RESOURCE + res;
	}

	/**
	 * Get the image from the resources.
	 * 
	 * @param id
	 *            The resource id to load
	 * @return The bitmap if the image is already loaded, null otherwise
	 */
	public Bitmap getBitmap(int id) {
		return getBitmap(HEADER_RESOURCE + id);
	}

	/**
	 * Get the Bitmap according to the uri. If it is not loaded, it try to load
	 * first.
	 * 
	 * @param uri
	 * @return
	 */
	public Bitmap getBitmap(String uri) {

		// if (DEBUG) {
		// cacheLog(TAG, mIdCache + " * Requesting: " + uri);
		// }
		if (uri == null) {
			return null;
		}

		Bitmap btm = mBitmapContainer.get(uri);

		if (btm == null || btm.isRecycled()) {
			if (btm != null) {
				mBitmapContainer.remove(uri);
			}

			btm = loadBitmap(uri);
		}

		return btm;
	}

	public boolean isImageLoaded(String uri) {
		Bitmap btm = mBitmapContainer.get(uri);

		if (btm == null || btm.isRecycled()) {
			return false;
		}
		return true;
	}

	/**
	 * Add a listener for when a bitmap that is not stored locally needs to be
	 * loaded
	 * 
	 * @param listener
	 */
	public void addOnExternalBitmapLoadedCahceListener(OnExternalBitmapLoadedCacheListener listener) {
		if (mOnLoadBitmapListener == null) {
			mOnLoadBitmapListener = new ArrayList<OnExternalBitmapLoadedCacheListener>();
		}
		if (!mOnLoadBitmapListener.contains(listener)) {
			mOnLoadBitmapListener.add(listener);
		}
	}

	/**
	 * Remove the listener
	 * 
	 * @param listener
	 * @return
	 */
	public boolean removeOnExternalBitmapLoadedCacheListener(
            OnExternalBitmapLoadedCacheListener listener) {
		if (mOnLoadBitmapListener == null) {
			return true;
		}
		return mOnLoadBitmapListener.remove(listener);
	}

	/**
	 * Normalize the resource int to a valid URI for the {@link BitmapCache}
	 * 
	 * @param res
	 * @return The res as a normalized URI ready to be used.
	 */
	public static String normalizeURI(int res) {
		return HEADER_RESOURCE + res;
	}

	public Bitmap requestDownloadBitmap(String uri) {
		Integer state = mLoadingResources.get(uri);
		if (state == null || state == IMAGE_NOT_LOADED || state == ERROR_LOADING_IMAGE) {
			mLoadingResources.put(uri, IMAGE_IN_PROGRESS);
			mThreadPool.execute(new TaskLoadHttpBitmap(uri));
		}
		return null;
	}

	private void cacheLogD(String msg) {
		if (!DEBUG_CACHE)
			return;
		Logger.d(TAG, msg);
	}

	private void cacheLogE(String msg) {
		if (!DEBUG_CACHE)
			return;
		Logger.e(TAG, msg);
	}

	private class TaskLoadHttpBitmap implements Runnable {

		private Bitmap mBtm;
		private String mUri;

		public TaskLoadHttpBitmap(String uri) {
			super();
			mUri = uri;
		}

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			Bitmap btm = null;

			if (DEBUG_CACHE) {
				cacheLogD(mIdCache + " ||||||||||   Obtaining the image from internet: " + mUri);
			}
			URL url;
			try {
				url = new URL(mUri);
				URLConnection urlConn = url.openConnection();
				urlConn.setConnectTimeout(DEFAULT_TIME_OUT);
				urlConn.setAllowUserInteraction(false);
				urlConn.setDoOutput(true);

				InputStream is = url.openStream();// urlConn.getInputStream();
				// Big image:
				// http://ettugamer.com/wp-content/gallery/marvel-vs-capcom-3-3/marvel_vs_capcom_3_ironman.png

				Options options = getOptimizedBitmapOption();
				if (options.inJustDecodeBounds) {
					DebugBitmap.decodeStream(is, null, options, mUri);
					options.inSampleSize = calculateInSampleSize(options, mMaxWidth, mMaxHeight);
					options.inJustDecodeBounds = false;
				}

				is.close();
				is = url.openStream();
				btm = DebugBitmap.decodeStream(is, null, options, mUri);

			} catch (MalformedURLException e) {
				String strData = mIdCache + " Error getting the image form internet: " + e;
				cacheLogE(strData);
				Logger.e(TAG, strData);
			} catch (IOException e) {
				String strData = mIdCache + " Error getting the image form internet: " + e;
				cacheLogE(strData);
				Logger.e(TAG, strData);
			}
			mBtm = btm;

			if (mBtm != null) {
				storeBitmap(mUri, mBtm);
				cacheLogD(mIdCache + " Image loaded form internet ("
						+ (System.currentTimeMillis() - time) + " ms): " + mUri);

				if (mOnLoadBitmapListener != null) {
					for (int i = 0; i < mOnLoadBitmapListener.size(); i++) {
						mOnLoadBitmapListener.get(i).onExternalBitmapLoaded(BitmapCache.this, mUri,
								mBtm);
					}
				}

			} else {
				mLoadingResources.put(mUri, ERROR_LOADING_IMAGE);
			}

		}

	}

	public static interface OnExternalBitmapLoadedCacheListener {

		/**
		 * This method is called when an external image (such as a network
		 * image) is loaded and stored in the cache.
		 * 
		 * @param cache
		 *            The cache that contains this bitmap.
		 * @param url
		 *            The URL that identify the bitmap
		 * @param btm
		 *            The Bitmap loaded
		 */
		public void onExternalBitmapLoaded(BitmapCache cache, String url, Bitmap btm);

	}

}
