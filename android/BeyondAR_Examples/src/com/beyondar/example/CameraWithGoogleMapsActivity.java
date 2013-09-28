package com.beyondar.example;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.beyondar.android.opengl.util.FpsUpdatable;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.BeyondarGLSurfaceView.OnARTouchListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;

public class CameraWithGoogleMapsActivity extends Activity implements OnARTouchListener, FpsUpdatable, OnClickListener {

	private BeyondarGLSurfaceView mBeyondarGLSurfaceView;
	private World mWorld;

	private TextView mLabelText;
	private String mFPS, mAction;

	private Button mShowMap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		loadViewFromXML();

		// We create the world...
		mWorld = new World(this);
		// ...And fill it
		WorldHelper.generateObjects(mWorld);

		mBeyondarGLSurfaceView.setWorld(mWorld);
		mBeyondarGLSurfaceView.setFpsUpdatable(this);

		// set listener for the geoObjects
		mBeyondarGLSurfaceView.setOnARTouchListener(this);

		// We can use this method to store an unique world instance
		World.setWorld(mWorld);

	}

	@Override
	public void onTouchARView(MotionEvent event, BeyondarGLSurfaceView beyondarView) {

		float x = event.getX();
		float y = event.getY();

		ArrayList<BeyondarObject> geoObjects = new ArrayList<BeyondarObject>();

		beyondarView.getARObjectOnScreenCoordinates(x, y, geoObjects);

		String textEvent = "";

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			textEvent = "Event type ACTION_DOWN: ";
			break;
		case MotionEvent.ACTION_UP:
			textEvent = "Event type ACTION_UP: ";
			break;
		case MotionEvent.ACTION_MOVE:
			textEvent = "Event type ACTION_MOVE: ";
			break;
		default:
			break;
		}

		Iterator<BeyondarObject> iterator = geoObjects.iterator();
		while (iterator.hasNext()) {
			BeyondarObject geoObject = iterator.next();
			textEvent = textEvent + " " + geoObject.getName();

		}
		mAction = textEvent;
		updateLabelText();
	}

	@Override
	public void onFpsUpdate(float fps) {
		mFPS = "" + fps;
		updateLabelText();
	}

	private void updateLabelText() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String text = "";
				if (mFPS != null) {
					text = text + "FPS: " + mFPS;
				}
				if (mAction != null) {
					text = text + " | " + "Action: " + mAction;
				}
				mLabelText.setText(text);
			}
		});
	}

	private void loadViewFromXML() {
		setContentView(R.layout.camera_with_google_maps);
		mBeyondarGLSurfaceView = (BeyondarGLSurfaceView) findViewById(R.id.customGLSurface);
		mLabelText = (TextView) findViewById(R.id.labelText);
		// mCameraView = (CameraView) findViewById(R.id.camera);

		mShowMap = (Button) findViewById(R.id.showMapButton);

		mShowMap.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBeyondarGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBeyondarGLSurfaceView.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v == mShowMap) {
			Intent intent = new Intent(this, GoogleMapActivity.class);
			startActivity(intent);
		}
	}

}
