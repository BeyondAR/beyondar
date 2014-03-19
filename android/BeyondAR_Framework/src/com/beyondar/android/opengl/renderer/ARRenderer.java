/*
b * Copyright (C) 2013 BeyondAR
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
package com.beyondar.android.opengl.renderer;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.view.Surface;

import com.beyondar.android.opengl.renderable.Renderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.opengl.util.MatrixGrabber;
import com.beyondar.android.util.Logger;
import com.beyondar.android.util.PendingBitmapsToBeLoaded;
import com.beyondar.android.util.Utils;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.math.Distance;
import com.beyondar.android.util.math.MathUtils;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

// Some references:
// http://ovcharov.me/2011/01/14/android-opengl-es-ray-picking/
// http://magicscrollsofcode.blogspot.com/2010/10/3d-picking-in-android.html
public class ARRenderer implements GLSurfaceView.Renderer, SensorEventListener,
		BitmapCache.OnExternalBitmapLoadedCacheListener {

	public static interface SnapshotCallback {
		/**
		 * This method is called when the snapshot of the GL Surface is ready.
		 * If there is an error, the image will be null
		 * 
		 * @param snapshot
		 */
		void onSnapshotTaken(Bitmap snapshot);
	}

	private class UriAndBitmap {
		String uri;
		Bitmap btm;
	}

	/**
	 * Specifies the distance from the viewer to the far clipping plane. Used
	 * for the GLU.gluPerspective
	 */
	public static float Z_FAR = 100.0f;
	public static final double TIMEOUT_LOAD_TEXTURE = 1500;

	private static final String TAG = "ARRenderer";

	private float mAccelerometerValues[] = new float[3];
	private float mMagneticValues[] = new float[3];

	private float mRotationMatrix[] = new float[16];
	private float mRemappedRotationMatrix[] = new float[16];

	private MatrixGrabber mMatrixGrabber = new MatrixGrabber();
	private int mWidth, mHeight;

	private static HashMap<String, Texture> sTextureHolder = new HashMap<String, Texture>();
	private static PendingBitmapsToBeLoaded<BeyondarObject> sPendingTextureObjects = new PendingBitmapsToBeLoaded<BeyondarObject>();
	private static ArrayList<UriAndBitmap> sNewBitmapsLoaded = new ArrayList<UriAndBitmap>();
	private static final float[] sInclination = new float[16];

	private World mWorld;

	private boolean mScreenshot;
	private SnapshotCallback mSnapshotCallback;
	private SensorEventListener mExternalSensorListener;

	private boolean mIsTablet;
	private int mSurfaceRotation;

	protected Point3 cameraPosition;
	protected boolean reloadWorldTextures;

	private boolean mRender;

	private boolean mGetFps = false;

	private long mCurrentTime = System.currentTimeMillis();
	private float mFrames = 0;
	private FpsUpdatable mFpsUpdatable;

	private float mMaxDistanceSizePoints;
	private float mMinDistanceSizePoints;

	private Queue<float[]> mFloat4ArrayPool;
	private OnBeyondarObjectRenderedListener mOnBeyondarObjectRenderedListener;

	/** This list keep track of the object that have been rendered */
	private List<BeyondarObject> mRenderedObjects;

	private boolean mFillPositions;

	// This GL extension allow us to load non square textures.
	private boolean isGL_OES_texture_npot;

	public ARRenderer() {
		reloadWorldTextures = false;
		setRendering(true);
		cameraPosition = new Point3(0, 0, 0);
		mFloat4ArrayPool = new ConcurrentLinkedQueue<float[]>();

		mRenderedObjects = new ArrayList<BeyondarObject>();

		mIsTablet = false;
		mFillPositions = false;
	}

	/**
	 * Use this method to specify the renderer that is running on a tablet. If
	 * so the renderer will rotate the view to be able to be displayed on
	 * tablets
	 * 
	 * @param isTablet
	 */
	public void rotateViewForTablet(boolean isTablet) {
		mIsTablet = isTablet;
	}

	/**
	 * Set the rotation of the device:<br>
	 * Surface.ROTATION_0<br>
	 * Surface.ROTATION_90<br>
	 * Surface.ROTATION_180<br>
	 * Surface.ROTATION_270<br>
	 * 
	 * @param surfaceRotation
	 */
	public void rotateView(int surfaceRotation) {
		mSurfaceRotation = surfaceRotation;
	}

	/**
	 * Define the world where the objects are stored.
	 * 
	 * @param world
	 */
	public void setWorld(World world) {
		mWorld = world;
		mWorld.getBitmapCache().addOnExternalBitmapLoadedCahceListener(this);
		reloadWorldTextures = true;
	}

	public World getWorld() {
		return mWorld;
	}

	/**
	 * Change the camera position. It is independent to the GPS position.
	 * 
	 * @param newCameraPos
	 *            The new camera position
	 */
	public void setCameraPosition(Point3 newCameraPos) {
		cameraPosition = newCameraPos;
	}

	/**
	 * Restore the camera position
	 */
	public void restoreCameraPosition() {
		cameraPosition.x = 0;
		cameraPosition.y = 0;
		cameraPosition.z = 0;
	}

	/**
	 * Get the camera position
	 * 
	 * @return
	 */
	public Point3 getCameraPosition() {
		return cameraPosition;
	}

	public void onDrawFrame(GL10 gl) {
		if (!mRender) {
			return;
		}
		long time = System.currentTimeMillis();

		SensorManager.getInclination(sInclination);
		SensorManager.getRotationMatrix(mRotationMatrix, sInclination, mAccelerometerValues,
				mMagneticValues);
		if (mIsTablet) {
			// SensorManager.remapCoordinateSystem(mRotationMatrix,
			// SensorManager.AXIS_MINUS_Y,
			// SensorManager.AXIS_X, mRotationMatrix);
			SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
					SensorManager.AXIS_Y, mRotationMatrix);
		}

		// TODO: Optimize this code
		// TODO: Fix rotation for 270
		switch (mSurfaceRotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_Y,
					SensorManager.AXIS_X, mRotationMatrix);
			break;
		case Surface.ROTATION_90:
			break;
		case Surface.ROTATION_270:
			break;
		}

		SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_Y,
				SensorManager.AXIS_MINUS_X, mRemappedRotationMatrix);

		// Clear color buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Load remapped matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLoadMatrixf(mRemappedRotationMatrix, 0);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		// gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

		// gl.glEnable(GL10.GL_BLEND); //Turn Blending On
		// gl.glDisable(GL10.GL_DEPTH_TEST); //Turn Depth Testing Off

		// getCurrentProjection(gl);
		// getCurrentModelView(gl);

		if (sNewBitmapsLoaded.size() > 0) {
			for (int i = 0; i < sNewBitmapsLoaded.size(); i++) {
				setAllTextures(gl, sNewBitmapsLoaded.get(i).uri, sNewBitmapsLoaded.get(i).btm,
						sPendingTextureObjects);
			}
			sNewBitmapsLoaded.clear();
		}

		// Store projection and modelview matrices. This is used for the
		// collision detections
		mMatrixGrabber.getCurrentState(gl);

		if (mWorld != null) {
			if (reloadWorldTextures) {
				loadWorldTextures(gl);
				reloadWorldTextures = false;
			}
			mRenderedObjects.clear();
			renderWorld(gl, time);

			OnBeyondarObjectRenderedListener tmpTraker = mOnBeyondarObjectRenderedListener;
			if (tmpTraker != null) {
				tmpTraker.onBeyondarObjectsRendered(mRenderedObjects);
			}
		}

		if (mScreenshot) {
			mScreenshot = false;
			if (mSnapshotCallback != null) {
				mSnapshotCallback.onSnapshotTaken(savePixels(gl));
			}
		}

	}

	protected Bitmap savePixels(GL10 gl) {
		// http://stackoverflow.com/questions/3310990/taking-screenshot-of-android-opengl
		// http://www.anddev.org/how_to_get_opengl_screenshot__useful_programing_hint-t829.html
		int b[] = new int[mWidth * mHeight];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		gl.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

		// The bytes within the ints are in the wrong order for android, but
		// convert into a
		// bitmap anyway. They're also bottom-to-top rather than top-to-bottom.
		// We'll fix
		// this up soon using some fast API calls.
		Bitmap glbitmap = Bitmap.createBitmap(b, mWidth, mHeight, Bitmap.Config.ARGB_4444);
		ib = null; // we're done with ib
		b = null; // we're done with b, so allow the memory to be freed

		// To swap the color channels, we'll use a
		// ColorMatrix/ColorMatrixFilter. From the Android docs:
		//
		// This is a 5x4 matrix: [ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o,
		// p, q, r, s, t ]
		// When applied to a color [r, g, b, a] the resulting color is computed
		// as (after clamping):
		//
		// R' = a*R + b*G + c*B + d*A + e;
		// G' = f*R + g*G + h*B + i*A + j;
		// B' = k*R + l*G + m*B + n*A + o;
		// A' = p*R + q*G + r*B + s*A + t;
		//
		// We want to swap R and B, so the coefficients will be:
		// R' = B => 0,0,1,0,0
		// G' = G => 0,1,0,0,0
		// B' = R => 1,0,0,0,0
		// A' = A => 0,0,0,1,0

		final float[] cmVals = { 0, 0, 1, 0, 0, //
				0, 1, 0, 0, 0, //
				1, 0, 0, 0, 0, //
				0, 0, 0, 1, 0 };

		Paint paint = new Paint();

		// our R<->B swapping paint
		paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(cmVals)));

		// the bitmap we're going to draw onto
		Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_4444);

		// we draw to the bitmap through a canvas
		Canvas canvas = new Canvas(bitmap);
		// draw the opengl bitmap onto the canvas, using the color swapping
		// paint
		canvas.drawBitmap(glbitmap, 0, 0, paint);

		glbitmap.recycle();
		glbitmap = null; // we're done with glbitmap, let go of its memory

		// the image is still upside-down, so vertically flip it
		Matrix matrix = new Matrix();
		matrix.preScale(1.0f, -1.0f); // scaling: x = x, y = -y, i.e. vertically
										// flip

		// new bitmap, using the flipping matrix
		Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
				matrix, true);

		bitmap.recycle();
		System.gc();
		return result;
	}

	private static final double[] sOut = new double[3];

	protected void convertGPStoPoint3(GeoObject geoObject, Point3 out) {
		float x, z, y;
		x = (float) (Distance.fastConversionGeopointsToMeters(geoObject.getLongitude()
				- mWorld.getLongitude()) / 2);
		z = (float) (Distance.fastConversionGeopointsToMeters(geoObject.getAltitude()
				- mWorld.getAltitude()) / 2);
		y = (float) (Distance.fastConversionGeopointsToMeters(geoObject.getLatitude()
				- mWorld.getLatitude()) / 2);

		if (mMaxDistanceSizePoints > 0 || mMinDistanceSizePoints > 0) {
			double totalDst = Distance.calculateDistance(x, y, 0, 0);

			if (mMaxDistanceSizePoints > 0 && totalDst > mMaxDistanceSizePoints) {
				MathUtils.linearInterpolate(0, 0, 0, x, y, 0, mMaxDistanceSizePoints, totalDst,
						sOut);
				x = (float) sOut[0];
				y = (float) sOut[1];
				if (mMinDistanceSizePoints > 0) {
					totalDst = Distance.calculateDistance(x, y, 0, 0);
				}
			}
			if (mMinDistanceSizePoints > 0 && totalDst < mMinDistanceSizePoints) {
				MathUtils.linearInterpolate(0, 0, 0, x, y, 0, mMinDistanceSizePoints, totalDst,
						sOut);
				x = (float) sOut[0];
				y = (float) sOut[1];
			}
		}

		out.x = x;
		out.y = y;
		out.z = z;
	}

	/**
	 * When a {@link GeoObject} is rendered according to its position it could
	 * look very small if it is far away. Use this method to render far objects
	 * as if there were closer.<br>
	 * For instance if there is an object at 100 meters and we want to have
	 * everything at least at 25 meters, we could use this method for that
	 * purpose. <br>
	 * To set it to the default behavior just set it to 0
	 * 
	 * @param maxDistanceSize
	 *            The top far distance (in meters) which we want to draw a
	 *            {@link GeoObject} , 0 to set again the default behavior
	 */
	public void setMaxDistanceSize(float maxDistanceSize) {
		mMaxDistanceSizePoints = (float) (maxDistanceSize / 2);
	}

	/**
	 * Get the max distance which a {@link GeoObject} will be rendered.
	 * 
	 * @return The current max distance. 0 is the default behavior
	 */
	public float getMaxDistanceSize() {
		return (float) (mMaxDistanceSizePoints * 2);
	}

	/**
	 * When a {@link GeoObject} is rendered according to its position it could
	 * look very big if it is too close. Use this method to render near objects
	 * as if there were farther.<br>
	 * For instance if there is an object at 1 meters and we want to have
	 * everything at least at 10 meters, we could use this method for that
	 * purpose. <br>
	 * To set it to the default behavior just set it to 0
	 * 
	 * @param minDistanceSize
	 *            The top near distance (in meters) which we want to draw a
	 *            {@link GeoObject} , 0 to set again the default behavior
	 */
	public void setMinDistanceSize(float minDistanceSize) {
		mMinDistanceSizePoints = (float) (minDistanceSize / 2);
	}

	/**
	 * Get the minimum distance which a {@link GeoObject} will be rendered.
	 * 
	 * @return The current minimum distance. 0 is the default behavior
	 */
	public float getMinDistanceSize() {
		return (float) (mMinDistanceSizePoints * 2);
	}

	/**
	 * Set the {@link FpsUpdatable} to get notified about the frames per
	 * seconds.
	 * 
	 * @param fpsUpdatable
	 *            The event listener. Use null to remove the
	 *            {@link FpsUpdatable}
	 */
	public void setFpsUpdatable(FpsUpdatable fpsUpdatable) {
		mCurrentTime = System.currentTimeMillis();
		mFpsUpdatable = fpsUpdatable;
		mGetFps = mFpsUpdatable != null;
	}

	public void setOnBeyondarObjectRenderedListener(OnBeyondarObjectRenderedListener rendererTracker) {
		mOnBeyondarObjectRenderedListener = rendererTracker;
	}

	protected void renderWorld(GL10 gl, long time) {
		BeyondarObjectList list = null;
		try {
			for (int i = 0; i < mWorld.getBeyondarObjectLists().size(); i++) {
				list = mWorld.getBeyondarObjectLists().get(i);
				if (list != null) {
					renderList(gl, list, time);
				}
			}
		} catch (ConcurrentModificationException e) {
		}
		mWorld.forceProcessRemoveQueue();

		if (mGetFps) {
			mFrames++;
			final long timeInterval = System.currentTimeMillis() - mCurrentTime;
			if (timeInterval > 1000) {
				if (Logger.DEBUG_OPENGL) {
					Logger.d("Frames/second:  " + mFrames / (timeInterval / 1000F));
				}
				if (mFpsUpdatable != null) {
					mFpsUpdatable.onFpsUpdate(mFrames / (timeInterval / 1000F));
				}
				mFrames = 0;
				mCurrentTime = System.currentTimeMillis();
			}
		}
	}

	protected void renderList(GL10 gl, BeyondarObjectList list, long time) {

		Texture listTexture = list.getTexture();

		if (!listTexture.isLoaded()) {
			Texture defaultTexture = sTextureHolder.get(list.getDefaultBitmapURI());
			if (defaultTexture == null || !defaultTexture.isLoaded()) {
				Logger.w("Warning!! The default texture for the list \"" + list.getType()
						+ "\" has not been loaded. Trying to load it now...");
				Bitmap defaultBtm = mWorld.getBitmapCache().getBitmap(list.getDefaultBitmapURI());
				defaultTexture = load2DTexture(gl, defaultBtm);
			}
			list.setTexture(defaultTexture == null ? null : defaultTexture.clone());
		}

		for (int j = 0; j < list.size(); j++) {
			BeyondarObject beyondarObject = list.get(j);
			if (beyondarObject == null) {
				continue;
			}
			renderBeyondarObject(gl, beyondarObject, listTexture, time);
		}
	}

	private float[] tmpEyeForRendering = new float[4];

	/**
	 * Override this method to customize the way to render the objects
	 * 
	 * @param gl
	 * @param beyondarObject
	 * @param defaultTexture
	 */
	protected void renderBeyondarObject(GL10 gl, BeyondarObject beyondarObject,
			Texture defaultTexture, long time) {
		boolean renderObject = false;
		Renderable renderable = beyondarObject.getOpenGLObject();

		if (renderable == null || !beyondarObject.isVisible()) {
			return;
		}
		double dst = 0;
		if (beyondarObject instanceof GeoObject) {
			dst = ((GeoObject) beyondarObject).calculateDistanceMeters(mWorld.getLongitude(),
					mWorld.getLatitude());
			convertGPStoPoint3((GeoObject) beyondarObject, beyondarObject.getPosition());
		} else {
			Point3 position = beyondarObject.getPosition();
			dst = MathUtils.GLUnitsToMeters((float) Distance.calculateDistanceCoordinates(0, 0, 0,
					position.x, position.y, position.z));
		}

		beyondarObject.setDistanceFromUser(dst);

		if (dst < mWorld.getArViewDistance()) {
			renderObject = true;
		}

		boolean forceDraw = renderable.update(time, (float) dst, beyondarObject);

		if (forceDraw || renderObject) {
			if (beyondarObject.isFacingToCamera()) {
				MathUtils.calcAngleFaceToCamera(beyondarObject.getPosition(), cameraPosition,
						beyondarObject.getAngle());
			}

			if (!beyondarObject.getTexture().isLoaded() && beyondarObject.getBitmapUri() != null) {
				int counter = beyondarObject.getTexture().getLoadTryCounter();
				double timeOut = TIMEOUT_LOAD_TEXTURE * (counter + 1);
				if (beyondarObject.getTexture().getTimeStamp() == 0
						|| time - beyondarObject.getTexture().getTimeStamp() > timeOut) {
					Logger.d("Loading new textures...");
					loadBeyondarObjectTexture(gl, beyondarObject);
					if (!beyondarObject.getTexture().isLoaded()) {
						beyondarObject.getTexture().setTimeStamp(time);
						beyondarObject.getTexture().setLoadTryCounter(counter + 1);
					}
				}
			}
			renderable.draw(gl, defaultTexture);
			getScreenCoordinates(beyondarObject.getPosition(),
					beyondarObject.getScreenPositionCenter(), tmpEyeForRendering);

			if (mFillPositions) {
				fillBeyondarObjectPositions(beyondarObject);
			}
			mRenderedObjects.add(beyondarObject);
		} else {
			renderable.onNotRendered(dst);
		}
	}

	/**
	 * Use this method to fill all the screen positions of the
	 * {@link BeyondarObject}. After this method is called you can use the
	 * following:<br>
	 * {@link BeyondarObject#getScreenPositionBottomLeft()}<br>
	 * {@link BeyondarObject#getScreenPositionBottomRight()}<br>
	 * {@link BeyondarObject#getScreenPositionTopLeft()}<br>
	 * {@link BeyondarObject#getScreenPositionTopRight()}
	 * 
	 * @param beyondarObject
	 *            The {@link BeyondarObject} to compute
	 */
	public void fillBeyondarObjectPositions(BeyondarObject beyondarObject) {
		getScreenCoordinates(beyondarObject.getBottomLeft(),
				beyondarObject.getScreenPositionBottomLeft());
		getScreenCoordinates(beyondarObject.getBottomRight(),
				beyondarObject.getScreenPositionBottomRight());
		getScreenCoordinates(beyondarObject.getTopLeft(), beyondarObject.getScreenPositionTopLeft());
		getScreenCoordinates(beyondarObject.getTopRight(),
				beyondarObject.getScreenPositionTopRight());
	}

	/**
	 * Take an snapshot of the view.
	 * 
	 * @param callBack
	 */
	public void tackePicture(SnapshotCallback callBack) {
		mSnapshotCallback = callBack;
		mScreenshot = true;
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// gl.glFrustumf(-ratio, ratio, -1, 1, 1f, 100);

		GLU.gluPerspective(gl, 45.0f, ratio, 0.1f, Z_FAR);
		mWidth = width;
		mHeight = height;
		setupViewPort();
	}

	private void checkGlExtensions(GL10 gl) {

		String extensions = gl.glGetString(GL10.GL_EXTENSIONS);

		if (extensions.contains("GL_OES_texture_npot")) {
			isGL_OES_texture_npot = true;
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		// Let's check the available OpenGL Extensions:
		checkGlExtensions(gl);

		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		gl.glDisable(GL10.GL_DITHER);

		gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping ( NEW )
		gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
		gl.glClearDepthf(1.0f); // Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
		// gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

		gl.glEnable(GL10.GL_BLEND); // Enable blending
		// GL10.GL_REPLACE;
		// gl.glDisable(GL10.GL_DEPTH_TEST); // Disable depth test
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		// gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE); // Set The Blending
		// Function For
		// Translucency

		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL10.GL_NOTEQUAL, 0);

		/*
		 * Some one-time OpenGL initialization can be made here probably based
		 * on features of this particular context
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		gl.glEnable(GL10.GL_CULL_FACE);

		// TODO What is the best choice?
		// Really Nice Perspective Calculations
		// gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		// gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		// GL10.GL_REPLACE);

		gl.glClearColor(0, 0, 0, 0);

		sTextureHolder.clear();
		Logger.d(TAG, "Loading textures...");
		loadWorldTextures(gl);
		loadAdditionalTextures(gl);
		Logger.d(TAG, "TEXTURES LOADED");

	}

	/**
	 * Override this method to load additional textures. Use the following code
	 * to help you:<br>
	 * <code>loadTexture(gl, yourBitmap)</code><br>
	 * This method is called when the surface is created.
	 * 
	 * @param gl
	 */
	protected void loadAdditionalTextures(GL10 gl) {
	}

	/**
	 * Load the world textures
	 * 
	 * @param gl
	 */
	private void loadWorldTextures(GL10 gl) {
		if (null != mWorld) {
			try {
				BeyondarObjectList list = null;
				for (int i = 0; i < mWorld.getBeyondarObjectLists().size(); i++) {
					list = mWorld.getBeyondarObjectLists().get(i);
					if (null != list) {
						Bitmap defaultBtm = mWorld.getBitmapCache().getBitmap(
								list.getDefaultBitmapURI());
						Texture texture = load2DTexture(gl, defaultBtm);
						list.setTexture(texture);

						for (int j = 0; j < list.size(); j++) {
							// loading texture
							loadBeyondarObjectTexture(gl, list.get(j));

						}
					}
				}
			} catch (ConcurrentModificationException e) {
				loadWorldTextures(gl);
			}
		}
		mWorld.getBitmapCache().cleanRecylcedBitmaps();
	}

	/**
	 * Load the textures of the specified geoObject
	 * 
	 * @param gl
	 * @param geoObject
	 *            The object to load the textures
	 */
	protected void loadBeyondarObjectTexture(GL10 gl, BeyondarObject geoObject) {

		Texture texture = getTexture(geoObject.getBitmapUri());

		if (texture == null) {
			Bitmap btm = mWorld.getBitmapCache().getBitmap(geoObject.getBitmapUri());

			texture = loadBitmapTexture(gl, btm, geoObject.getBitmapUri());

			if (texture == null || !texture.isLoaded()) {
				sPendingTextureObjects.addObject(geoObject.getBitmapUri(), geoObject);
			}
			if (btm == null) {
				if (Logger.DEBUG_OPENGL) {
					Logger.e(TAG, "ERROR: the resource " + geoObject.getBitmapUri()
							+ " has not been loaded. Object Name: " + geoObject.getName());
				}
			}
		}

		geoObject.setTexture(texture);
	}

	/**
	 * Load the {@link Texture} from a {@link Bitmap}
	 * 
	 * @param gl
	 * @param btm
	 *            The {@link Bitmap} to load
	 * @param uri
	 *            The unique id of the bitmap
	 * @return The {@link Texture} object
	 */
	protected Texture loadBitmapTexture(GL10 gl, Bitmap btm, String uri) {

		if (null == btm) {
			return null;
		}
		Texture texture = null;
		// Check if the texture already exist
		texture = sTextureHolder.get(uri);
		if (texture == null) {
			texture = load2DTexture(gl, btm);

			sTextureHolder.put(uri, texture);
		}
		return texture.clone();
	}

	/**
	 * Check if the bitmap texture object is loaded. To check if the
	 * {@link Texture} has been loaded with a pointer, use
	 * {@link Texture#isLoaded()}
	 * 
	 * @param bitmap
	 * @return true if it is already loaded, false otherwise.
	 */
	protected boolean isTextureLoaded(Bitmap bitmap) {
		return sTextureHolder.containsValue(bitmap);
	}

	/**
	 * Check if the image URI has been loaded as a texture. To check if the
	 * {@link Texture} has any GL texture pointer use {@link Texture#isLoaded()}
	 * 
	 * @param uri
	 * @return true if it is already loaded, false otherwise.
	 */
	protected boolean isTextureObjectLoaded(String uri) {
		return sTextureHolder.get(uri) != null;
	}

	/**
	 * get a copy of the texture pointer for the specified URI
	 * 
	 * @param uri
	 * @return
	 */
	protected Texture getTexture(String uri) {
		if (uri == null) {
			return null;
		}
		Texture texture = sTextureHolder.get(uri);
		if (texture != null) {
			texture = texture.clone();
		}
		return texture;
	}

	/**
	 * Create the texture for the specified {@link Bitmap}.<br>
	 * <b>NOTE:</b> The method will recycle the bitmap after being used
	 * 
	 * @param gl
	 * @param bitmap
	 * @return
	 */
	protected Texture load2DTexture(GL10 gl, Bitmap bitmap) {
		// see
		// http://stackoverflow.com/questions/3921685/issues-with-glutils-teximage2d-and-alpha-in-textures
		int[] tmpTexture = new int[1];
		if (null == bitmap) {
			return null;
		}
		int imageWidth = bitmap.getWidth();
		int imageHeight = bitmap.getHeight();

		if (!isGL_OES_texture_npot && !Utils.isCompatibleWithOpenGL(bitmap)) {
			Bitmap tmp = Utils.resizeImageToPowerOfTwo(bitmap);
			bitmap.recycle();
			bitmap = tmp;
		}

		// generate one texture pointer
		gl.glGenTextures(1, tmpTexture, 0);
		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tmpTexture[0]);

		// create nearest filtered texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Different possible texture parameters, e.g.
		// GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		//
		// Use Android GLUtils to specify a two-dimensional
		// texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		//
		// Clean up
		bitmap.recycle();
		return new Texture(tmpTexture[0]).setImageSize(imageWidth, imageHeight);

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (mExternalSensorListener != null) {
			mExternalSensorListener.onAccuracyChanged(sensor, accuracy);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!mRender) {
			return;
		}
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			LowPassFilter.filter(event.values, mAccelerometerValues);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			LowPassFilter.filter(event.values, mMagneticValues);
			break;
		default:
			break;
		}

		if (mExternalSensorListener != null) {
			mExternalSensorListener.onSensorChanged(event);
		}
	}

	// view port
	private final int[] viewport = new int[4];

	private void setupViewPort() {
		viewport[2] = mWidth;
		viewport[3] = mHeight;
	}

	public void getViewRay(float x, float y, Ray ray) {

		// far eye point
		float[] eye = mFloat4ArrayPool.poll();
		if (eye == null) {
			eye = new float[4];
		} else {
			eye[0] = eye[1] = eye[2] = eye[3] = 0;
		}
		GLU.gluUnProject(x, mHeight - y, 0.9f, mMatrixGrabber.mModelView, 0,
				mMatrixGrabber.mProjection, 0, viewport, 0, eye, 0);

		// fix
		if (eye[3] != 0) {
			eye[0] = eye[0] / eye[3];
			eye[1] = eye[1] / eye[3];
			eye[2] = eye[2] / eye[3];
		}

		// ray vector
		ray.setVector((eye[0] - cameraPosition.x), (eye[1] - cameraPosition.y),
				(eye[2] - cameraPosition.z));
		mFloat4ArrayPool.add(eye);
	}

	public void getScreenCoordinates(Point3 position, Point3 point) {
		float[] eye = mFloat4ArrayPool.poll();
		if (eye == null) {
			eye = new float[4];
		}
		getScreenCoordinates(position.x, position.y, position.z, point, mFloat4ArrayPool.poll());
		mFloat4ArrayPool.add(eye);
	}

	public void getScreenCoordinates(Point3 position, Point3 point, float[] eye) {
		getScreenCoordinates(position.x, position.y, position.z, point, eye);
	}

	public void getScreenCoordinates(float x, float y, float z, Point3 point, float[] eye) {
		// far eye point
		if (eye == null) {
			eye = new float[4];
		} else {
			eye[0] = eye[1] = eye[2] = eye[3] = 0;
		}

		GLU.gluProject(x, y, z, mMatrixGrabber.mModelView, 0, mMatrixGrabber.mProjection, 0,
				viewport, 0, eye, 0);

		// fix
		if (eye[3] != 0) {
			eye[0] = eye[0] / eye[3];
			eye[1] = eye[1] / eye[3];
			eye[2] = eye[2] / eye[3];
		}

		// Screen coordinates
		point.x = eye[0];
		point.y = mHeight - eye[1];
		point.z = eye[2];
	}

	/**
	 * Specify if the {@link ARRenderer} should render the world.
	 * 
	 * @param render
	 */
	public void setRendering(boolean render) {
		mRender = render;
	}

	/**
	 * Get known if the {@link ARRenderer} is rendering the world
	 * 
	 * @return
	 */
	public boolean isRendering() {
		return mRender;
	}

	@Override
	public void onExternalBitmapLoaded(BitmapCache cache, String url, Bitmap btm) {
		UriAndBitmap uriAndBitmap = new UriAndBitmap();
		uriAndBitmap.uri = url;
		uriAndBitmap.btm = btm;
		sNewBitmapsLoaded.add(uriAndBitmap);
	}

	public synchronized void setAllTextures(GL10 gl, String uri, Bitmap btm,
			PendingBitmapsToBeLoaded<BeyondarObject> pendingList) {
		if (uri == null) {
			return;
		}
		ArrayList<BeyondarObject> list = pendingList.getPendingList(uri);
		if (list == null) {
			return;
		}

		Texture texture = loadBitmapTexture(gl, btm, uri);

		for (int i = 0; i < list.size() && texture.isLoaded(); i++) {
			BeyondarObject object = list.get(i);
			object.setTexture(texture);
		}
		pendingList.removePendingList(uri);
	}

	public void forceFillBeyondarObjectPositions(boolean fill) {
		mFillPositions = fill;
	}

	public static interface FpsUpdatable {

		static final String __ON_FPS_UPDATE_METHOD_NAME__ = "onFpsUpdate";

		/**
		 * This method will get the frames per second rendered by the
		 * {@link ARRenderer}
		 * 
		 * @param fps
		 *            The Frames per second rendered
		 */
		public void onFpsUpdate(float fps);
	}
}