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
package com.beyondar.android.view;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.beyondar.android.util.DebugBitmap;
import com.beyondar.android.util.Logger;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback {
	public static interface BeyondarPictureCallback {
		/**
		 * This method is called when the snapshot of the camera is ready. If
		 * there is an error, the image will be null
		 * 
		 * @param picture
		 */
		void onPictureTaken(Bitmap picture);
	}

	private static final String CAMERA_PARAM_ORIENTATION = "orientation";
	private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
	private static final String CAMERA_PARAM_PORTRAIT = "portrait";

	private static final String TAG = "camera";
	private static final double ASPECT_TOLERANCE = 0.25;
	private static final int MAX_TIME_WAIT_FOR_CAMERA = 1000;

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private BeyondarPictureCallback mCameraCallback;
	private BitmapFactory.Options mOptions;

	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;
	private List<String> mSupportedFlashModes;
	
	private OnFocusableListener mOnFocusableListener;

	private boolean mIsPreviewing;

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
	
	public void setOnFocusableListener(OnFocusableListener listener) {
		mOnFocusableListener = listener;
	}

	@SuppressWarnings("deprecation")
	private void init(Context context) {
		mIsPreviewing = false;
		mHolder = getHolder();
		mHolder.addCallback(this);

		configureCamera();

		if (Build.VERSION.SDK_INT <= 10) {// Android 2.3.x or lower
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}
	
	public Camera getCamera() {
		return mCamera;
	}

	private void configureCamera() {
		if (mCamera != null) {
			return;
		}
		try {
			getTheCamera();
			// mCamera = Camera.open();
		} catch (Exception e) {
			Logger.e(TAG, "ERROR: Unable to open the camera", e);
			return;
		}
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
			// Set the camera to Auto Flash mode.
			if (mSupportedFlashModes != null
					&& mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				mCamera.setParameters(parameters);
			}
		}
	}

	public boolean isPreviewing() {
		return mCamera != null && mIsPreviewing;
	}

	public void setSupportedPreviewSizes(List<Size> supportedPreviewSizes) {
		mSupportedPreviewSizes = supportedPreviewSizes;
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
		releaseCamera();
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
		double targetRatio = (double) width / height;

		if (sizes == null)
			return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = height;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
				continue;
			}
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mCamera == null) {
			init(getContext());
		}

		if (mCamera == null || mPreviewSize == null) {
			return;
		}
		stopPreviewCamera();

		Camera.Parameters parameters = mCamera.getParameters();
		
		int orientation = 0;

		if (Build.VERSION.SDK_INT < 9) {
			if (isPortrait()) {
				parameters.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
				orientation = 90;
			} else {
				parameters.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
				orientation = 0;
			}
		} else {
			orientation = getCameraDisplayOrientation();
			parameters.setRotation(orientation);
		}
		mCamera.setDisplayOrientation(orientation);

		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

		// parameters.set("jpeg-quality", 70);
		// parameters.setPictureSize(100, 100);

		mCamera.setParameters(parameters);
		startPreviewCamera();

	}

	private int getCameraDisplayOrientation() {
		int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result = 0;
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		return result;
	}

	private boolean isPortrait() {
		return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	}

	@Override
	public void onPictureTaken(byte[] imageData, Camera camera) {
		if (imageData != null && mCameraCallback != null) {
			Bitmap btm = convertByteImage(imageData);

			mCameraCallback.onPictureTaken(btm);
		}
		startPreviewCamera();
	}
	
	public void stopPreviewCamera() {
		mIsPreviewing = false;
		if (mCamera == null) {
			return;
		}
		mOnFocusableListener.onFocusableStatus(false);
		mCamera.stopPreview();
	}
	
	public void startPreviewCamera() {
		if (mCamera == null) {
			init(getContext());
		}
		if (mCamera == null) {
			return;
		}
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			mOnFocusableListener.onFocusableStatus(true);
			mIsPreviewing = true;
		} catch (Exception e) {
			Logger.w(TAG, "Cannot start preview.", e);
		}
	}


	public void releaseCamera() {
		stopPreviewCamera();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	private Bitmap convertByteImage(byte[] imageData) {

		Bitmap myImage = DebugBitmap.decodeByteArray(imageData, 0, imageData.length, mOptions);

		imageData = null;
		System.gc();

		return myImage;
	}

	public void takePicture(BeyondarPictureCallback cameraCallback) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		// TODO: Improve this part
		options.inSampleSize = 4;
		// options.inSampleSize = 1;
		takePicture(cameraCallback, options);
	}

	public void takePicture(BeyondarPictureCallback cameraCallback, BitmapFactory.Options options) {
		if (mCamera == null) {
			return;
		}
		mCameraCallback = cameraCallback;
		mCamera.takePicture(null, this, this);
		mOptions = options;
	}

	private boolean getTheCamera() {
		Log.v(TAG, "getTheCamera");
		// keep trying to acquire the camera until "maximumWaitTimeForCamera"
		// seconds have passed
		boolean acquiredCam = false;
		int timePassed = 0;
		while (!acquiredCam && timePassed < MAX_TIME_WAIT_FOR_CAMERA) {
			try {
				mCamera = Camera.open();
				Log.v(TAG, "acquired the camera");
				acquiredCam = true;
				return true;
			} catch (Exception e) {
				Logger.e(TAG, "Exception encountered opening camera:" + e.getLocalizedMessage());
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException ee) {
				Logger.e(TAG, "Exception encountered sleeping:" + ee.getLocalizedMessage());
			}
			timePassed += 200;
		}
		return false;
	}
}
