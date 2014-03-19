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
package com.beyondar.android.screenshot;

import android.graphics.Bitmap;

import com.beyondar.android.opengl.renderer.ARRenderer.SnapshotCallback;
import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.CameraView;
import com.beyondar.android.view.CameraView.BeyondarPictureCallback;

public class ScreenshotHelper {

	/**
	 * Take a snapshot of the BeyondarGLSurface with the camera image as
	 * background
	 * 
	 * @param cameraView
	 * @param bgls
	 * @param callback
	 */
	public static void takeScreenshot(CameraView cameraView, BeyondarGLSurfaceView bgls,
			OnScreenshotListener callback) {
		ScreenShootCallback callbackProcessing = new ScreenShootCallback(callback);

		if (cameraView != null && cameraView.isPreviewing()) {
			// CacheManager.getInventoryCache().purge();
			cameraView.takePicture(callbackProcessing);
		} else {
			callbackProcessing.onPictureTaken(null);
		}
		bgls.tackePicture(callbackProcessing);
	}

	private static class ScreenShootCallback implements BeyondarPictureCallback, SnapshotCallback {

		Bitmap btmCamera;
		Bitmap btmGl;
		volatile int status = 0;
		OnScreenshotListener callback;

		ScreenShootCallback(OnScreenshotListener cb) {
			callback = cb;
		}

		@Override
		public void onSnapshotTaken(Bitmap picture) {
			btmGl = picture;
			checkResults();
		}

		@Override
		public void onPictureTaken(Bitmap picture) {
			btmCamera = picture;
			checkResults();
		}
		
		private synchronized void checkResults() {
			status++;

			if (status == 2 && callback != null) {

				if (btmCamera == null) {
					callback.onScreenshot(btmGl);
					return;
				}
				if (btmGl == null) {
					callback.onScreenshot(btmCamera);
					return;
				}
				
				Bitmap btm = ImageUtils.mergeBitmaps(btmCamera, btmGl);
				btmCamera.recycle();
				btmGl.recycle();

				System.gc();
				callback.onScreenshot(btm);
			}
		}

	}
}
