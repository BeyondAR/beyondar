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

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.beyondar.android.opengl.renderer.ARRenderer;
import com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable;
import com.beyondar.android.opengl.renderer.ARRenderer.SnapshotCallback;
import com.beyondar.android.opengl.renderer.OnBeyondarObjectRenderedListener;
import com.beyondar.android.opengl.util.BeyondarSensorManager;
import com.beyondar.android.opengl.util.MatrixTrackingGL;
import com.beyondar.android.util.CompatibilityUtil;
import com.beyondar.android.util.Logger;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

public class BeyondarGLSurfaceView extends GLSurfaceView implements OnBeyondarObjectRenderedListener {

	protected ARRenderer mRenderer;
	private SensorManager mSensorManager;
	private Context mContext;

	private BeyondarViewAdapter mViewAdapter;
	private ViewGroup mParent;

	@Deprecated
	private OnTouchBeyondarViewListener mTouchListener;

	private World mWorld;

	private int mSensorDelay;

	public void tackePicture(SnapshotCallback callBack) {
		mRenderer.tackePicture(callBack);
	}

	public BeyondarGLSurfaceView(Context context) {
		super(context);
		init(context);

	}

	public BeyondarGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);

	}

	private void init(Context context) {
		mContext = context;
		mSensorDelay = SensorManager.SENSOR_DELAY_UI;

		if (Logger.DEBUG_OPENGL) {
			setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		}

		// Wrapper set so the renderer can
		// access the gl transformation matrixes.
		setGLWrapper(new GLSurfaceView.GLWrapper() {
			@Override
			public GL wrap(GL gl) {
				return new MatrixTrackingGL(gl);
			}
		});

		mRenderer = createRenderer();
		mRenderer.setOnBeyondarObjectRenderedListener(this);
		configureRenderer(mRenderer);

		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setRenderer(mRenderer);

		requestFocus();
		// This call will allow the GLSurface to be on the top of all the
		// Surfaces. It is needed because when the camera is rotated the camera
		// tend to overlap the GLSurface.
		setZOrderMediaOverlay(true);
		setFocusableInTouchMode(true);
	}

	/**
	 * Override this method to change the renderer. For instance:<br>
	 * <code>return new CustomARRenderer();</code><br>
	 * 
	 */
	protected ARRenderer createRenderer() {
		return new ARRenderer();
	}

	/**
	 * Override this method to personalize the configuration of the ARRenderer
	 * 
	 * @param renderer
	 */
	protected void configureRenderer(ARRenderer renderer) {
		renderer.rotateViewForTablet(CompatibilityUtil.isTablet(mContext)
				&& !CompatibilityUtil.is7InchTablet(getContext()));
	}

	public void setFpsUpdatable(FpsUpdatable fpsUpdatable) {
		mRenderer.setFpsUpdatable(fpsUpdatable);
	}

	@Override
	public void setVisibility(int visibility) {
		if (visibility == VISIBLE) {
			mRenderer.setRendering(true);
		} else {
			mRenderer.setRendering(false);
		}
		super.setVisibility(visibility);
	}

	/**
	 * Specify the delay to apply to the accelerometer and the magnetic field
	 * sensor. If you don't know what is the best value, don't touch it. The
	 * following values are applicable:<br>
	 * <br>
	 * SensorManager.SENSOR_DELAY_UI<br>
	 * SensorManager.SENSOR_DELAY_NORMAL <br>
	 * SensorManager.SENSOR_DELAY_GAME <br>
	 * SensorManager.SENSOR_DELAY_GAME <br>
	 * SensorManager.SENSOR_DELAY_FASTEST <br>
	 * <br>
	 * You can find more information in the
	 * {@link android.hardware.SensorManager} class
	 * 
	 * 
	 * @param delay
	 */
	public void setSensorDelay(int delay) {
		mSensorDelay = delay;
		unregisterSensorListener();
		registerSensorListener(mSensorDelay);
	}

	/**
	 * Get the current sensor delay. See {@link android.hardware.SensorManager}
	 * for more information
	 * 
	 * @return sensor delay
	 */
	public int getSensorDelay() {
		return mSensorDelay;
	}

	/**
	 * Define the world where the objects are stored.
	 * 
	 * @param world
	 */
	public void setWorld(World world) {
		if (null == mWorld) {// first time
			unregisterSensorListener();
			registerSensorListener(mSensorDelay);
		}
		mWorld = world;
		mRenderer.setWorld(world);
	}

	private void unregisterSensorListener() {
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

		BeyondarSensorManager.initializeSensors(mSensorManager);
		BeyondarSensorManager.unregisterSensorListener(mRenderer);
	}

	private void registerSensorListener(int sensorDealy) {
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

		BeyondarSensorManager.initializeSensors(mSensorManager);
		BeyondarSensorManager.registerSensorListener(mRenderer);

	}

	@Override
	public void onPause() {
		unregisterSensorListener();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerSensorListener(mSensorDelay);
		if (mRenderer != null) {
			Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			mRenderer.rotateView(display.getRotation());
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (mWorld == null || mTouchListener == null || event == null) {
			return false;
		}

		mTouchListener.onTouchBeyondarView(event, this);
		return false;
	}

	@Deprecated
	public void setOnTouchBeyondarViewListener(OnTouchBeyondarViewListener listener) {
		mTouchListener = listener;
	}

	private static final Ray sRay = new Ray(0, 0, 0);

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen
	 * 
	 * @param x
	 * @param y
	 * @param beyondarObjects
	 *            The output list to place all the {@link BeyondarObject} that
	 *            collide with the screen cord
	 * @return
	 */
	public synchronized void getBeyondarObjectsOnScreenCoordinates(float x, float y,
			ArrayList<BeyondarObject> beyondarObjects) {
		getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects, sRay);

	}

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen
	 * 
	 * @param x
	 * @param y
	 * @param beyondarObjects
	 *            The output list to place all the {@link BeyondarObject} that
	 *            collide with the screen cord
	 * @param ray
	 *            The ray that will hold the direction of the screen coordinate
	 * @return
	 */
	public synchronized void getBeyondarObjectsOnScreenCoordinates(float x, float y,
			ArrayList<BeyondarObject> beyondarObjects, Ray ray) {
		mRenderer.getViewRay(x, y, ray);
		mWorld.getBeyondarObjectsCollideRay(ray, beyondarObjects);
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
		mRenderer.setMaxDistanceSize(maxDistanceSize);
	}

	/**
	 * Get the max distance which a {@link GeoObject} will be rendered.
	 * 
	 * @return The current max distance. 0 is the default behavior
	 */
	public float getMaxDistanceSize() {
		return mRenderer.getMaxDistanceSize();
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
		mRenderer.setMinDistanceSize(minDistanceSize);
	}

	/**
	 * Get the minimum distance which a {@link GeoObject} will be rendered.
	 * 
	 * @return The current minimum distance. 0 is the default behavior
	 */
	public float getMinDistanceSize() {
		return mRenderer.getMinDistanceSize();
	}

	public void setBeyondarViewAdapter(BeyondarViewAdapter beyondarViewAdapter, ViewGroup parent) {
		mViewAdapter = beyondarViewAdapter;
		mParent = parent;
	}

	@Override
	public void onBeyondarObjectsRendered(List<BeyondarObject> renderedBeyondarObjects) {
		BeyondarViewAdapter tmpView = mViewAdapter;
		if (tmpView != null) {
			tmpView.processList(new ArrayList<BeyondarObject>(renderedBeyondarObjects), mParent, this);
		}
	}

	public void forceFillBeyondarObjectPositions(boolean fill) {
		mRenderer.forceFillBeyondarObjectPositions(true);
	}

	public void fillBeyondarObjectPositions(BeyondarObject beyondarObject) {
		mRenderer.fillBeyondarObjectPositions(beyondarObject);
	}

}
