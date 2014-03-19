Beyondar
========

This framework has been designed to offer some resources to those developers with an interest in working with Augmented Reality based on geolocalization on SmartPhones and tablets.

[BeyondAR Game](https://play.google.com/store/apps/details?id=com.beyondar#?t=W251bGwsMSwxLDIxMiwiY29tLmJleW9uZGFyIl0.) is using this platform to show the creatures around the user. Here some images:

![Screenshot](http://beyondar.com/pictures/screenshots/screen_4.jpg)
![Screenshot](http://beyondar.com/pictures/screenshots/screen_1.jpg)

BeyondAR platform also supports Google Glass

![Screenshot](http://beyondar.com/pictures/glass.jpg)

##Adding BeyondAR in to your project

Just download the latest version of the framework [here](https://github.com/BeyondAR/beyondar/tree/master/android/libs) and add the needed jar files in to you libs folder.

* beyondar-v#.jar: The basic lib to be able to run the framework
* beyondar-googlemap-module-v#.jar: Module to use GoogleMaps with your `World`

##How to build your first app

To be able to run BeyondAR we need to add the following lines on the AndroidManifest.xml
```xml
<!-- Minimum permissions for Beyondar -->
<uses-permission android:name="android.permission.CAMERA" />
    
<!-- For beyondar this is not mandatory unless you want to load something from internet (for instance images) -->
<uses-permission android:name="android.permission.INTERNET" />

<!--  BeyondAR needs the following features-->
<uses-feature android:glEsVersion="0x00020000" android:required="true" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-feature android:name="android.hardware.sensor.accelerometer" />
<uses-feature android:name="android.hardware.sensor.compass" />
```

To create the UI it we can choose using an Android Layout XML or using java code. For both of them we can use the `BeyondarFragmentSupport` or the `BeyondarFragment` fragments.

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >

    <fragment
        android:id="@+id/beyondarFragment"
        android:name="com.beyondar.android.fragment.BeyondarFragmentSupport"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>

```
Once we have the layout ready it is time to load it.

```java
private BeyondarFragmentSupport mBeyondarFragment;
...
@Override
public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     setContentView(R.layout.simple_camera);
     mBeyondarFragment = (BeyondarFragmentSupport)
     getSupportFragmentManager().findFragmentById(R.id.beyondarFragment);
     ...
}
```

The next step is to create the `World` that holds the information related to the objects that need to be displayed in the app using augmented reality.

```java
World world = new World(context);

// The user can set the default bitmap. This is useful if you are
// loading images form Internet and the connection get lost
world.setDefaultBitmap(R.drawable.beyondar_default_unknow_icon);

// User position (you can change it using the GPS listeners form Android
// API)
world.setGeoPosition(41.26533734214473d, 1.925848038959814d);

// Create an object with an image in the app resources.
GeoObject go1 = new GeoObject(1l);
go1.setGeoPosition(41.26523339794433d, 1.926036406654116d);
go1.setImageResource(R.drawable.creature_1);
go1.setName("Creature 1");

// Is it also possible to load the image asynchronously form internet
GeoObject go2 = new GeoObject(2l);
go2.setGeoPosition(41.26518966360719d, 1.92582424468222d);
go2.setImageUri("http://beyondar.com/sites/default/files/logo_reduced.png");
go2.setName("Online image");

// Also possible to get images from the SDcard
GeoObject go3 = new GeoObject(3l);
go3.setGeoPosition(41.26550959641445d, 1.925873388087619d);
go3.setImageUri("/sdcard/TheAvengers_IronMan.jpeg");
go3.setName("IronMan from sdcard");

// And the same goes for the app assets
GeoObject go4 = new GeoObject(4l);
go4.setGeoPosition(41.26518862002349d, 1.925662767707665d);
go4.setImageUri("assets://creature_7.png");
go4.setName("Image from assets");

//We add this GeoObjects to the world
world.addBeyondarObject(go1);
world.addBeyondarObject(go2);
world.addBeyondarObject(go3);
world.addBeyondarObject(go4);

//Finally we add the Wold data in to the fragment
mBeyondarFragment.setWorld(mWorld);
```
Now we have the app ready to show the GeoObjects. But we also want to capture events, for instance, when the user clicks on a GeoObject. For that we need to implement `OnClikBeyondarObjectListener`
```java
...
mBeyondarFragment.setOnClickBeyondarObjectListener(this);
...
@Override
public void onClickBeyondarObject(ArrayList<BeyondarObject> beyondarObjects) {
		// The first element in the array belongs to the closest BeyondarObject
		Toast.makeText(this, "Clicked on: " + beyondarObjects.get(0).getName(), Toast.LENGTH_LONG).show();
	}
```
We also can capture the touch events using the `OnTouchBeyondarViewListener`:
```java
...
@Override
public void onTouchBeyondarView(MotionEvent event, BeyondarGLSurfaceView beyondarView) {

	float x = event.getX();
	float y = event.getY();

	ArrayList<BeyondarObject> geoObjects = new ArrayList<BeyondarObject>();
	
	//This method call is better to don't do it in the UI thread!
	// This method is also available in the BeyondarFragment
	getBeyondarObjectsOnScreenCoordinates(x, y, geoObjects);

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
		...
		// Do something
		...

	}
}
...
```

##Adding GoogleMaps support
BeyondAR Framework uses modules to be able to add multiple features to the world engine. Google Maps is one example.

To draw the all the `World` elements in the Google Map framework we just need a few lines of code:

```java
...
private GoogleMap mMap;
private GoogleMapWorldModule mGoogleMapModule;
...

@Override
protected void onCreate(Bundle savedInstanceState) {
     ...
     mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

     // We create the world...
     mWorld = new World(this);
     // As we want to use GoogleMaps, we are going to create the module and
     // attach it to the World
     mGoogleMapModule = new GoogleMapWorldModule();
     // Then we need to set the map in to the GoogleMapModule
     mGoogleMapModule.setGoogleMap(mMap);
     // Now that we have the module created let's add it in to our world
     mWorld.addModule(mGoogleMapModule);
     
     // Now we fill the world
     ...
}
```
The `GoogleMapWorldModule` will take care of drawing all the `GeoObjects` in the `GoogleMap` object. So we also can add the a listener to the map to get notify when a `Marker` is click and then we can check which `GeoObject` is the owner of that `Marker`:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
     ...
     mMap.setOnMarkerClickListener(this);
     ...
}

@Override
public boolean onMarkerClick(Marker marker) {
     // To get the GeoObject that owns the marker we use the following
     // method:
     GeoObject geoObject = mGoogleMapModule.getGeoObjectOwner(marker);
     if (geoObject != null) {
          Toast.makeText(this, "Click on a marker owned by a GeoOject with the name: " + geoObject.getName(), Toast.LENGTH_SHORT).show();
     }
     return false;
}
```	


