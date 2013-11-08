package com.beyondar.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable;
import com.beyondar.android.util.math.geom.Ray;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.CameraView;
import com.beyondar.android.view.OnClikBeyondarObjectListener;
import com.beyondar.android.view.OnTouchBeyondarViewListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

public class BeyondarFragmentSupport extends Fragment implements FpsUpdatable, OnClickListener,
		OnTouchListener {

	private CameraView mBeyondarCameraView;
	private BeyondarGLSurfaceView mBeyondarGLSurface;
	private TextView mFpsTextView;
	private FrameLayout mMailLayout;

	private World mWorld;

	private OnTouchBeyondarViewListener mTouchListener;
	private OnClikBeyondarObjectListener mClickListener;

	private float mLastScreenTouchX, mLastScreenTouchY;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private void init() {
		android.view.ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		mMailLayout = new FrameLayout(getActivity());
		mBeyondarGLSurface = getBeyondarGLSurfaceView();
		mBeyondarGLSurface.setOnTouchListener(this);

		mBeyondarCameraView = createCameraView();

		mMailLayout.addView(mBeyondarCameraView, params);
		mMailLayout.addView(mBeyondarGLSurface, params);
	}

	/**
	 * Override this method to personalize the {@link BeyondarGLSurfaceView}
	 * that will be instantiated
	 * 
	 * @return
	 */
	protected BeyondarGLSurfaceView getBeyondarGLSurfaceView() {
		return new BeyondarGLSurfaceView(getActivity());
	}

	/**
	 * Override this method to personalize the {@link CameraView} that will be
	 * instantiated
	 * 
	 * @return
	 */
	protected CameraView createCameraView() {
		return new CameraView(getActivity());
	}

	/**
	 *
	 * Returns the CameraView for this class instance
	 * 
	 * @return
	 */
	public CameraView getCameraView(){
		return mBeyondarCameraView;
	}

     /**
	 * Returns the SurfaceView for this class instance
	 * 
	 * @return
	 */
	public BeyondarGLSurfaceView getGLSurfaceView(){
		return mBeyondarGLSurface;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		startRenderingAR();
		return mMailLayout;
	}

	@Override
	public void onResume() {
		super.onResume();
		mBeyondarCameraView.startPreviewCamera();
		mBeyondarGLSurface.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mBeyondarCameraView.stopPreviewCamera();
		mBeyondarGLSurface.onPause();
	}

	/**
	 * Set the listener to get notified when the user touch the AR view
	 * 
	 * @param listener
	 */
	public void setOnTouchBeyondarViewListener(OnTouchBeyondarViewListener listener) {
		mTouchListener = listener;
	}

	public void setOnClickBeyondarObjectListener(OnClikBeyondarObjectListener listener) {
		mClickListener = listener;
		mMailLayout.setClickable(true);
		mMailLayout.setOnClickListener(this);
	}

	@Override
	public boolean onTouch(View v, final MotionEvent event) {
		mLastScreenTouchX = event.getX();
		mLastScreenTouchY = event.getY();

		if (mWorld == null || mTouchListener == null || event == null) {
			return false;
		}
		mTouchListener.onTouchBeyondarView(event, mBeyondarGLSurface);
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == mMailLayout) {
			if (mClickListener == null) {
				return;
			}
			final float lastX = mLastScreenTouchX;
			final float lastY = mLastScreenTouchY;

			new Thread(new Runnable() {
				@Override
				public void run() {
					final ArrayList<BeyondarObject> beyondarObjects = new ArrayList<BeyondarObject>();
					mBeyondarGLSurface.getBeyondarObjectsOnScreenCoordinates(lastX, lastY, beyondarObjects);
					mBeyondarGLSurface.post(new Runnable() {
						@Override
						public void run() {
							OnClikBeyondarObjectListener listener = mClickListener;
							if (listener != null) {
								listener.onClikBeyondarObject(beyondarObjects);
							}
						}
					});
				}
			}).start();
		}
	}

	/**
	 * Get the world in use by the fragment
	 * 
	 * @return
	 */
	public World getWorld() {
		return mWorld;
	}

	/**
	 * Set the world to be shown
	 * 
	 * @param world
	 */
	public void setWorld(World world) {
		mWorld = world;
		mBeyondarGLSurface.setWorld(world);
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
		mBeyondarGLSurface.setSensorDelay(delay);
	}

	/**
	 * Get the current sensor delay. See {@link android.hardware.SensorManager}
	 * for more information
	 * 
	 * @return sensor delay
	 */
	public int getSensorDelay() {
		return mBeyondarGLSurface.getSensorDelay();
	}

	public void setFpsUpdatable(FpsUpdatable fpsUpdatable) {
		mBeyondarGLSurface.setFpsUpdatable(fpsUpdatable);
	}

	/**
	 * Force the GLSurface to stop rendering the AR world
	 */
	public void stopRenderingAR() {
		mBeyondarGLSurface.setVisibility(View.INVISIBLE);
	}

	/**
	 * Force the GLSurface to start rendering the AR world
	 */
	public void startRenderingAR() {
		mBeyondarGLSurface.setVisibility(View.VISIBLE);
	}

	/**
	 * Get the GeoObject that intersect with the coordinates x, y on the screen.<br>
	 * NOTE: When this method is called a new {@link List} is created.
	 * 
	 * @param x
	 * @param y
	 * 
	 * @return A new list with the {@link BeyondarObject} that collide with the
	 *         screen cord
	 */
	public List<BeyondarObject> getBeyondarObjectsOnScreenCoordinates(float x, float y) {
		ArrayList<BeyondarObject> beyondarObjects = new ArrayList<BeyondarObject>();
		mBeyondarGLSurface.getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects);
		return beyondarObjects;
	}

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
	public void getBeyondarObjectsOnScreenCoordinates(float x, float y,
			ArrayList<BeyondarObject> beyondarObjects) {
		mBeyondarGLSurface.getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects);
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
	public void getBeyondarObjectsOnScreenCoordinates(float x, float y,
			ArrayList<BeyondarObject> beyondarObjects, Ray ray) {
		mBeyondarGLSurface.getBeyondarObjectsOnScreenCoordinates(x, y, beyondarObjects, ray);

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
		mBeyondarGLSurface.setMaxDistanceSize(maxDistanceSize);
	}

	/**
	 * Get the max distance which a {@link GeoObject} will be rendered.
	 * 
	 * @return The current max distance. 0 is the default behavior
	 */
	public float getMaxDistanceSize() {
		return mBeyondarGLSurface.getMaxDistanceSize();
	}

	/**
	 * Show the number of frames per second. False by default
	 * 
	 * @param show
	 *            True to show the FPS, false otherwise
	 */
	public void showFPS(boolean show) {
		if (show) {
			if (mFpsTextView == null) {
				mFpsTextView = new TextView(getActivity());
				mFpsTextView.setBackgroundResource(android.R.color.black);
				mFpsTextView.setTextColor(getResources().getColor(android.R.color.white));
				android.view.ViewGroup.LayoutParams params = new LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				mMailLayout.addView(mFpsTextView, params);
			}
			mFpsTextView.setVisibility(View.VISIBLE);
			setFpsUpdatable(this);
		} else if (mFpsTextView != null) {
			mFpsTextView.setVisibility(View.GONE);
			setFpsUpdatable(null);
		}
	}

	@Override
	public void onFpsUpdate(final float fps) {
		if (mFpsTextView != null) {
			mFpsTextView.post(new Runnable() {
				@Override
				public void run() {
					mFpsTextView.setText("fps: " + fps);
				}
			});
		}
	}
}
