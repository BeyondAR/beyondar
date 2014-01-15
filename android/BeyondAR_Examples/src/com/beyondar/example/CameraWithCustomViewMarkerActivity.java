package com.beyondar.example;

/*
 * @author Yasir.Ali <ali.yasir0@gmail.com>
 */

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.world.World;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class CameraWithCustomViewMarkerActivity extends FragmentActivity {

		private BeyondarFragmentSupport mBeyondarFragment;
		private World mWorld;

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
			mWorld = CustomWorldHelper.generateObjectsWithViewMarkers(this, getLayoutInflater());
			// .. and send it to the fragment
			mBeyondarFragment.setWorld(mWorld);

			// We also can see the Frames per seconds
			mBeyondarFragment.showFPS(true);

		}

	}
