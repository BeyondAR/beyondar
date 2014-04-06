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
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.view.BeyondarViewAdapter;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.World;

public class AttachViewToGeoObjectActivity extends FragmentActivity implements OnClickBeyondarObjectListener,
		OnClickListener {

	private BeyondarFragmentSupport mBeyondarFragment;
	private World mWorld;

	private List<BeyondarObject> showViewOn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showViewOn = Collections.synchronizedList(new ArrayList<BeyondarObject>());

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.simple_camera);

		mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.beyondarFragment);

		// We create the world and fill it ...
		mWorld = CustomWorldHelper.generateObjects(this);
		// .. and send it to the fragment
		mBeyondarFragment.setWorld(mWorld);

		// We also can see the Frames per seconds
		mBeyondarFragment.showFPS(true);

		mBeyondarFragment.setOnClickBeyondarObjectListener(this);

		CustomBeyondarViewAdapter customBeyondarViewAdapter = new CustomBeyondarViewAdapter(this);
		mBeyondarFragment.setBeyondarViewAdapter(customBeyondarViewAdapter);
		
		Toast.makeText(this, "Click on any object to attach it a view", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	@Override
	public void onClickBeyondarObject(ArrayList<BeyondarObject> beyondarObjects) {
		if (beyondarObjects.size() == 0) {
			return;
		}
		BeyondarObject beyondarObject = beyondarObjects.get(0);
		if (showViewOn.contains(beyondarObject)) {
			showViewOn.remove(beyondarObject);
		} else {
			showViewOn.add(beyondarObject);
		}
	}

	private class CustomBeyondarViewAdapter extends BeyondarViewAdapter {

		LayoutInflater inflater;

		public CustomBeyondarViewAdapter(Context context) {
			super(context);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent) {
			if (!showViewOn.contains(beyondarObject)) {
				return null;
			}
			if (recycledView == null) {
				recycledView = inflater.inflate(R.layout.beyondar_object_view, null);
			}

			TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
			textView.setText(beyondarObject.getName());
			Button button = (Button) recycledView.findViewById(R.id.button);
			button.setOnClickListener(AttachViewToGeoObjectActivity.this);

			// Once the view is ready we specify the position
			setPosition(beyondarObject.getScreenPositionTopRight());

			return recycledView;
		}

	}

}
