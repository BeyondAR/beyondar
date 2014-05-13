/*
b * Copyright (C) 2014 BeyondAR
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
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.view.Surface;

import com.beyondar.android.opengl.renderable.Renderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.opengl.util.MatrixGrabber;
import com.beyondar.android.plugin.GLPlugin;
import com.beyondar.android.plugin.Plugable;
import com.beyondar.android.sensor.BeyondarSensorListener;
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

/**
 * Renderer for drawing the {@link com.beyondar.android.world.World World} with
 * OpenGL.
 */
public class ARRenderer implements GLSurfaceView.Renderer, BeyondarSensorListener,
		BitmapCache.OnExternalBitmapLoadedCacheListener, Plugable<GLPlugin> {

	/**
	 * Callback to get notified when the snapshot of the OpenGL view is taken.
	 */
	public static interface GLSnapshotCallback {
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
	 * The default maximum distance that the object will be displayed (meters)
	 * in the AR view.
	 */
	public static final float DEFAULT_MAX_AR_VIEW_DISTANCE = 100;
	
	public static final float DEFAULT_DISTANCE_FACTOR = 2;

	/**
	 * Specifies the distance from the viewer to the far clipping plane. Used
	 * for the GLU.gluPerspective
	 */
	public static float Z_FAR = 400.0f;
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

	private float mArViewDistance;
	private float mDistanceFactor;

	private World mWorld;

	/** List of loaded plugins. */
	protected List<GLPlugin> plugins;
	/**
	 * Lock to synchronize the access to the plugins. Use it to modify the
	 * loaded plugins.
	 */
	protected Object lockPlugins = new Object();

	private boolean mScreenshot;
	private GLSnapshotCallback mSnapshotCallback;

	private int mSurfaceRotation;

	private Point3 mCameraPosition;

	private boolean mReloadWorldTextures;

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

	/**
	 * {@link ARRenderer} constructor.
	 */
	public ARRenderer() {
		mArViewDistance = DEFAULT_MAX_AR_VIEW_DISTANCE;
		mDistanceFactor = DEFAULT_DISTANCE_FACTOR;
		mReloadWorldTextures = false;
		setRendering(true);
		mCameraPosition = new Point3(0, 0, 0);
		mFloat4ArrayPool = new ConcurrentLinkedQueue<float[]>();

		mRenderedObjects = new ArrayList<BeyondarObject>();

		mFillPositions = false;

		plugins = new ArrayList<GLPlugin>();
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
		mReloadWorldTextures = true;
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				plugin.setup(mWorld, this);
			}
		}
	}

	/**
	 * Get the {@link com.beyondar.android.world.World World} used for by the
	 * {@link ARRenderer}.
	 * 
	 * @return
	 */
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
		mCameraPosition = newCameraPos;
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				plugin.onCameraPositionChanged(newCameraPos);
			}
		}
	}

	/**
	 * Restore the camera position
	 */
	public void restoreCameraPosition() {
		mCameraPosition.x = 0;
		mCameraPosition.y = 0;
		mCameraPosition.z = 0;
		setCameraPosition(mCameraPosition);
	}

	/**
	 * Get the camera position
	 * 
	 * @return
	 */
	public Point3 getCameraPosition() {
		return mCameraPosition;
	}

	public void onDrawFrame(GL10 gl) {
		if (!mRender) {
			return;
		}
		long time = System.currentTimeMillis();

		SensorManager.getInclination(sInclination);
		SensorManager.getRotationMatrix(mRotationMatrix, sInclination, mAccelerometerValues, mMagneticValues);

		float rotation = 0;
		switch (mSurfaceRotation) {
		case Surface.ROTATION_0:
			rotation = 270;
			break;
		case Surface.ROTATION_90:
			break;
		case Surface.ROTATION_180:
			rotation = 90;
			break;
		case Surface.ROTATION_270:
			rotation = 180;
			break;
		}
		
		gl.glRotatef(rotation, 0, 0, 1);

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
			if (mReloadWorldTextures) {
				loadWorldTextures(gl);
				mReloadWorldTextures = false;
			}
			mRenderedObjects.clear();
			renderWorld(gl, time);

			OnBeyondarObjectRenderedListener tmpTraker = mOnBeyondarObjectRenderedListener;
			if (tmpTraker != null) {
				tmpTraker.onBeyondarObjectsRendered(mRenderedObjects);
			}
		}

		try {
			for (GLPlugin plugin : plugins) {
				plugin.onFrameRendered(gl);
			}
		} catch (ConcurrentModificationException e) {
			Logger.w("Some plugins where changed while drawing a frame");
		}

		if (mScreenshot) {
			mScreenshot = false;
			if (mSnapshotCallback != null) {
				mSnapshotCallback.onSnapshotTaken(savePixels(gl));
			}
		}

	}

	/**
	 * Generate a new bitmap from the gl.
	 * 
	 * @param gl
	 * @return A new Bitmap of the last frame.
	 */
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
		Bitmap result = Bitmap
				.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		bitmap.recycle();
		System.gc();
		return result;
	}

	private static final double[] sOut = new double[3];

	/**
	 * Convert the geolocation to gl points representation.
	 * 
	 * @param geoObject
	 *            The {@link com.beyondar.android.world.GeoObject GeoObject} to
	 *            be processed.
	 * @param out
	 *            Where the results be stored.
	 */
	protected void convertGPStoPoint3(GeoObject geoObject, Point3 out) {
		float x, z, y;
		x = (float) (Distance.fastConversionGeopointsToMeters(geoObject.getLongitude()
				- mWorld.getLongitude()) / mDistanceFactor);
		z = (float) (Distance.fastConversionGeopointsToMeters(geoObject.getAltitude() 
				- mWorld.getAltitude()) / mDistanceFactor);
		y = (float) (Distance.fastConversionGeopointsToMeters(geoObject.getLatitude() 
				- mWorld.getLatitude()) / mDistanceFactor);

		if (mMaxDistanceSizePoints > 0 || mMinDistanceSizePoints > 0) {
			double totalDst = Distance.calculateDistance(x, y, 0, 0);

			if (mMaxDistanceSizePoints > 0 && totalDst > mMaxDistanceSizePoints) {
				MathUtils.linearInterpolate(0, 0, 0, x, y, 0, mMaxDistanceSizePoints, totalDst, sOut);
				x = (float) sOut[0];
				y = (float) sOut[1];
				if (mMinDistanceSizePoints > 0) {
					totalDst = Distance.calculateDistance(x, y, 0, 0);
				}
			}
			if (mMinDistanceSizePoints > 0 && totalDst < mMinDistanceSizePoints) {
				MathUtils.linearInterpolate(0, 0, 0, x, y, 0, mMinDistanceSizePoints, totalDst, sOut);
				x = (float) sOut[0];
				y = (float) sOut[1];
			}
		}

		out.x = x;
		out.y = y;
		out.z = z;
	}

	/**
	 * When a {@link com.beyondar.android.world.GeoObject GeoObject} is rendered
	 * according to its position it could look very small if it is far away. Use
	 * this method to render far objects as if there were closer.<br>
	 * For instance if there is an object at 100 meters and we want to have
	 * everything at least at 25 meters, we could use this method for that
	 * purpose. <br>
	 * To set it to the default behavior just set it to 0
	 * 
	 * @param maxDistanceSize
	 *            The top far distance (in meters) which we want to draw a
	 *            {@link com.beyondar.android.world.GeoObject GeoObject} , 0 to
	 *            set again the default behavior
	 */
	public void setPullCloserDistance(float maxDistanceSize) {
		mMaxDistanceSizePoints = (float) (maxDistanceSize / 2);
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				plugin.onMaxDistanceSizeChanged(mMaxDistanceSizePoints);
			}
		}
	}

	/**
	 * Get the max distance which a {@link com.beyondar.android.world.GeoObject
	 * GeoObject} will be rendered.
	 * 
	 * @return The current max distance. 0 is the default behavior
	 */
	public float getPullCloserDistance() {
		return (float) (mMaxDistanceSizePoints * 2);
	}

	/**
	 * When a {@link com.beyondar.android.world.GeoObject GeoObject} is rendered
	 * according to its position it could look very big if it is too close. Use
	 * this method to render near objects as if there were farther.<br>
	 * For instance if there is an object at 1 meters and we want to have
	 * everything at least at 10 meters, we could use this method for that
	 * purpose. <br>
	 * To set it to the default behavior just set it to 0
	 * 
	 * @param minDistanceSize
	 *            The top near distance (in meters) which we want to draw a
	 *            {@link com.beyondar.android.world.GeoObject GeoObject} , 0 to
	 *            set again the default behavior
	 */
	public void setPushAwayDistance(float minDistanceSize) {
		mMinDistanceSizePoints = (float) (minDistanceSize / 2);
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				plugin.onMaxDistanceSizeChanged(mMinDistanceSizePoints);
			}
		}
	}

	/**
	 * Get the minimum distance which a
	 * {@link com.beyondar.android.world.GeoObject GeoObject} will be rendered.
	 * 
	 * @return The current minimum distance. 0 is the default behavior
	 */
	public float getPushAwayDistance() {
		return (float) (mMinDistanceSizePoints * 2);
	}

	/**
	 * Set the
	 * {@link com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable
	 * FpsUpdatable} to get notified about the frames per seconds.
	 * 
	 * @param fpsUpdatable
	 *            The event listener. Use null to remove the
	 *            {@link com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable
	 *            FpsUpdatable}
	 */
	public void setFpsUpdatable(FpsUpdatable fpsUpdatable) {
		mCurrentTime = System.currentTimeMillis();
		mFpsUpdatable = fpsUpdatable;
		mGetFps = mFpsUpdatable != null;
	}

	public void setOnBeyondarObjectRenderedListener(OnBeyondarObjectRenderedListener rendererTracker) {
		mOnBeyondarObjectRenderedListener = rendererTracker;
	}

	/**
	 * Render the {@link com.beyondar.android.world.World World}.
	 * 
	 * @param gl
	 * @param time
	 *            Time mark to be used for drawing the frame.
	 */
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

	/**
	 * Render a specific list.
	 * 
	 * @param gl
	 * @param list
	 *            List to render.
	 * @param time
	 *            Time mark to be used for drawing the frame.
	 */
	protected void renderList(GL10 gl, BeyondarObjectList list, long time) {

		Texture listTexture = list.getDefaultTexture();

		if (!listTexture.isLoaded()) {
			Texture defaultTexture = sTextureHolder.get(list.getDefaultImageUri());
			if (defaultTexture == null || !defaultTexture.isLoaded()) {
				Logger.w("Warning!! The default texture for the list \"" + list.getType()
						+ "\" has not been loaded. Trying to load it now...");
				Bitmap defaultBtm = mWorld.getBitmapCache().getBitmap(list.getDefaultImageUri());
				defaultTexture = load2DTexture(gl, defaultBtm);
			}
			list.setDefaultTexture(defaultTexture == null ? null : defaultTexture.clone());
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
	 * Render a specific {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject}.
	 * 
	 * @param gl
	 * @param beyondarObject
	 *            {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject} to render.
	 * @param defaultTexture
	 *            If the texture from the
	 *            {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject} is not available the defaultTexure will be
	 *            used.
	 */
	protected void renderBeyondarObject(GL10 gl, BeyondarObject beyondarObject, Texture defaultTexture,
			long time) {
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

		if (dst < mArViewDistance) {
			renderObject = true;
		}
		
		boolean forceDraw = renderable.update(time, (float) dst, beyondarObject);

		if (forceDraw || renderObject) {
			if (beyondarObject.isFacingToCamera()) {
				MathUtils.calcAngleFaceToCamera(beyondarObject.getPosition(), mCameraPosition,
						beyondarObject.getAngle());
			}

			if (!beyondarObject.getTexture().isLoaded() && beyondarObject.getImageUri() != null) {
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

			getScreenCoordinates(beyondarObject.getPosition(), beyondarObject.getScreenPositionCenter(),
					tmpEyeForRendering);

			try {
				for (GLPlugin plugin : plugins) {
					plugin.onDrawBeyondaarObject(gl, beyondarObject, defaultTexture);
				}
			} catch (ConcurrentModificationException e) {
				Logger.w("Some plugins where changed while drawing a frame");
			}

			renderable.draw(gl, defaultTexture);

			if (mFillPositions) {
				fillBeyondarObjectScreenPositions(beyondarObject);
			}
			mRenderedObjects.add(beyondarObject);
		} else {
			renderable.onNotRendered(dst);
		}
	}

	/**
	 * Use this method to fill all the screen positions of the
	 * {@link com.beyondar.android.world.BeyondarObject BeyondarObject}. After
	 * this method is called you can use the following:<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionBottomLeft()}<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionBottomRight()}<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionTopLeft()}<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionTopRight()}
	 * 
	 * @param beyondarObject
	 *            The {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject} to compute
	 */
	public void fillBeyondarObjectScreenPositions(BeyondarObject beyondarObject) {
		getScreenCoordinates(beyondarObject.getBottomLeft(), beyondarObject.getScreenPositionBottomLeft());
		getScreenCoordinates(beyondarObject.getBottomRight(), beyondarObject.getScreenPositionBottomRight());
		getScreenCoordinates(beyondarObject.getTopLeft(), beyondarObject.getScreenPositionTopLeft());
		getScreenCoordinates(beyondarObject.getTopRight(), beyondarObject.getScreenPositionTopRight());
	}

	/**
	 * Take an snapshot of the view. The callback will be notified when the
	 * picture is ready.
	 * 
	 * @param callBack
	 */
	public void tackePicture(GLSnapshotCallback callBack) {
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
		float screenRatio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// gl.glFrustumf(-ratio, ratio, -1, 1, 1f, 100);

		GLU.gluPerspective(gl, 45.0f, screenRatio, 0.1f, Z_FAR);
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
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				plugin.loadAdditionalTextures(gl);
			}
		}
	}

	/**
	 * Load the world textures.
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
						Bitmap defaultBtm = mWorld.getBitmapCache().getBitmap(list.getDefaultImageUri());
						Texture texture = load2DTexture(gl, defaultBtm);
						list.setDefaultTexture(texture);

						for (int j = 0; j < list.size(); j++) {
							// loading texture
							loadBeyondarObjectTexture(gl, list.get(j));

						}
					}
				}
			} catch (ConcurrentModificationException e) {
				loadWorldTextures(gl);
			}
			mWorld.getBitmapCache().cleanRecylcedBitmaps();
		}
		
	}

	/**
	 * Load the textures of the specified geoObject.
	 * 
	 * @param gl
	 * @param geoObject
	 *            The object to load the textures.
	 */
	public void loadBeyondarObjectTexture(GL10 gl, BeyondarObject geoObject) {

		Texture texture = getTexture(geoObject.getImageUri());

		if (texture == null) {
			Bitmap btm = mWorld.getBitmapCache().getBitmap(geoObject.getImageUri());

			texture = loadBitmapTexture(gl, btm, geoObject.getImageUri());

			if (texture == null || !texture.isLoaded()) {
				sPendingTextureObjects.addObject(geoObject.getImageUri(), geoObject);
			}
			if (btm == null) {
				if (Logger.DEBUG_OPENGL) {
					Logger.e(TAG, "ERROR: the resource " + geoObject.getImageUri()
							+ " has not been loaded. Object Name: " + geoObject.getName());
				}
			}
		}

		geoObject.setTexture(texture);
	}

	/**
	 * Load the {@link com.beyondar.android.opengl.texture.Texture Texture} from
	 * a Bitmap.
	 * 
	 * @param gl
	 * @param btm
	 *            The Bitmap to load.
	 * @param uri
	 *            The unique id of the bitmap.
	 * @return The {@link com.beyondar.android.opengl.texture.Texture Texture}
	 *         object.
	 */
	public Texture loadBitmapTexture(GL10 gl, Bitmap btm, String uri) {

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
	 * {@link com.beyondar.android.opengl.texture.Texture Texture} has been
	 * loaded with a pointer, use
	 * {@link com.beyondar.android.opengl.texture.Texture Texture.isLoaded()}
	 * 
	 * @param bitmap
	 * @return true if it is already loaded, false otherwise.
	 */
	public boolean isTextureLoaded(Bitmap bitmap) {
		return sTextureHolder.containsValue(bitmap);
	}

	/**
	 * Check if the image URI has been loaded as a texture. To check if the
	 * {@link com.beyondar.android.opengl.texture.Texture Texture} has any GL
	 * texture pointer use {@link com.beyondar.android.opengl.texture.Texture
	 * Texture.isLoaded()}
	 * 
	 * @param uri
	 * @return true if it is already loaded, false otherwise.
	 */
	public boolean isTextureObjectLoaded(String uri) {
		return sTextureHolder.get(uri) != null;
	}

	/**
	 * Get a copy of the texture pointer for the specified URI.
	 * 
	 * @param uri
	 * @return
	 */
	public Texture getTexture(String uri) {
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
	 * Create the texture for the specified Bitmap.<br>
	 * __Important__ The method will recycle the bitmap after being used.
	 * 
	 * @param gl
	 * @param bitmap
	 * @return A new texture for the bitmap.
	 */
	public Texture load2DTexture(GL10 gl, Bitmap bitmap) {
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
	public void onSensorChanged(float[] filteredValues, SensorEvent event) {
		if (!mRender) {
			return;
		}
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			mAccelerometerValues = filteredValues;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			mMagneticValues = filteredValues;
			break;
		default:
			break;
		}

	}

	// view port
	private final int[] viewport = new int[4];

	private void setupViewPort() {
		viewport[2] = mWidth;
		viewport[3] = mHeight;
	}

	/**
	 * Get a view {@link com.beyondar.android.util.math.geom.Ray Ray} for the
	 * screen position (x,y). Use this to object to check if there are any
	 * {@link com.beyondar.android.world.GeoObject GeoObject} that collide.
	 * 
	 * @param x
	 * @param y
	 * @param ray
	 */
	public void getViewRay(float x, float y, Ray ray) {

		// far eye point
		float[] eye = mFloat4ArrayPool.poll();
		if (eye == null) {
			eye = new float[4];
		} else {
			eye[0] = eye[1] = eye[2] = eye[3] = 0;
		}
		GLU.gluUnProject(x, mHeight - y, 0.9f, mMatrixGrabber.mModelView, 0, mMatrixGrabber.mProjection, 0,
				viewport, 0, eye, 0);

		// fix
		if (eye[3] != 0) {
			eye[0] = eye[0] / eye[3];
			eye[1] = eye[1] / eye[3];
			eye[2] = eye[2] / eye[3];
		}

		// ray vector
		ray.setVector((eye[0] - mCameraPosition.x), (eye[1] - mCameraPosition.y),
				(eye[2] - mCameraPosition.z));
		mFloat4ArrayPool.add(eye);
	}

	/**
	 * Get the screen position of a given
	 * {@link com.beyondar.android.util.math.geom.Point3 Point3}.
	 * 
	 * @param position
	 *            The 3D position to transform to a screen coordinates.
	 * @param outPoint
	 *            The object where the result will be stored.
	 */
	public void getScreenCoordinates(Point3 position, Point3 outPoint) {
		float[] eye = mFloat4ArrayPool.poll();
		if (eye == null) {
			eye = new float[4];
		}
		getScreenCoordinates(position.x, position.y, position.z, outPoint, mFloat4ArrayPool.poll());
		mFloat4ArrayPool.add(eye);
	}

	/**
	 * Get the screen position of a given
	 * {@link com.beyondar.android.util.math.geom.Point3 Point3}.
	 * 
	 * @param position
	 *            The 3D position to transform to a screen coordinates.
	 * @param outPoint
	 *            The object where the result will be stored.
	 * @param eye
	 *            Array where the eye position will be stored to do the
	 *            calculations.
	 */
	public void getScreenCoordinates(Point3 position, Point3 outPoint, float[] eye) {
		getScreenCoordinates(position.x, position.y, position.z, outPoint, eye);
	}

	/**
	 * Get the screen position of a given
	 * {@link com.beyondar.android.util.math.geom.Point3 Point3}.
	 * 
	 * @param x
	 *            The 3D position (x) to transform to a screen coordinates.
	 * @param y
	 *            The 3D position (y) to transform to a screen coordinates.
	 * @param z
	 *            The 3D position (z) to transform to a screen coordinates.
	 * @param outPoint
	 *            The object where the result will be stored.
	 * @param eye
	 *            Array where the eye position will be stored to do the
	 *            calculations.
	 */
	public void getScreenCoordinates(float x, float y, float z, Point3 outPoint, float[] eye) {
		// far eye point
		if (eye == null) {
			eye = new float[4];
		} else {
			eye[0] = eye[1] = eye[2] = eye[3] = 0;
		}

		GLU.gluProject(x, y, z, mMatrixGrabber.mModelView, 0, mMatrixGrabber.mProjection, 0, viewport, 0,
				eye, 0);

		// fix
		if (eye[3] != 0) {
			eye[0] = eye[0] / eye[3];
			eye[1] = eye[1] / eye[3];
			eye[2] = eye[2] / eye[3];
		}

		// Screen coordinates
		outPoint.x = eye[0];
		outPoint.y = mHeight - eye[1];
		outPoint.z = eye[2];
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

	private synchronized void setAllTextures(GL10 gl, String uri, Bitmap btm,
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

	/**
	 * Use this method to fill all the screen positions of the
	 * {@link com.beyondar.android.world.BeyondarObject BeyondarObject} when a
	 * object is rendered. Remember that the information is filled when the
	 * object is rendered, so it is asynchronous.<br>
	 * 
	 * After this method is called you can use the following:<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionBottomLeft()}<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionBottomRight()}<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionTopLeft()}<br>
	 * {@link com.beyondar.android.world.BeyondarObject
	 * BeyondarObject.getScreenPositionTopRight()}
	 * 
	 * __Important__ Enabling this feature will reduce the FPS, use only when is
	 * needed.
	 * 
	 * @param fill
	 *            Enable or disable this feature.
	 */
	public void forceFillBeyondarObjectPositions(boolean fill) {
		mFillPositions = fill;
	}

	public static interface FpsUpdatable {

		/**
		 * This method will get the frames per second rendered by the
		 * {@link ARRenderer}
		 * 
		 * @param fps
		 *            The Frames per second rendered
		 */
		public void onFpsUpdate(float fps);
	}

	@Override
	public void addPlugin(GLPlugin plugin) {
		synchronized (lockPlugins) {
			if (!plugins.contains(plugin)) {
				plugins.add(plugin);
			}
		}
		plugin.setup(mWorld, this);
	}

	@Override
	public boolean removePlugin(GLPlugin plugin) {
		boolean removed = false;
		synchronized (lockPlugins) {
			removed = plugins.remove(plugin);
		}
		if (removed) {
			plugin.onDetached();
		}
		return removed;
	}

	@Override
	public void removeAllPlugins() {
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				removePlugin(plugin);
			}
		}
	}

	@Override
	public GLPlugin getFirstPlugin(Class<? extends GLPlugin> pluginClass) {
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					return plugin;
				}
			}
		}
		return null;
	}

	@Override
	public boolean containsAnyPlugin(Class<? extends GLPlugin> pluginClass) {
		return getFirstPlugin(pluginClass) != null;
	}

	@Override
	public boolean containsPlugin(GLPlugin plugin) {
		synchronized (lockPlugins) {
			return plugins.contains(plugin);
		}
	}

	@Override
	public List<GLPlugin> getAllPlugins(Class<? extends GLPlugin> pluginClass, List<GLPlugin> result) {
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					result.add(plugin);
				}
			}
		}
		return result;
	}

	@Override
	public List<GLPlugin> getAllPugins(Class<? extends GLPlugin> pluginClass) {
		ArrayList<GLPlugin> result = new ArrayList<GLPlugin>(5);
		return getAllPlugins(pluginClass, result);
	}

	@Override
	public List<GLPlugin> getAllPlugins() {
		synchronized (lockPlugins) {
			return new ArrayList<GLPlugin>(plugins);
		}
	}

	/**
	 * Called when the application is paused.
	 */
	public void onPause() {
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				plugin.onPause();
			}
		}
	}

	/**
	 * Called when the application is resumed.
	 */
	public void onResume() {
		synchronized (lockPlugins) {
			for (GLPlugin plugin : plugins) {
				plugin.onResume();
			}
		}
	}
	
	/**
	 * Set the distance (in meters) which the app will draw the objects.
	 * 
	 * @param meters
	 */
	public void setMaxDistanceToRender(float meters) {
		mArViewDistance = meters;
	}

	/**
	 * Get the distance (in meters) which the AR view will draw the objects.
	 * 
	 * @return meters
	 */
	public float getMaxDistanceToRender() {
		return mArViewDistance;
	}

	/**
	 * Set the distance factor for rendering all the objects. As bigger the
	 * factor the closer the objects.
	 * 
	 * @param factor
	 *            number bigger than 0.
	 */
	public void setDistanceFactor(float factor) {
		if (factor <= 0)
			return;
		mDistanceFactor = factor;
	}

	/**
	 * Get the distance factor.
	 * 
	 * @return Distance factor
	 */
	public float getDistanceFactor() {
		return mDistanceFactor;
	}
}