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
import com.beyondar.android.opengl.renderer.ARRenderer.GLSnapshotCallback;
import com.beyondar.android.opengl.renderer.OnBeyondarObjectRenderedListener;
import com.beyondar.android.opengl.util.MatrixTrackingGL;
import com.beyondar.android.sensor.BeyondarSensorManager;
import com.beyondar.android.util.Logger;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;

/**
 * GL View to draw the {@link com.beyondar.android.world.World World} using the
 * {@link com.beyondar.android.opengl.renderer.ARRenderer ARRenderer}
 */
public class BeyondarGLSurfaceView extends GLSurfaceView implements OnBeyondarObjectRenderedListener {

	protected ARRenderer mRenderer;

	private BeyondarViewAdapter mViewAdapter;
	private ViewGroup mParent;

	private World mWorld;
	private int mSensorDelay;

	public BeyondarGLSurfaceView(Context context) {
		super(context);
		init(context);

	}

	public BeyondarGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
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
	 * Take an snapshot of the view. The callback will be notified when the
	 * picture is ready.
	 * 
	 * @param callBack
	 *            {@link com.beyondar.android.opengl.renderer.GLSnapshotCallback
	 *            GLSnapshotCallback}
	 */
	public void tackePicture(GLSnapshotCallback callBack) {
		mRenderer.tackePicture(callBack);
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
		BeyondarSensorManager.unregisterSensorListener(mRenderer);
	}

	private void registerSensorListener(int sensorDealy) {
		BeyondarSensorManager.registerSensorListener(mRenderer);

	}

	@Override
	public void onPause() {
		unregisterSensorListener();
		super.onPause();
		mRenderer.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerSensorListener(mSensorDelay);
		if (mRenderer != null) {
			Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			mRenderer.rotateView(display.getRotation());
			mRenderer.onResume();
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (mWorld == null || event == null) {
			return false;
		}

		return false;
	}

	private static final Ray sRay = new Ray(0, 0, 0);

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen
	 * 
	 * @param x
	 * @param y
	 * @param beyondarObjects
	 *            The output list to place all the
	 *            {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject} that collide with the screen cord
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
	 *            The output list to place all the
	 *            {@link com.beyondar.android.world.BeyondarObject
	 *            BeyondarObject} that collide with the screen cord
	 * @param ray
	 *            The ray that will hold the direction of the screen coordinate
	 * @return
	 */
	public synchronized void getBeyondarObjectsOnScreenCoordinates(float x, float y,
			ArrayList<BeyondarObject> beyondarObjects, Ray ray) {
		mRenderer.getViewRay(x, y, ray);
		mWorld.getBeyondarObjectsCollideRay(ray, beyondarObjects, getArViewDistance());
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
	public void setMaxDistanceSize(float maxDistanceSize) {
		mRenderer.setMaxDistanceSize(maxDistanceSize);
	}

	/**
	 * Get the max distance which a {@link com.beyondar.android.world.GeoObject
	 * GeoObject} will be rendered.
	 * 
	 * @return The current max distance. 0 is the default behavior
	 */
	public float getMaxDistanceSize() {
		return mRenderer.getMaxDistanceSize();
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
	public void setMinDistanceSize(float minDistanceSize) {
		mRenderer.setMinDistanceSize(minDistanceSize);
	}

	/**
	 * Get the minimum distance which a
	 * {@link com.beyondar.android.world.GeoObject GeoObject} will be rendered.
	 * 
	 * @return The current minimum distance. 0 is the default behavior
	 */
	public float getMinDistanceSize() {
		return mRenderer.getMinDistanceSize();
	}
	
	/**
	 * Set the distance (in meters) which the app will draw the objects.
	 * 
	 * @param meters
	 */
	public void setArViewDistance(float meters) {
		mRenderer.setArViewDistance(meters);
	}

	/**
	 * Get the distance (in meters) which the AR view will draw the objects.
	 * 
	 * @return meters
	 */
	public float getArViewDistance() {
		return mRenderer.getArViewDistance();
	}

	public void setBeyondarViewAdapter(BeyondarViewAdapter beyondarViewAdapter, ViewGroup parent) {
		mViewAdapter = beyondarViewAdapter;
		mParent = parent;
	}

	@Override
	public void onBeyondarObjectsRendered(List<BeyondarObject> renderedBeyondarObjects) {
		BeyondarViewAdapter tmpView = mViewAdapter;
		if (tmpView != null) {
			List<BeyondarObject> elements = World
					.sortGeoObjectByDistanceFromCenter(new ArrayList<BeyondarObject>(renderedBeyondarObjects));
			tmpView.processList(elements, mParent, this);
		}
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
	public void forceFillBeyondarObjectPositionsOnRendering(boolean fill) {
		mRenderer.forceFillBeyondarObjectPositions(fill);
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
	public void fillBeyondarObjectPositions(BeyondarObject beyondarObject) {
		mRenderer.fillBeyondarObjectScreenPositions(beyondarObject);
	}
}
