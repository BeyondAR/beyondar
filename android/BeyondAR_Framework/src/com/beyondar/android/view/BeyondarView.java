package com.beyondar.android.view;

import com.beyondar.android.opengl.util.FpsUpdatable;
import com.beyondar.android.view.BeyondarGLSurfaceView.OnARTouchListener;
import com.beyondar.android.world.World;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class BeyondarView extends FrameLayout implements FpsUpdatable {

	private CameraView mBeyondarCameraView;
	private BeyondarGLSurfaceView mBeyondarGLSurface;
	private TextView mFpsTextView;

	public BeyondarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BeyondarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BeyondarView(Context context) {
		super(context);
		init();
	}

	private void init() {
		android.view.ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		mBeyondarGLSurface = getBeyondarGLSurfaceView();
		mBeyondarCameraView = getCameraView();

		addView(mBeyondarCameraView, params);
		addView(mBeyondarGLSurface, params);
	}

	/**
	 * Override this method to personalize the {@link BeyondarGLSurfaceView}
	 * that will be instantiated
	 * 
	 * @return
	 */
	protected BeyondarGLSurfaceView getBeyondarGLSurfaceView() {
		return new BeyondarGLSurfaceView(getContext());
	}

	/**
	 * Override this method to personalize the {@link CameraView}
	 * that will be instantiated
	 * 
	 * @return
	 */
	protected CameraView getCameraView() {
		return new CameraView(getContext());
	}
	
	public void stopRenderingAR(){
		mBeyondarGLSurface.setVisibility(View.INVISIBLE);
	}
	
	public void startRenderingAR(){
		mBeyondarGLSurface.setVisibility(View.VISIBLE);
	}

	public void pause() {
		mBeyondarGLSurface.onPause();
	}

	public void resume() {
		mBeyondarGLSurface.onResume();
	}

	public void setOnARTouchListener(OnARTouchListener listener) {
		mBeyondarGLSurface.setOnARTouchListener(listener);
	}

	public void setWorld(World world) {
		mBeyondarGLSurface.setWorld(world);
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		mBeyondarGLSurface.setVisibility(visibility);
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

	public void showFPS(boolean show) {
		if (show) {
			if (mFpsTextView == null) {
				mFpsTextView = new TextView(getContext());
				mFpsTextView.setBackgroundResource(android.R.color.black);
				mFpsTextView.setTextColor(getResources().getColor(android.R.color.white));
				android.view.ViewGroup.LayoutParams params = new LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				addView(mFpsTextView, params);
			}
			mFpsTextView.setVisibility(View.VISIBLE);
			setFpsUpdatable(this);
		} else if (mFpsTextView != null) {
			mFpsTextView.setVisibility(View.GONE);
			setFpsUpdatable(null);
		}
	}

	public void setFpsUpdatable(FpsUpdatable fpsUpdatable) {
		mBeyondarGLSurface.setFpsUpdatable(fpsUpdatable);
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
