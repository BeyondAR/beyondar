package com.beyondar.example;

import java.util.ArrayList;
import java.util.Iterator;

import com.beyondar.android.opengl.views.BeyondarGLSurfaceView;
import com.beyondar.android.opengl.views.IOnARTouchListener;
import com.beyondar.android.opengl.views.ARRenderer.IFpsUpdatable;
import com.beyondar.android.views.CameraView;
import com.beyondar.android.world.World;
import com.beyondar.android.world.objects.BeyondarObject;
import com.beyondar.android.world.objects.GeoObject;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity implements IOnARTouchListener, IFpsUpdatable {

	private static final int COMMAND_UPDATE_LABEL_TEXT = 25234;
	// private SensorManager mSensorManager;
	private CameraView mCameraView;

	private BeyondarGLSurfaceView mBeyondarGLSurfaceView;

	private TextView mLabelText;
	private String mFPS, mAction;

	//
	// private GLSurfaceView mGLSurfaceView;

	public static Context mContext;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case COMMAND_UPDATE_LABEL_TEXT:
				updateLabelText();
				break;
			default:
				break;
			}
		};
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Choose the way to load the views
		loadViewFromXML();
		// loadViewFromSource();

		// We have to create the world
		World world = createWorld();
		mBeyondarGLSurfaceView.setWorld(world);
		mBeyondarGLSurfaceView.setFpsUpdatable(this);

		// Create the listener for the geoObjects
		mBeyondarGLSurfaceView.setonARTouchListener(this);

		mContext = this;
	}

	@Override
	public void onTouchARView(MotionEvent event, BeyondarGLSurfaceView beyondarView) {

		float x = event.getX();
		float y = event.getY();

		ArrayList<BeyondarObject> geoObjects = new ArrayList<BeyondarObject>();

		beyondarView.getARObjectOnScreenCoordinates(x, y, geoObjects);

		Iterator<BeyondarObject> iterator = geoObjects.iterator();

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

		while (iterator.hasNext()) {
			BeyondarObject geoObject = iterator.next();
			textEvent = textEvent + " " + geoObject.getName();
			// geoObject.setVisibile(false);

		}
		// Toast.makeText(MainActivity.this, textEvent,
		// Toast.LENGTH_SHORT).show();
		mAction = textEvent;
		mHandler.sendEmptyMessage(COMMAND_UPDATE_LABEL_TEXT);

	}

	@Override
	public void onFpsUpdate(float fps) {
		mFPS = "" + fps;
		mHandler.sendEmptyMessage(COMMAND_UPDATE_LABEL_TEXT);
	}

	private void updateLabelText() {
		String text = "";
		if (mFPS != null) {
			text = text + "FPS: " + mFPS;
		}
		if (mAction != null) {
			text = text + " | " + "Action: " + mAction;
		}
		mLabelText.setText(text);
	}

	private void loadViewFromSource() {
		mBeyondarGLSurfaceView = new BeyondarGLSurfaceView(this);
		setContentView(mBeyondarGLSurfaceView);
	}

	private void loadViewFromXML() {
		setContentView(R.layout.main);
		mBeyondarGLSurfaceView = (BeyondarGLSurfaceView) findViewById(R.id.customGLSurface);
		mLabelText = (TextView) findViewById(R.id.labelText);
		mCameraView = (CameraView) findViewById(R.id.camera);
	}

	private World createWorld() {
		// Create the world instance
		World world = new World(this);

		// User position (you can change it using the GPS listeners form
		// Android
		// API)
		world.setLongitude(1.925848038959814d);
		world.setLatitude(41.26533734214473d);

		GeoObject go1 = new GeoObject(1l);
		go1.setLongitude(1.926036406654116d);
		go1.setLatitude(41.26523339794433d);
		go1.setImageResource(R.drawable.creature_1);
		go1.setName("Creature 1");

		GeoObject go2 = new GeoObject(2l);
		go2.setLongitude(1.925662767707665d);
		go2.setLatitude(41.26518862002349d);
		go2.setImageResource(R.drawable.creature_2);
		go2.setName("Creature 2");

		GeoObject go3 = new GeoObject(3l);
		go3.setLongitude(1.92582424468222d);
		go3.setLatitude(41.26518966360719d);
		// go3.setImageUri("http://ettugamer.com/wp-content/gallery/marvel-vs-capcom-3-3/marvel_vs_capcom_3_ironman.png");
		go3.setImageUri("http://images2.wikia.nocookie.net/__cb20120507035042/marvelmovies/images/1/19/TheAvengers_IronMan.jpg");
		// go3.setImageUri("http://beyondar.com/pictures/ironman.png");
		// go3.setImageUri("assets://TheAvengers_IronMan.jpeg");
		go3.setName("Online image");

		GeoObject go4 = new GeoObject(4l);
		go4.setLongitude(1.925873388087619d);
		go4.setLatitude(41.26550959641445d);
		go4.setImageUri("/sdcard/TheAvengers_IronMan.jpeg");
		go4.setName("Creature 4");

		GeoObject go5 = new GeoObject(5l);
		go5.setLongitude(1.925777906882577d);
		go5.setLatitude(41.26553066234138d);
		go5.setImageResource(R.drawable.creature_5);
		go5.setName("Creature 5");

		GeoObject go6 = new GeoObject(6l);
		go6.setLongitude(1.925250806050688d);
		go6.setLatitude(41.26496218466268d);
		go6.setImageResource(R.drawable.creature_6);
		go6.setName("Creature 6");

		GeoObject go7 = new GeoObject(6l);
		go7.setLongitude(1.925932313852319d);
		go7.setLatitude(41.26581776104766d);
		go7.setImageResource(R.drawable.creature_7);
		go7.setName("Creature 7");

		GeoObject go8 = new GeoObject(6l);
		go8.setLongitude(1.926164369775198d);
		go8.setLatitude(41.26534261025682d);
		go8.setImageResource(R.drawable.image_test_pow2_small);
		go8.setName("Object 8");

		world.addBeyondarObject(go1);
		world.addBeyondarObject(go2);
		world.addBeyondarObject(go3);
		// world.addBeyondarObject(go4);
		world.addBeyondarObject(go5);
		world.addBeyondarObject(go6);
		world.addBeyondarObject(go7);
		world.addBeyondarObject(go8);

		// The user can set the default bitmap. This is useful if you are
		// loading
		// images form Internet and the connection is lots
		world.setDefaultBitmap(R.drawable.beyondar_default_unknow_icon);

		// You can force the images to be loaded
		// world.loadImages();

		return world;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// mGLSurfaceView.onResume();
		mBeyondarGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// mGLSurfaceView.onPause();
		mBeyondarGLSurfaceView.onPause();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void onStop() {
		super.onStop();
	}

}
