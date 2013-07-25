package com.beyondar.android.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.beyondar.android.util.cache.BitmapCache;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.util.Log;

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
		Log.d("DebugBitmap", "++++++++++++++++++++++++++++++++++++++++++++");
		Log.d("DebugBitmap", "++++++++++++++++++++++++++++++++++++++++++++");
		Log.d("DebugBitmap", "++++++++++++++++++++++++++++++++++++++++++++");

		Integer count = 0;
		Integer recicled = 0;
		Iterator<String> keySet = sBitmapsTracker.keySet().iterator();
		while (keySet.hasNext()) {
			String string = (String) keySet.next();
			Log.d("DebugBitmap", "URI:  " + string);
			ArrayList<Bitmap> arrayList = sBitmapsTracker.get(string);
			printArrayList(arrayList, count, recicled);
		}
		Log.d("DebugBitmap", "\n\nTotal=  " + recicled + "/" + count
				+ "   recicled");
		Log.d("DebugBitmap", "---------------------------------------------");
		Log.d("DebugBitmap", "---------------------------------------------");
		Log.d("DebugBitmap", "---------------------------------------------");
	}

	private static void printArrayList(ArrayList<Bitmap> arrayList,
			Integer counter, Integer recicled) {
		if (arrayList == null || arrayList.size() == 0) {
			Log.d("DebugBitmap", "   No bitmaps");
		}
		int reciledCount = 0;
		int total = 0;
		for (Bitmap bitmap : arrayList) {
			if (bitmap == null) {
				continue;
			}
			total++;
			if (bitmap.isRecycled()) {
				reciledCount++;
			} else {
				Log.d("DebugBitmap", "      Not recicled=" + bitmap.getWidth()
						+ "x" + bitmap.getHeight());

			}
		}
		counter = counter + total;
		recicled = recicled + reciledCount;
		Log.d("DebugBitmap", "   " + reciledCount + "/" + total + " Recicled");
	}
}
