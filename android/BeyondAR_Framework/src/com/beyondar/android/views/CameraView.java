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
package com.beyondar.android.views;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.beyondar.android.util.DebugBitmap;
import com.beyondar.android.util.Logger;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback,
		Camera.PictureCallback {
	public static interface IPictureCallback {
		/**
		 * This method is called when the snapshot of the camera is ready. If
		 * there is an error, the image will be null
		 * 
		 * @param picture
		 */
		void onPictureTaken(Bitmap picture);
	}

	private static final String TAG = "camera";
	
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private IPictureCallback mCameraCallback;
	private BitmapFactory.Options mOptions;

	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;
	private List<String> mSupportedFlashModes;

	public CameraView(Context context) {
		super(context);
		init(context);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@SuppressWarnings("deprecation")
	private void init(Context context) {
		mHolder = getHolder();
		mHolder.addCallback(this);

		try {
			mCamera = Camera.open();
			setCamera(mCamera);
		} catch (Exception e) {
			Logger.e(TAG, "ERROR: Unable to open the camera", e);
		}

		if (android.os.Build.VERSION.SDK_INT <= 10) {// Android 2.3.x or lower
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
			// Set the camera to Auto Flash mode.
			if (mSupportedFlashModes != null
					&& mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				mCamera.setParameters(parameters);
			}
		}
	}

	public void setSupportedPreviewSizes(List<Size> supportedPreviewSizes) {
		mSupportedPreviewSizes = supportedPreviewSizes;
	}

	public Size getPreviewSize() {
		return mPreviewSize;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {

			if (mCamera == null) {
				init(getContext());
				if (mCamera == null) {
					return;
				}
			}

			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			if (mCamera != null) {
				mCamera.release();
			}
			mCamera = null;
			Logger.e(TAG, "CameraView -- ERROR en SurfaceCreated", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera == null) {
			return;
		}
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int width, int height) {

		Size result = null;

		for (Camera.Size size : sizes) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return result;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mCamera == null || getPreviewSize() == null) {
			return;
		}

		Camera.Parameters parameters = mCamera.getParameters();
		Size previewSize = getPreviewSize();
		parameters.setPreviewSize(previewSize.width, previewSize.height);

		mCamera.setParameters(parameters);
		previewCamera();

	}

	@Override
	public void onPictureTaken(byte[] imageData, Camera camera) {
		if (imageData != null && mCameraCallback != null) {
			mCameraCallback.onPictureTaken(StoreByteImage(imageData));
		}
		previewCamera();
	}

	public void previewCamera() {
		if (mCamera == null){
			return;
		}
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception e) {
			Logger.w(TAG, "Cannot start preview.", e);
		}
	}

	private Bitmap StoreByteImage(byte[] imageData) {

		Bitmap myImage = DebugBitmap.decodeByteArray(imageData, 0, imageData.length, mOptions);

		imageData = null;
		System.gc();

		return myImage;
	}

	public void tackePicture(IPictureCallback cameraCallback) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		tackePicture(cameraCallback, options);
	}

	public void tackePicture(IPictureCallback cameraCallback, BitmapFactory.Options options) {
		if (mCamera == null) {
			return;
		}
		mCameraCallback = cameraCallback;
		mCamera.takePicture(null, this, this);
		mOptions = options;
	}
}
