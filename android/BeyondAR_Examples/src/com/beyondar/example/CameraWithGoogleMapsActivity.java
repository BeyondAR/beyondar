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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.world.World;

public class CameraWithGoogleMapsActivity extends FragmentActivity implements OnClickListener {

	private BeyondarFragmentSupport mBeyondarFragment;
	private World mWorld;

	private Button mShowMap;

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
	}

	private void loadViewFromXML() {
		setContentView(R.layout.camera_with_google_maps);
		
		mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.beyondarFragment);

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
