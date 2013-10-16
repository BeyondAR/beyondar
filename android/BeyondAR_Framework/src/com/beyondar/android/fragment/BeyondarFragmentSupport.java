package com.beyondar.android.fragment;

import com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable;
import com.beyondar.android.view.BeyondarView;
import com.beyondar.android.view.BeyondarGLSurfaceView.OnARTouchListener;
import com.beyondar.android.world.World;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BeyondarFragmentSupport extends Fragment {

	private BeyondarView mBeyondarView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mBeyondarView = new BeyondarView(getActivity());
		mBeyondarView.startRenderingAR();
		return mBeyondarView;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Every time that the activity is resumed we need to notify the
		// BeyondarView
		mBeyondarView.resume();
		mBeyondarView.startRenderingAR();
	}

	@Override
	public void onPause() {
		super.onPause();
		// Every time that the activity is paused we need to notify the
		// BeyondarView
		mBeyondarView.pause();
		mBeyondarView.stopRenderingAR();
	}

	public void setOnARTouchListener(OnARTouchListener listener) {
		mBeyondarView.setOnARTouchListener(listener);
	}

	public void setWorld(World world) {
		mBeyondarView.setWorld(world);
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
		mBeyondarView.setSensorDelay(delay);
	}

	/**
	 * Get the current sensor delay. See {@link android.hardware.SensorManager}
	 * for more information
	 * 
	 * @return sensor delay
	 */
	public int getSensorDelay() {
		return mBeyondarView.getSensorDelay();
	}

	public void showFPS(boolean show) {
		mBeyondarView.showFPS(show);
	}

	public void setFpsUpdatable(FpsUpdatable fpsUpdatable) {
		mBeyondarView.setFpsUpdatable(fpsUpdatable);
	}

	public void stopRenderingAR() {
		mBeyondarView.setVisibility(View.INVISIBLE);
	}

	public void startRenderingAR() {
		mBeyondarView.setVisibility(View.VISIBLE);
	}
}
