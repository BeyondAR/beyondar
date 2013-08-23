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
package com.beyondar.android.opengl.views;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

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
import android.util.Log;

import com.beyondar.android.opengl.renderable.IRenderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.opengl.util.MatrixGrabber;
import com.beyondar.android.util.Constants;
import com.beyondar.android.util.Utils;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.cache.BitmapCache.IOnExternalBitmapLoadedCahceListener;
import com.beyondar.android.util.math.Distance;
import com.beyondar.android.util.math.MathUtils;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.World;
import com.beyondar.android.world.objects.GeoObject;
import com.beyondar.android.world.objects.BeyondarObject;

// Some references:
// http://ovcharov.me/2011/01/14/android-opengl-es-ray-picking/
// http://magicscrollsofcode.blogspot.com/2010/10/3d-picking-in-android.html
public class ARRenderer implements GLSurfaceView.Renderer, SensorEventListener,
		IOnExternalBitmapLoadedCahceListener {

	/**
	 * 
	 * @author Joan Puig Sanz (joanpuigsanz@gmail.com)
	 * 
	 */
	public static interface ISnapshotCallback {
		/**
		 * This method is called when the snapshot of the GL Surface is ready.
		 * If there is an error, the image will be null
		 * 
		 * @param snapshot
		 */
		void onSnapshootTaken(Bitmap snapshot);
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

	private static final boolean DEBUG = false;

	private float mAccelerometerValues[] = new float[3];
	private float mMagneticValues[] = new float[3];
	// private float mOrientationValues[] = new float[3];

	private float rotationMatrix[] = new float[16];
	private float remappedRotationMatrix[] = new float[16];

	private MatrixGrabber mg = new MatrixGrabber();

	public Point3 mPostionCamera;

	private int mWidth, mHeight;

	private static HashMap<String, Texture> mTextureHolder;
	private static PendingTextureObjects mPendingTextureObjects;

	private static ArrayList<UriAndBitmap> sNewBitmapsLoaded;

	private World mWorld;
	protected boolean reloadWorldTextures;

	private boolean mScreenshot;
	private ISnapshotCallback mSnapshotCallback;
	private SensorEventListener mExternalSensorListener;

	private boolean mIsTablet;

	/**
	 * The OpenGL camera position
	 */
	private Point3 mMyPos;

	private boolean mRender;

	// TESTING
	// private Square squareTest = new Square();
	float mAngle = 0;

	private boolean mGetFps = false;

	public ARRenderer() {
		reloadWorldTextures = false;
		setRendering(true);
		mPostionCamera = new Point3(0, 0, 0);

		mMyPos = new Point3(0, 0, 0);
		mTextureHolder = new HashMap<String, Texture>();
		mPendingTextureObjects = new PendingTextureObjects();
		sNewBitmapsLoaded = new ArrayList<UriAndBitmap>();

		mIsTablet = false;
	}

	/**
	 * Use this method to specify the renderer that is running on a tablet. If
	 * so the renderer will rotate the view to be able to be displayed on
	 * tablets
	 * 
	 * @param isTablet
	 */
	public void rotateView(boolean isTablet) {
		mIsTablet = isTablet;
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
		mMyPos = newCameraPos;
	}

	/**
	 * Restore the camera position
	 */
	public void restoreCameraPosition() {
		mMyPos.x = 0;
		mMyPos.y = 0;
		mMyPos.z = 0;
	}

	/**
	 * Get the camera position
	 * 
	 * @return
	 */
	public Point3 getCameraPosition() {
		return mMyPos;
	}

	private static final float[] i = new float[16];

	public void onDrawFrame(GL10 gl) {
		if (!mRender) {
			return;
		}

		long time = System.currentTimeMillis();

		SensorManager.getInclination(i);
		// Get rotation matrix from the sensor
		// TODO: Check if getRotationMatrix do the rotation
		SensorManager.getRotationMatrix(rotationMatrix, i, mAccelerometerValues, mMagneticValues);

		if (mIsTablet) {
			android.hardware.SensorManager.remapCoordinateSystem(rotationMatrix,
					android.hardware.SensorManager.AXIS_MINUS_Y, android.hardware.SensorManager.AXIS_X,
					rotationMatrix);
		}

		// // As the documentation says, we are using the device as a compass in
		// // landscape mode
		SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
				remappedRotationMatrix);

		// Clear color buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Load remapped matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLoadMatrixf(remappedRotationMatrix, 0);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		// gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

		// gl.glEnable(GL10.GL_BLEND); //Turn Blending On
		// gl.glDisable(GL10.GL_DEPTH_TEST); //Turn Depth Testing Off

		// getCurrentProjection(gl);
		// getCurrentModelView(gl);

		if (sNewBitmapsLoaded.size() > 0) {
			for (int i = 0; i < sNewBitmapsLoaded.size(); i++) {
				mPendingTextureObjects.setAllTextures(gl, sNewBitmapsLoaded.get(i).uri,
						sNewBitmapsLoaded.get(i).btm);
			}
			sNewBitmapsLoaded.clear();
		}

		// Store projection and modelview matrices
		mg.getCurrentState(gl);

		if (mWorld != null) {
			if (reloadWorldTextures) {
				loadWorldTextures(gl);
				reloadWorldTextures = false;
			}
			drawWorld(gl, time);
		}

		if (mScreenshot) {
			mScreenshot = false;
			if (mSnapshotCallback != null) {
				mSnapshotCallback.onSnapshootTaken(savePixels(gl));
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
		Bitmap result = Bitmap
				.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		bitmap.recycle();
		System.gc();
		return result;
	}

	protected void convertGPStoPoint3(GeoObject geoObject, Point3 out) {
		float x = (float) ((geoObject.getLongitude() - mWorld.getLongitude()) * Distance.METERS_TO_GEOPOINT / 2);
		float z = (float) ((geoObject.getAltitude() - mWorld.getAltitude()) * Distance.METERS_TO_GEOPOINT / 2);
		float y = (float) ((geoObject.getLatitude() - mWorld.getLatitude()) * Distance.METERS_TO_GEOPOINT / 2);
		// out = new Point3(x, y, z);

		Log.d("bar", "geoObject: " + geoObject.getName() + " Longitude=" + geoObject.getLongitude()
				+ " Latitude=" + geoObject.getLatitude());
		out.x = x;
		out.y = y;
		out.z = z;
	}

	private long mCurrentTime = System.currentTimeMillis();
	private float mFrames = 0;
	private IFpsUpdatable mFpsUpdatable;

	public void setFpsUpdatable(IFpsUpdatable fpsUpdatable) {
		mCurrentTime = System.currentTimeMillis();
		mFpsUpdatable = fpsUpdatable;
		mGetFps = mFpsUpdatable != null;
	}

	protected void drawWorld(GL10 gl, long time) {
		BeyondarObjectList list = null;
		try {
			for (int i = 0; i < mWorld.getBeyondarObjectLists().size(); i++) {
				list = mWorld.getBeyondarObjectLists().get(i);
				if (list != null) {
					drawList(gl, list, time);
				}

			}
		} catch (ConcurrentModificationException e) {
		}
		mWorld.forceProcessRemoveQueue();

		if (mGetFps) {
			mFrames++;
			long timeInterval = System.currentTimeMillis() - mCurrentTime;
			if (timeInterval > 1000) {
				if (DEBUG) {
					Log.d(Constants.TAG, "Frames/second:  " + mFrames / (timeInterval / 1000F));
				}
				if (mFpsUpdatable != null) {
					mFpsUpdatable.onFpsUpdate(mFrames / (timeInterval / 1000F));
				}
				mFrames = 0;
				mCurrentTime = System.currentTimeMillis();
			}
		}
	}

	protected void drawList(GL10 gl, BeyondarObjectList list, long time) {

		Texture listTexture = list.getTexture();

		// Check if the list's default bitmap has been loaded.
		if (!listTexture.isLoaded()) {
			Texture defaultTexture = mTextureHolder.get(list.getDefaultBitmapURI());
			if (defaultTexture == null || !defaultTexture.isLoaded()) {
				if (DEBUG) {
					Log.w(Constants.TAG, "Warning!! The default texture for the list \"" + list.getType()
							+ "\" has not been loaded. Trying to load it now...");
				}
				Bitmap defaultBtm = mWorld.getBitmapCache().getBitmap(list.getDefaultBitmapURI());
				defaultTexture = load2DTexture(gl, defaultBtm);
			}
			list.setTexture(defaultTexture == null ? null : defaultTexture.clone());
		}

		Log.d("bar", "world: Longitude=" + mWorld.getLongitude() + " Latitude=" + mWorld.getLatitude());
		for (int j = 0; j < list.size(); j++) {
			BeyondarObject beyondarObject = list.get(j);
			if (beyondarObject == null) {
				continue;
			}
			renderGeoObject(gl, beyondarObject, listTexture, time);
		}
	}

	/**
	 * Override this method to customize the way to render the objects
	 * 
	 * @param gl
	 * @param beyondarObject
	 * @param defaultTexture
	 */
	protected void renderGeoObject(GL10 gl, BeyondarObject beyondarObject, Texture defaultTexture, long time) {
		boolean renderObject = false;
		IRenderable renderable = beyondarObject.getOpenGLObject();

		if (renderable == null || !beyondarObject.isVisible()) {
			return;
		}
		double dst = 0;
		if (beyondarObject instanceof GeoObject) {
			dst = ((GeoObject) beyondarObject).calculateDistanceMeters(mWorld.getLongitude(),
					mWorld.getLatitude());
			convertGPStoPoint3((GeoObject) beyondarObject, beyondarObject.getPosition());
		} else {// TODO: Set the 0,0,0 to the camera position
			Point3 position = beyondarObject.getPosition();
			dst = MathUtils.GLUnitsToMeters((float) Distance.calculateDistanceCoordinates(0, 0, 0,
					position.x, position.y, position.z));
		}

		if (dst < mWorld.getViewDistance()) {
			renderObject = true;
		}

		boolean forceDraw = renderable.update(time, (float) dst, beyondarObject);

		if (forceDraw || renderObject) {
			if (beyondarObject.isFacingToCamera()) {
				MathUtils.calcAngleFaceToCamera(beyondarObject.getPosition(), mMyPos,
						beyondarObject.getAngle());
			}

			if (!beyondarObject.getTexture().isLoaded() && beyondarObject.getBitmapUri() != null) {
				if (beyondarObject.getTexture().getTimeStamp() == 0
						|| time - beyondarObject.getTexture().getTimeStamp() > TIMEOUT_LOAD_TEXTURE) {
					// TODO: Implement incremental time for the timeout?
					if (DEBUG) {
						Log.d(Constants.TAG, "Loading new texture...");
					}
					loadBeyondarObjectTexture(gl, beyondarObject);
					if (!beyondarObject.getTexture().isLoaded()) {
						beyondarObject.getTexture().setTimeStamp(time);
					}
				}

			}
			renderable.draw(gl, defaultTexture);
		} else {
			renderable.onNotRendered(dst);
		}
	}

	public void tackePicture(ISnapshotCallback callBack) {
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
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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

		// TODO What is the best choice?
		// Really Nice Perspective Calculations
		// gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		// gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		// GL10.GL_REPLACE);

		gl.glClearColor(0, 0, 0, 0);

		mTextureHolder.clear();
		if (DEBUG) {
			Log.d(Constants.TAG, "Loading textures...");
		}
		loadWorldTextures(gl);
		loadAditionalTextures(gl);
		if (DEBUG) {
			Log.d(Constants.TAG, "TEXTURES LOADED");
		}

	}

	/**
	 * Override this method to load additional textures. Use the following code
	 * to help you:<br>
	 * <code>loadTexture(gl, yourBitmap)</code>
	 * 
	 * @param gl
	 */
	protected void loadAditionalTextures(GL10 gl) {
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
						Bitmap defaultBtm = mWorld.getBitmapCache().getBitmap(list.getDefaultBitmapURI());
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
		// Optimize this part. The bitmaps should be cleaned only for the images
		// loaded
		mWorld.getBitmapCache().cleanRecylcedBitmaps();
	}

	/**
	 * Load the textures of the specified geoObject
	 * 
	 * @param gl
	 * @param geoObject
	 *            The object to get the textures
	 */
	protected void loadBeyondarObjectTexture(GL10 gl, BeyondarObject geoObject) {

		Texture texture = getTexture(geoObject.getBitmapUri());

		if (texture == null) {
			Bitmap btm = mWorld.getBitmapCache().getBitmap(geoObject.getBitmapUri());

			texture = loadBitmapTexture(gl, btm, geoObject.getBitmapUri());

			if (texture == null || !texture.isLoaded()) {
				mPendingTextureObjects.addObject(geoObject.getBitmapUri(), geoObject);
			}
			if (btm == null) {
				if (DEBUG) {
					Log.e(Constants.TAG, "ERROR: the resource " + geoObject.getBitmapUri()
							+ " has not been loaded. Object Name: " + geoObject.getName());
				}
			}
		}

		geoObject.setTexture(texture);
	}

	protected Texture loadBitmapTexture(GL10 gl, Bitmap btm, String uri) {

		if (null == btm) {
			return null;
		}
		Texture texture = null;
		// Check if the texture already exist
		texture = mTextureHolder.get(uri);
		if (texture == null) {
			texture = load2DTexture(gl, btm);

			mTextureHolder.put(uri, texture);
		}
		return texture.clone();
	}

	/**
	 * Check if the ExBitmap texture object is loaded. To check if the
	 * {@link Texture} has been loaded with a pointer, use
	 * {@link Texture#isLoaded()}
	 * 
	 * @param exBitmap
	 * @return true if it is already loaded, false otherwise.
	 */
	protected boolean isTextureLoaded(Bitmap exBitmap) {
		return mTextureHolder.containsValue(exBitmap);
	}

	/**
	 * Check if the image URI has been loaded as a texture. To check if the
	 * {@link Texture} has been loaded with a pointer, use
	 * {@link Texture#isLoaded()}
	 * 
	 * @param exBitmap
	 * @return true if it is already loaded, false otherwise.
	 */
	protected boolean isTextureObjectLoaded(String uri) {
		return mTextureHolder.get(uri) != null;
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
		Texture texture = mTextureHolder.get(uri);
		if (texture != null) {
			texture = texture.clone();
		}
		return texture;
	}

	/**
	 * Create the texture for the specified {@link Bitmap}. The method will
	 * recycle the bitmap after being used
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

		if (!Utils.isCompatibleWithOpenGL(bitmap)) {
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
		// Log.d(Constants.TAG, "Texture pointer= " + tmpTexture[0]);
		return new Texture(tmpTexture[0]);

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

	public void getViewRay(float x, float y, Ray ray) {
		// view port
		int[] viewport = { 0, 0, mWidth, mHeight };

		// far eye point
		float[] eye = new float[4];
		GLU.gluUnProject(x, mHeight - y, 0.9f, mg.mModelView, 0, mg.mProjection, 0, viewport, 0, eye, 0);

		// fix
		if (eye[3] != 0) {
			eye[0] = eye[0] / eye[3];
			eye[1] = eye[1] / eye[3];
			eye[2] = eye[2] / eye[3];
		}

		// ray vector
		ray.setVector((eye[0] - mPostionCamera.x), (eye[1] - mPostionCamera.y), (eye[2] - mPostionCamera.z));
	}

	public void setRendering(boolean render) {
		mRender = render;

	}

	public boolean isRendereing() {
		return mRender;
	}

	@Override
	public void onExternalBitmapLoaded(BitmapCache cache, String url, Bitmap btm) {
		UriAndBitmap uriAndBitmap = new UriAndBitmap();
		uriAndBitmap.uri = url;
		uriAndBitmap.btm = btm;
		sNewBitmapsLoaded.add(uriAndBitmap);
	}

	private class PendingTextureObjects {

		private HashMap<String, ArrayList<BeyondarObject>> mHolder;

		public PendingTextureObjects() {
			mHolder = new HashMap<String, ArrayList<BeyondarObject>>();
		}

		public synchronized void addObject(String url, BeyondarObject object) {
			if (url == null || object == null) {
				return;
			}

			ArrayList<BeyondarObject> list = mHolder.get(url);
			if (list == null) {
				list = new ArrayList<BeyondarObject>();
				mHolder.put(url, list);
			}
			list.add(object);
		}

		public synchronized void setAllTextures(GL10 gl, String uri, Bitmap btm) {
			if (uri == null) {
				return;
			}
			ArrayList<BeyondarObject> list = mHolder.get(uri);
			if (list == null) {
				return;
			}

			Texture texture = loadBitmapTexture(gl, btm, uri);

			for (int i = 0; i < list.size() && texture.isLoaded(); i++) {
				BeyondarObject object = list.get(i);

				object.setTexture(texture);
				list.remove(i);
				i--;
			}
		}
	}

	public static interface IFpsUpdatable {

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