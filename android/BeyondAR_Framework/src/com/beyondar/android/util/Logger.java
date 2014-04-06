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

import android.util.Log;

/**
 * Logger class for BeyondAR framework
 */
public class Logger {

	/** All the BeyondAR logs output have this tag */
	public static final String TAG = "BeyondAR";

	public volatile static boolean DEBUG = true;

	/**
	 * Set this flag to enable the OpenGL debug log. If You use this, the touch
	 * events will not work! use only to debug the openGL Draw stuff
	 */
	public static boolean DEBUG_OPENGL = false;

	public static void e(String msg) {
		e(null, msg);
	}

	public static void e(String msg, Throwable tr) {
		e(null, msg, tr);
	}

	public static void e(String tag, String msg) {
		if (!DEBUG)
			return;
		Log.e(generateTag(tag), msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (!DEBUG)
			return;
		Log.e(generateTag(tag), msg, tr);
	}

	public static void i(String msg) {
		if (!DEBUG)
			return;
		i(null, msg);
	}

	public static void i(String msg, Throwable tr) {
		i(null, msg, tr);
	}

	public static void i(String tag, String msg) {
		if (!DEBUG)
			return;
		Log.i(generateTag(tag), msg);
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (!DEBUG)
			return;
		Log.i(generateTag(tag), msg, tr);
	}

	public static void d(String msg) {
		d(null, msg);
	}

	public static void d(String msg, Throwable tr) {
		d(null, msg, tr);
	}

	public static void d(String tag, String msg) {
		if (!DEBUG)
			return;
		Log.d(generateTag(tag), msg);
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (!DEBUG)
			return;
		Log.d(generateTag(tag), msg, tr);
	}

	public static void v(String msg) {
		v(null, msg);
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (!DEBUG)
			return;
		Log.v(generateTag(tag), msg, tr);
	}

	public static void v(String tag, String msg) {
		if (!DEBUG)
			return;
		Log.v(generateTag(tag), msg);
	}

	public static void v(String msg, Throwable tr) {
		if (!DEBUG)
			return;
		Log.v(TAG, msg, tr);
	}

	public static void w(String msg) {
		w(null, msg);
	}

	public static void w(String msg, Throwable tr) {
		w(null, msg, tr);
	}

	public static void w(String tag, String msg) {
		if (!DEBUG)
			return;
		Log.w(generateTag(tag), msg);
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (!DEBUG)
			return;
		Log.w(generateTag(tag), msg, tr);
	}

	private static String generateTag(String sufixTag) {
		if (sufixTag == null) {
			return TAG;
		}
		return TAG + "_" + sufixTag;
	}

}
