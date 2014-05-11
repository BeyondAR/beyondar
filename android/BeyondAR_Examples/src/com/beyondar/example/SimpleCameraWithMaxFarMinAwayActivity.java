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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.world.World;

public class SimpleCameraWithMaxFarMinAwayActivity extends FragmentActivity implements OnSeekBarChangeListener {

	private BeyondarFragmentSupport mBeyondarFragment;
	private World mWorld;

	private SeekBar mSeekBarMax, mSeekBarMin, mSeekBarArViewDst;
	private TextView mMaxFarText, mMinFarText, mArViewDistanceText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.simple_camera_with_distance_seekbars);
		mMaxFarText = (TextView) findViewById(R.id.textBarMax);
		mMinFarText = (TextView) findViewById(R.id.textBarMin);
		mArViewDistanceText = (TextView) findViewById(R.id.textBarArViewDistance);
		mMaxFarText.        setText("Max far:    ");
		mMinFarText.        setText("Min far:    ");
		mArViewDistanceText.setText("Ar view dst:");
		
		mSeekBarMax = (SeekBar) findViewById(R.id.seekBarMax);
		mSeekBarMin = (SeekBar) findViewById(R.id.seekBarMin);
		mSeekBarArViewDst = (SeekBar) findViewById(R.id.seekBarArViewDistance);
		mSeekBarMax.setOnSeekBarChangeListener(this);
		mSeekBarMin.setOnSeekBarChangeListener(this);
		mSeekBarArViewDst.setOnSeekBarChangeListener(this);
		mSeekBarMax.setMax(100);
		mSeekBarMin.setMax(100);
		mSeekBarArViewDst.setMax(1000);

		mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.beyondarFragment);

		// We create the world and fill it ...
		mWorld = CustomWorldHelper.generateObjects(this);
		// .. and send it to the fragment
		mBeyondarFragment.setWorld(mWorld);

		// We also can see the Frames per seconds
		mBeyondarFragment.showFPS(true);
		
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (mBeyondarFragment == null) return;
		
		if (seekBar == mSeekBarMax) {
			mBeyondarFragment.setMaxFarDistance(progress);
		} else if (seekBar == mSeekBarMin) {
			mBeyondarFragment.setMinFarDistanceSize(progress);
		} else if (seekBar == mSeekBarArViewDst) {
			mBeyondarFragment.setArViewDistance(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

}
