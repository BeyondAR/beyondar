package com.beyondar.example;

/*
 * @author Yasir.Ali <ali.yasir0@gmail.com>
 * 
 * GenRadar Library
 * com.yasiralijaved.genradar
 * Copied from
 * https://github.com/yasiralijaved/GenRadar
 */

import java.util.ArrayList;
import java.util.List;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.world.World;
import com.yasiralijaved.genradar.main.GenRadarManager;
import com.yasiralijaved.genradar.main.GenRadarPoint;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.LinearLayout;

public class CameraWithGenRadarActivity extends FragmentActivity {
	
	private BeyondarFragmentSupport mBeyondarFragment;
	private World mWorld;
	
	// Radar
	private GenRadarManager mGenRadar;
	private List<GenRadarPoint> mGenRadarPoints;
	private GenRadarPoint mCentralGenRadarPoint;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_camera_with_gen_radar);
		
		mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(
				R.id.beyondarFragment);

		// We create the world and fill it ...
		mWorld = CustomWorldHelper.generateObjects(this);
		// .. and send it to the fragment
		mBeyondarFragment.setWorld(mWorld);

		// We also can see the Frames per seconds
		mBeyondarFragment.showFPS(true);
		
		mCentralGenRadarPoint = new GenRadarPoint("Center Point", 33.683232, 72.988972, 0, 0, GenRadarManager.POINT_RADIUS, Color.RED);
		
		mGenRadar = new GenRadarManager(CameraWithGenRadarActivity.this, (LinearLayout) findViewById(R.id.container), 120, 120);
		
		initRadar2();

	}
	
	/*
	 * This method creates Sample POIs
	 */
	private void initRadar2() {

		mGenRadarPoints = new ArrayList<GenRadarPoint>();
		
		float radius = 1.5f;
		//MyRadarPoint currentLocation = getCircle(33.683232, 72.988972, Color.RED);

		// Center Point
		//myRadarPoints.add( new MyRadarPoint("Center Point", 33.683232, 72.988972, 0, 0, radius, Color.RED) );

		// Model Filling Station
		mGenRadarPoints.add( new GenRadarPoint("Model Filling Station", 33.685354, 72.985651, 0, 0, radius, Color.BLUE) );

		// IMCB
		mGenRadarPoints.add( new GenRadarPoint("IMCB", 33.688210, 72.991315, 0, 0, radius, Color.YELLOW) );

		// UnKnown
		mGenRadarPoints.add( new GenRadarPoint("UnKnown", 33.684854, 72.991315, 0, 0, radius, Color.WHITE) );

		// Shifa medical Center
		mGenRadarPoints.add( new GenRadarPoint("Shifa medical Center", 33.683836, 72.986573, 0, 0, radius, Color.GREEN) );

		// Alian Enterprises
		mGenRadarPoints.add( new GenRadarPoint("Alian Enterprises", 33.681399, 72.990545, 0, 0, radius, Color.CYAN) );

		// Sadar police Station
		mGenRadarPoints.add( new GenRadarPoint("Sadar police Station", 33.691424, 72.970287, 0, 0, radius, Color.MAGENTA) );

		// Spain
		//myRadarPoints.add( new MyRadarPoint("Spain", 40.178873, -3.793030, 0, 0, radius, Color.parseColor("#EA8622")) );

		
		mGenRadar.initAndUpdateRadarWithPoints(mCentralGenRadarPoint, mGenRadarPoints);

	}
}
