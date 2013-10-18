package com.beyondar.android.fragment;

import com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.CameraView;
import com.beyondar.android.view.BeyondarGLSurfaceView.OnARTouchListener;
import com.beyondar.android.world.World;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

@SuppressLint("NewApi")
public class BeyondarFragment extends Fragment implements FpsUpdatable {

	private CameraView mBeyondarCameraView;
	private BeyondarGLSurfaceView mBeyondarGLSurface;
	private TextView mFpsTextView;
	private FrameLayout mMailLayout;

	private World mWorld;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private void init() {
		android.view.ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		mMailLayout = new FrameLayout(getActivity());
		mBeyondarGLSurface = getBeyondarGLSurfaceView();
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
	public void setOnARTouchListener(OnARTouchListener listener) {
		mBeyondarGLSurface.setOnARTouchListener(listener);
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
