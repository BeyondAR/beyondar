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

import com.beyondar.android.util.math.MathUtils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class Utils {

	public static final int MAX_SIZE = 512;

	/**
	 * Check if the bitmap is compatible to use it with OpenGL, if not, use
	 * resizeImageToPowerOfTwo(bitmap) method
	 * 
	 * @param bitmap
	 *            The bitmap to check
	 * @return True if there are no problems, false otherwise
	 */
	public static boolean isCompatibleWithOpenGL(Bitmap bitmap) {
		return (MathUtils.isPowerOfTwo(bitmap.getHeight()) && MathUtils.isPowerOfTwo(bitmap.getWidth()));
	}

	/**
	 * Prepare the image to draw it using OpenGL. There are devices that doesn't
	 * support images non power of two (size). Remember to recicle die bitmap if
	 * the old one is not needed anymore
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap resizeImageToPowerOfTwo(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		int newWidth = (int) Math.pow(2, Math.ceil(Math.log10(width) / Math.log10(2)));
		int newHeight = (int) Math.pow(2, Math.ceil(Math.log10(height) / Math.log10(2)));

		if (newWidth > MAX_SIZE) {
			newWidth = MAX_SIZE;
		}

		if (newHeight > MAX_SIZE) {
			newHeight = MAX_SIZE;
		}

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}
}
