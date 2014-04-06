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
package com.beyondar.android.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;

import com.beyondar.android.util.cache.BitmapCache;

public class DebugBitmap {

	private static final boolean DEBUG = false;

	private static final String DEFAULT_KEY = "No_key";

	private static HashMap<String, ArrayList<Bitmap>> sBitmapsTracker;

	static {
		sBitmapsTracker = new HashMap<String, ArrayList<Bitmap>>();
	}

	public synchronized static Bitmap decodeFile(String file, Options options) {

		Bitmap btm = BitmapFactory.decodeFile(file, options);

		addToTracker(file, btm);

		return btm;
	}

	public synchronized static Bitmap decodeStream(InputStream is, String uri) {

		Bitmap btm = BitmapFactory.decodeStream(is);

		addToTracker(uri, btm);

		return btm;
	}

	public synchronized static Bitmap decodeResource(Resources res, int id) {
		Bitmap btm = BitmapFactory.decodeResource(res, id);

		addToTracker(BitmapCache.generateUri(id), btm);

		return btm;
	}

	public synchronized static Bitmap decodeByteArray(byte[] data, int offset,
			int length, Options opts) {
		Bitmap btm = BitmapFactory.decodeByteArray(data, offset, length, opts);
		addToTracker(null, btm);

		return btm;
	}

	public synchronized static Bitmap decodeResource(Resources res, int id,
			Options options) {
		Bitmap btm = BitmapFactory.decodeResource(res, id, options);

		addToTracker(BitmapCache.generateUri(id), btm);

		return btm;
	}

	public synchronized static Bitmap decodeStream(InputStream is, Rect rect,
			Options options, String uri) {
		Bitmap btm = BitmapFactory.decodeStream(is, rect, options);

		addToTracker(uri, btm);

		return btm;
	}

	@SuppressWarnings("unused")
	private synchronized static void addToTracker(String uri, Bitmap btm) {
		if (!DEBUG || btm == null) {
			return;
		}
		if (uri == null) {
			uri = DEFAULT_KEY;
		}

		ArrayList<Bitmap> list = sBitmapsTracker.get(uri);
		if (list == null) {
			list = new ArrayList<Bitmap>();
			sBitmapsTracker.put(uri, list);
		}

		list.add(btm);
	}

	public synchronized static void printReport() {
		Logger.d("DebugBitmap", "++++++++++++++++++++++++++++++++++++++++++++");
		Logger.d("DebugBitmap", "++++++++++++++++++++++++++++++++++++++++++++");
		Logger.d("DebugBitmap", "++++++++++++++++++++++++++++++++++++++++++++");

		Integer count = 0;
		Integer recycled = 0;
		Iterator<String> keySet = sBitmapsTracker.keySet().iterator();
		while (keySet.hasNext()) {
			String string = (String) keySet.next();
			Logger.d("DebugBitmap", "URI:  " + string);
			ArrayList<Bitmap> arrayList = sBitmapsTracker.get(string);
			printArrayList(arrayList, count, recycled);
		}
		Logger.d("DebugBitmap", "\n\nTotal=  " + recycled + "/" + count
				+ "   recycled");
		Logger.d("DebugBitmap", "---------------------------------------------");
		Logger.d("DebugBitmap", "---------------------------------------------");
		Logger.d("DebugBitmap", "---------------------------------------------");
	}

	private static void printArrayList(ArrayList<Bitmap> arrayList,
			Integer counter, Integer recicled) {
		if (arrayList == null || arrayList.size() == 0) {
			Logger.d("DebugBitmap", "   No bitmaps");
		}
		int recycledCount = 0;
		int total = 0;
		for (Bitmap bitmap : arrayList) {
			if (bitmap == null) {
				continue;
			}
			total++;
			if (bitmap.isRecycled()) {
                recycledCount++;
			} else {
				Logger.d("DebugBitmap", "      Not recycled=" + bitmap.getWidth()
						+ "x" + bitmap.getHeight());

			}
		}
		counter = counter + total;
		recicled = recicled + recycledCount;
		Logger.d("DebugBitmap", "   " + recycledCount + "/" + total + " Recycled");
	}
}
