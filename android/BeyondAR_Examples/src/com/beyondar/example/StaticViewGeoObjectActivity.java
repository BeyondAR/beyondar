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

import java.io.File;
import java.io.IOException;
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
import com.beyondar.android.util.ImageUtils;
import com.beyondar.android.view.BeyondarViewAdapter;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;
import com.beyondar.android.world.World;

public class StaticViewGeoObjectActivity extends FragmentActivity {

	private static final String TMP_IMAGE_PREFIX = "viewImage_";

	private BeyondarFragmentSupport mBeyondarFragment;
	private World mWorld;

	private List<BeyondarObject> showViewOn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// The first thing that we do is to remove all the generated temporal
		// images. Remember that the application needs external storage write
		// permission.
		cleanTempFolder();

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

		replaceImagesByStaticViews(mWorld);

	}

	private void replaceImagesByStaticViews(World world) {
		String path = getTmpPath();

		for (BeyondarObjectList beyondarList : world.getBeyondarObjectLists()) {
			for (BeyondarObject beyondarObject : beyondarList) {
				View view = getLayoutInflater().inflate(R.layout.static_beyondar_object_view, null);
				TextView textView = (TextView) view.findViewById(R.id.geoObjectName);
				textView.setText(beyondarObject.getName());
				try {
					String tmpPath = TMP_IMAGE_PREFIX + view.hashCode() + ".png";
					ImageUtils.storeView(view, path, tmpPath);
					beyondarObject.setImageUri(path + tmpPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getTmpPath() {
		return getExternalFilesDir(null).getAbsoluteFile() + "/tmp/";
	}

	private void cleanTempFolder() {
		File tmpFolder = new File(getTmpPath());
		if (tmpFolder.isDirectory()) {
			String[] children = tmpFolder.list();
			for (int i = 0; i < children.length; i++) {
				if (children[i].startsWith(TMP_IMAGE_PREFIX)) {
					new File(tmpFolder, children[i]).delete();
				}
			}
		}
	}

}
