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
package com.beyondar.android.util;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class ImageUtils {

	/**
	 * Resize the image
	 * 
	 * @param bitmapOrg
	 *            The original bitmap
	 * @param newWidth
	 *            The new with
	 * @param newHeight
	 *            The new height
	 * @return
	 */
	public static Bitmap resizeImage(Bitmap bitmapOrg, int newWidth, int newHeight) {
		if (bitmapOrg == null) {
			return null;
		}
		// load the original BitMap
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, false);

	}

	/**
	 * Resize the image
	 * 
	 * @param bitmapOrg
	 *            The original bitmap
	 * @param scale
	 *            Scale to resize
	 * @return
	 */
	public static Bitmap resizeImage(Bitmap bitmapOrg, float scale) {
		if (bitmapOrg == null) {
			return null;
		}
		if (scale == 1) {
			return bitmapOrg;
		}

		// load the original BitMap
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scale, scale);
		// rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);

	}

	public static Bitmap rotate(Bitmap b, float degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();

			m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
				if (b != b2) {
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				throw ex;
			}
		}
		return b;
	}

	public static final int TIME_OUT_CONNECTION = 15000;

	/**
	 * Download the file from Internet
	 * 
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public static Bitmap LoadImageFromInternet(final String uri) throws Exception {

		URL url;
		url = new URL(uri);
		URLConnection urlConn = url.openConnection();
		urlConn.setConnectTimeout(TIME_OUT_CONNECTION);
		urlConn.setAllowUserInteraction(false);
		urlConn.setDoOutput(true);

		InputStream is = (InputStream) urlConn.getInputStream();
		return DebugBitmap.decodeStream(is, uri);
	}

	public static Bitmap mergeBitmaps(Bitmap bmp1, Bitmap bmp2) {

		int width = Math.max(bmp1.getWidth(), bmp2.getWidth());
		int height = Math.max(bmp1.getHeight(), bmp2.getHeight());

		Bitmap bmOverlay = Bitmap.createBitmap(width, height, bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);

		Bitmap bmpSized = Bitmap.createScaledBitmap(bmp1, width, height, true);
		canvas.drawBitmap(bmpSized, 0, 0, null);

		bmpSized.recycle();

		bmpSized = Bitmap.createScaledBitmap(bmp2, width, height, true);
		canvas.drawBitmap(bmpSized, 0, 0, null);

		bmpSized.recycle();

		return bmOverlay;

	}

	// Matrix matrix = new Matrix();
	// matrix.postRotate(90);
	// Bitmap pictureRotated = Bitmap.createBitmap(picture, 0, 0,
	// picture.getWidth(), picture.getHeight(), matrix, true);

	/**
	 * 
	 * Linear interpolation between two points. Return interpolated color Y at
	 * distance l.<br>
	 * 
	 * Source: http://tech-algorithm.com/articles/linear-interpolation/
	 * 
	 * @param A
	 *            ARGB for point A.
	 * @param B
	 *            ARGB for point B.
	 * @param l
	 *            Distance Y from A.
	 * @param L
	 *            Distance between A and B.
	 * @return Interpolated color Y.
	 * 
	 */
	public int linearInterpolate(int A, int B, int l, int L) {
		// extract r, g, b information
		// A and B is a ARGB-packed int so we use bit operation to extract
		int Ar = (A >> 16) & 0xff;
		int Ag = (A >> 8) & 0xff;
		int Ab = A & 0xff;
		int Br = (B >> 16) & 0xff;
		int Bg = (B >> 8) & 0xff;
		int Bb = B & 0xff;
		// now calculate Y. convert float to avoid early rounding
		// There are better ways but this is for clarity's sake
		int Yr = (int) (Ar + l * (Br - Ar) / (float) L);
		int Yg = (int) (Ag + l * (Bg - Ag) / (float) L);
		int Yb = (int) (Ab + l * (Bb - Ab) / (float) L);
		// pack ARGB with hardcoded alpha
		return 0xff000000 | // alpha
				((Yr << 16) & 0xff0000) | ((Yg << 8) & 0xff00) | (Yb & 0xff);
	}

}
