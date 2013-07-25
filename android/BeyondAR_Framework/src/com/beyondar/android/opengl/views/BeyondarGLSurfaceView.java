package com.beyondar.android.opengl.views;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.beyondar.android.opengl.util.BeyondarSensorManager;
import com.beyondar.android.opengl.util.MatrixTrackingGL;
import com.beyondar.android.opengl.views.ARRenderer.IFpsUpdatable;
import com.beyondar.android.opengl.views.ARRenderer.ISnapshotCallback;
import com.beyondar.android.util.Constants;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.world.World;
import com.beyondar.android.world.objects.BeyondarObject;

public class BeyondarGLSurfaceView extends GLSurfaceView {

	protected ARRenderer mRenderer;
	private SensorManager mSensorManager;
	private Context mContext;

	// Listeners
	private IOnARTouchListener mTouchListener;
	// End listeners

	private World mWorld;

	private int mSensorDelay;

	public void tackePicture(ISnapshotCallback callBack) {
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

		if (Constants.DEBUG_OPENGL) {
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

		createRenderer();

		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		setRenderer(mRenderer);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);

		this.requestFocus();
		this.setFocusableInTouchMode(true);
	}

	/**
	 * Override this method to change the renderer. For instance:<br>
	 * <code>mRenderer = new CustomARRenderer();</code>
	 * 
	 */
	protected void createRenderer() {
		mRenderer = new ARRenderer();
	}

	public void setFpsUpdatable(IFpsUpdatable fpsUpdatable) {
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
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mWorld == null || mTouchListener == null || event == null) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			processScreenActionUp(event);
			return true;
		case MotionEvent.ACTION_DOWN:
			processScreenAtionDown(event);
			return true;
		case MotionEvent.ACTION_MOVE:
			processScreenActionMove(event);
			return true;
		}

		return false;

	}

	public void setonARTouchListener(IOnARTouchListener listener) {
		mTouchListener = listener;
	}

	public synchronized void processScreenAtionDown(MotionEvent event) {
		mTouchListener.onTouchARView(event, this);
	}

	public synchronized void processScreenActionMove(MotionEvent event) {
		mTouchListener.onTouchARView(event, this);
	}

	public synchronized void processScreenActionUp(MotionEvent event) {
		mTouchListener.onTouchARView(event, this);
	}

	private static final Ray sRay = new Ray(0, 0, 0);

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen
	 * 
	 * @param x
	 * @param y
	 * @param beyondarObjects
	 *            The list to place the objects that has been collide
	 * @return
	 */
	public synchronized void getARObjectOnScreenCoordinates(float x, float y,
			ArrayList<BeyondarObject> beyondarObjects) {
		getARObjectOnScreenCoordinates(x, y, beyondarObjects, sRay);

	}

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen
	 * 
	 * @param x
	 * @param y
	 * @param beyondarObjects
	 *            The list to place the objects that has been collide
	 * @param ray
	 *            The ray that will hold the direction of the screen coordinate
	 * @return
	 */
	public synchronized void getARObjectOnScreenCoordinates(float x, float y,
			ArrayList<BeyondarObject> beyondarObjects, Ray ray) {
		mRenderer.getViewRay(x, y, ray);
		mWorld.getBeyondarObjectsCollideRay(ray, beyondarObjects);

	}
}
