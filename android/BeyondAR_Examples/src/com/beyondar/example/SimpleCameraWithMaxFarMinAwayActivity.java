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

	private SeekBar mSeekBarPullCloserDistance, mSeekBarPushAwayDistance, mSeekBarMaxDistanceToRender, mSeekBarDistanceFactor;
	private TextView mMaxFarText, mMinFarText, mArViewDistanceText, mZfarText;
	private TextView mTextValues;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.simple_camera_with_distance_seekbars);
		mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.beyondarFragment);
		mTextValues = (TextView) findViewById(R.id.textValues);
		
		mMaxFarText = (TextView) findViewById(R.id.textBarMax);
		mMinFarText = (TextView) findViewById(R.id.textBarMin);
		mArViewDistanceText = (TextView) findViewById(R.id.textBarArViewDistance);
		mZfarText= (TextView) findViewById(R.id.textBarZFar);
		mMaxFarText.setText("Pull closer:");
		mMinFarText.setText("Push away:");
		mArViewDistanceText.setText("Max dst render:");
		mZfarText.setText("Dst factor:");
		
		mSeekBarPullCloserDistance = (SeekBar) findViewById(R.id.seekBarMax);
		mSeekBarPushAwayDistance = (SeekBar) findViewById(R.id.seekBarMin);
		mSeekBarMaxDistanceToRender = (SeekBar) findViewById(R.id.seekBarArViewDistance);
		mSeekBarDistanceFactor = (SeekBar) findViewById(R.id.seekBarZFar);
		mSeekBarPullCloserDistance.setMax(1000);
		mSeekBarPushAwayDistance.setMax(1000);
		mSeekBarMaxDistanceToRender.setMax(20000); // 20 km
		mSeekBarDistanceFactor.setMax(50000);
		
		mSeekBarPullCloserDistance.setOnSeekBarChangeListener(this);
		mSeekBarPushAwayDistance.setOnSeekBarChangeListener(this);
		mSeekBarMaxDistanceToRender.setOnSeekBarChangeListener(this);
		mSeekBarDistanceFactor.setOnSeekBarChangeListener(this);
		
		updateTextValues();
		
		mSeekBarPushAwayDistance.setProgress(115);
		mSeekBarMaxDistanceToRender.setProgress(20000);
		mSeekBarDistanceFactor.setProgress(50000);

		// We create the world and fill it ...
		mWorld = CustomWorldHelper.generateObjects(this);
		// .. and send it to the fragment
		mBeyondarFragment.setWorld(mWorld);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar == mSeekBarPullCloserDistance) {
			mBeyondarFragment.setPullCloserDistance(progress);
		} else if (seekBar == mSeekBarPushAwayDistance) {
			mBeyondarFragment.setPushAwayDistance(progress);
		} else if (seekBar == mSeekBarMaxDistanceToRender) {
			mBeyondarFragment.setMaxDistanceToRender(progress);
		} else if (seekBar == mSeekBarDistanceFactor) {
			mBeyondarFragment.setDistanceFactor(progress);
		}
		updateTextValues();
	}
	
	private void updateTextValues() {
		mTextValues.setText("dst factor=" + mBeyondarFragment.getDistanceFactor() + " max dst render="
				+ mBeyondarFragment.getMaxDistanceToRender() + "\npull closer="
				+ mBeyondarFragment.getPullCloserDistance() + " push away="
				+ mBeyondarFragment.getPushAwayDistance());
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

}
