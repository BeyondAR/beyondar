package com.beyondar.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.beyondar.android.view.BeyondarView;
import com.beyondar.android.world.World;

public class SimpleCameraActivity extends Activity {

	private BeyondarView mBeyondarView;
	private World mWorld;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.simple_camera);
		mBeyondarView = (BeyondarView) findViewById(R.id.beyondarView);

		// We create the world...
		mWorld = new World(this);
		// ... fill it ...
		WorldHelper.generateObjects(mWorld);
		// .. and send it to the view
		mBeyondarView.setWorld(mWorld);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Every time that the activity is resumed we need to notify the BeyondarView
		mBeyondarView.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Every time that the activity is paused we need to notify the BeyondarView
		mBeyondarView.pause();
	}

}
