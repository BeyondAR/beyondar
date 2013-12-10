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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.util.Logger;
import com.beyondar.android.util.math.geom.Point2;
import com.beyondar.android.view.BeyondarViewAdapter;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.World;

public class AttachViewToGeoObjectActivity extends FragmentActivity implements OnClickBeyondarObjectListener {

	private BeyondarFragmentSupport mBeyondarFragment;
	private World mWorld;
	private Button button;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		attchView(mWorld);
		
		CustomBeyondarViewAdapter customBeyondarViewAdapter = new CustomBeyondarViewAdapter(this);
		mBeyondarFragment.setBeyondarViewAdapter(customBeyondarViewAdapter);
	}

	public void attchView(World world) {
		for (BeyondarObjectList beyondarList : world.getBeyondarObjectLists()) {
			for (BeyondarObject beyondarObject : beyondarList) {
				if (beyondarObject.getId() == 8d) {
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		return super.onTouchEvent(event);
	}

	// Arreglar el on touch, que el absorbeix el view qu hi ha dins del fragment
	// i no permet que aquesta activity revi el ebent

	@Override
	public void onClickBeyondarObject(ArrayList<BeyondarObject> beyondarObjects) {
//		if (beyondarObjects.size() == 0) {
//			return;
//		}
//		Point2 point = new Point2(0, 0);
//
//		BeyondarObject barObject = beyondarObjects.get(0);
//		mBeyondarFragment
//				.getGLSurfaceView()
//				.getRenderer()
//				.getScreenCoordinates(barObject.getPosition().x, barObject.getPosition().y,
//						barObject.getPosition().z, point);
//		Logger.d("Ray: " + point.x + "," + point.y);
//
//		if (button != null) {
//			mBeyondarFragment.getMainLayout().removeView(button);
//		}
//
//		android.view.ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT);
//		button = new Button(this);
//		button.setText("Hello");
//		button.setTranslationX((int) point.x);
//		button.setTranslationY((int) point.y);
//		// button.((int)point.x, (int)point.y, 0, 0);
//		mBeyondarFragment.getMainLayout().addView(button, params);
	}

	private class CustomBeyondarViewAdapter extends BeyondarViewAdapter {

		LayoutInflater inflater;

		public CustomBeyondarViewAdapter(Context context) {
			super(context);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent) {
			if (recycledView == null) {
				recycledView = inflater.inflate(R.layout.beyondar_object_view, null);
			}

			TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
			textView.setText(beyondarObject.getName());
			
			recycledView.setTranslationX(beyondarObject.getScreenPositionTopRight().x);
			recycledView.setTranslationY(beyondarObject.getScreenPositionTopRight().y);

			return recycledView;
		}

	}

}
