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
package com.beyondar.example;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.view.BeyondarGLSurfaceView;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.view.OnTouchBeyondarViewListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;

public class ChangeGeoObjectImagesOnTouchActivity extends FragmentActivity implements OnTouchBeyondarViewListener,
        OnClickBeyondarObjectListener {

	private BeyondarFragmentSupport mBeyondarFragment;
	private World mWorld;

	private TextView mLabelText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		loadViewFromXML();

		// We create the world and fill it
		mWorld = CustomWorldHelper.generateObjects(this);

		mBeyondarFragment.setWorld(mWorld);
		mBeyondarFragment.showFPS(true);

		// set listener for the geoObjects
		mBeyondarFragment.setOnTouchBeyondarViewListener(this);
		mBeyondarFragment.setOnClickBeyondarObjectListener(this);

	}

	@Override
	public void onTouchBeyondarView(MotionEvent event, BeyondarGLSurfaceView beyondarView) {

		float x = event.getX();
		float y = event.getY();

		ArrayList<BeyondarObject> geoObjects = new ArrayList<BeyondarObject>();

		// This method call is better to don't do it in the UI thread!
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
		mLabelText.setText("Event: " + textEvent);
	}

	private void loadViewFromXML() {
		setContentView(R.layout.camera_with_text);
		mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.beyondarFragment);

		mLabelText = (TextView) findViewById(R.id.labelText);

	}

	@Override
	public void onClickBeyondarObject(ArrayList<BeyondarObject> beyondarObjects) {
		if (beyondarObjects.size() > 0) {
			beyondarObjects.get(0).setImageResource(R.drawable.splash);
		}
	}

}
