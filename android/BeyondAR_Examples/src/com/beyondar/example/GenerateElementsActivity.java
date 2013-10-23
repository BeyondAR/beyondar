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
package com.beyondar.example;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.opengl.renderer.ARRenderer.FpsUpdatable;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.OnTouchBeyondarViewListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;

public class GenerateElementsActivity extends FragmentActivity implements OnTouchBeyondarViewListener,
		FpsUpdatable, OnClickListener {

	private BeyondarFragmentSupport mBeyondarFragment;
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

		// We create the world and fill it
		mWorld = new World(this);

		mBeyondarFragment.setWorld(mWorld);
		mBeyondarFragment.setFpsUpdatable(this);

		// set listener for the geoObjects
		mBeyondarFragment.setOnTouchBeyondarViewListener(this);

		Toast.makeText(this, "Touch the screen to create an object", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onTouchBeyondarView(MotionEvent event, BeyondarGLSurfaceView beyondarView) {

		float x = event.getX();
		float y = event.getY();

		ArrayList<BeyondarObject> geoObjects = new ArrayList<BeyondarObject>();

		beyondarView.getBeyondarObjectsOnScreenCoordinates(x, y, geoObjects);

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
		mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.beyondarFragment);

		mLabelText = (TextView) findViewById(R.id.labelText);

		mShowMap = (Button) findViewById(R.id.showMapButton);

		mShowMap.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mShowMap) {
			Intent intent = new Intent(this, GoogleMapActivity.class);
			startActivity(intent);
		}
	}

}
