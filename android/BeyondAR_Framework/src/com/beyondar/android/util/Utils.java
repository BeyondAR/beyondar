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

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.beyondar.android.opengl.renderer.ARRenderer.SnapshotCallback;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.CameraView;
import com.beyondar.android.view.CameraView.IPictureCallback;

public class Utils {

	public static final int MAX_SIZE = 512;

	/**
	 * Check a number if it is a power of two
	 * 
	 * @param n
	 *            The number to check
	 * @return
	 */
	public static boolean isPowerOfTwo(int n) {
		return ((n != 0) && (n & (n - 1)) == 0);
	}

	/**
	 * Check if the bitmap is compatible to use it with OpenGL, if not, use
	 * resizeImageToPowerOfTwo(bitmap) method
	 * 
	 * @param bitmap
	 *            The bitmap to check
	 * @return True if there are no problems, false otherwise
	 */
	public static boolean isCompatibleWithOpenGL(Bitmap bitmap) {
		return (Utils.isPowerOfTwo(bitmap.getHeight()) && Utils.isPowerOfTwo(bitmap.getWidth()));
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

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

	}

	/**
	 * Take a snapshot of the BeyondarGLSurface with the camera image as
	 * background
	 * 
	 * @param cameraView
	 * @param bgls
	 * @param callback
	 */
	public static void takeSnapShot(CameraView cameraView, BeyondarGLSurfaceView bgls,
			ICallBackBeyondARPicture callback) {
		ScreenShootCallback callbackProcessing = new ScreenShootCallback(callback);
		
		if (cameraView != null) {
			// CacheManager.getInventoryCache().purge();
			cameraView.tackePicture(callbackProcessing);
		} else {
			callbackProcessing.onPictureTaken(null);
		}
		bgls.tackePicture(callbackProcessing);
	}

	public static interface ICallBackBeyondARPicture {
		void onFinishSnapShotProcess(Bitmap screenshot);
	}

	private static class ScreenShootCallback implements IPictureCallback, SnapshotCallback {

		Bitmap btmCamera;
		Bitmap btmGl;
		int status = 0;
		ICallBackBeyondARPicture callback;

		ScreenShootCallback(ICallBackBeyondARPicture cb) {
			callback = cb;
		}

		@Override
		public void onSnapshootTaken(Bitmap picture) {
			btmGl = picture;
			checkResults();
		}

		@Override
		public void onPictureTaken(Bitmap picture) {
			btmCamera = ImageUtils.rotate(picture, 90);
			checkResults();
		}

		private synchronized void checkResults() {
			status++;

			if (status == 2 && callback != null) {

				if (btmCamera == null) {
					callback.onFinishSnapShotProcess(btmGl);
					return;
				}
				if (btmGl == null) {
					callback.onFinishSnapShotProcess(btmCamera);
					return;
				}

				float factor = ((float) btmGl.getWidth() / (float) btmCamera.getWidth());

				float newWidth = factor * btmCamera.getWidth();
				float newHeight = factor * btmCamera.getHeight();

				Bitmap newBtmCamera = ImageUtils.resizeImage(btmCamera, (int) newWidth, (int) newHeight);
				if (newBtmCamera != btmCamera) {
					btmCamera.recycle();
				}

				Bitmap btm = ImageUtils.mergeBitmaps(newBtmCamera, btmGl);
				newBtmCamera.recycle();

				Bitmap result = Bitmap.createBitmap(btm, 0, 0, btmGl.getWidth(), btmGl.getHeight());

				btmGl.recycle();
				btm.recycle();

				System.gc();
				callback.onFinishSnapShotProcess(result);
			}
		}
	}

}
