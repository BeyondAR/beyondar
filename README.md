Beyondar
========

This framework has been designed to offer some resources to those developers with an interest in working with Augmented Reality based on geolocalization on SmartPhones and tablets.

Don't forget to check the [wiki](https://github.com/BeyondAR/beyondar/wiki) and the [documentation](http://beyondar.github.io/beyondar/doxygen/index.html) to get more information about how to use this framework.

BeyondAR platform only supports Android (Google Glass included). We are currently working to support other platforms.


<table  border="0" align="center">
<tr>
  <td><img src="http://beyondar.github.io/beyondar/images/android.png"></td>
  <td><img src="http://beyondar.github.io/beyondar/images/glass.jpg"></td> 
</tr>
</table>


[BeyondAR Game](https://play.google.com/store/apps/details?id=com.beyondar#?t=W251bGwsMSwxLDIxMiwiY29tLmJleW9uZGFyIl0.) is using this platform to show the creatures around the user. Here some images:


<table  border="0" align="center">
<tr>
  <td><img src="http://beyondar.github.io/beyondar/images/screen_4.jpg"></td>
  <td><img src="http://beyondar.github.io/beyondar/images/screen_1.jpg"></td> 
</tr>
</table>

## Adding BeyondAR in to your project

Just download the latest version of the framework [here](android/libs) and add the needed jar files in to you libs folder.

* beyondar-v#.jar: The basic lib to be able to run the framework
* beyondar-googlemap-plugin-v#.jar: Plugin to use GoogleMaps with your `World `

##How to build your first app

To be able to run BeyondAR we need to add the following lines on the AndroidManifest.xml

~~~xml
<!-- Minimum permissions for Beyondar -->
<uses-permission android:name="android.permission.CAMERA" />
    
<!-- For beyondar this is not mandatory unless you want to load something from Internet (for instance images) -->
<uses-permission android:name="android.permission.INTERNET" />

<!--  BeyondAR needs the following features-->
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-feature android:name="android.hardware.sensor.accelerometer" />
<uses-feature android:name="android.hardware.sensor.compass" />
~~~

To create the UI it we can choose using an Android Layout XML or using Java code. For both of them we can use the `BeyondarFragmentSupport ` or the `BeyondarFragment ` fragments.

~~~xml
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

~~~
Once we have the layout ready it is time to load it.

~~~java
private BeyondarFragmentSupport mBeyondarFragment;
// ...
@Override
public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     setContentView(R.layout.simple_camera);
     mBeyondarFragment = (BeyondarFragmentSupport)
     getSupportFragmentManager().findFragmentById(R.id.beyondarFragment);
     // ...
}

~~~

The next step is to create the `World ` that holds the information related to the objects that need to be displayed in the app using augmented reality.

~~~java
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
~~~
Now we have the app ready to show the GeoObjects. But we also want to capture events, for instance, when the user clicks on a GeoObject. For that we need to implement `OnClikBeyondarObjectListener `
~~~java
// ...
mBeyondarFragment.setOnClickBeyondarObjectListener(this);
// ...
@Override
public void onClickBeyondarObject(ArrayList<BeyondarObject> beyondarObjects) {
		// The first element in the array belongs to the closest BeyondarObject
		Toast.makeText(this, "Clicked on: " + beyondarObjects.get(0).getName(), Toast.LENGTH_LONG).show();
	}
~~~

We also can capture the touch events using the `OnTouchBeyondarViewListener `:

~~~java
// ...
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
		// Do something
	}
}
// ...
~~~

## Adding GoogleMaps plugin
BeyondAR Framework uses plugins to be able to add multiple features to the world engine. Google Maps Plugin is one example ([jar](https://github.com/BeyondAR/beyondar/tree/master/android/libs/plugins) & [src](android/BeyondAR_Framework/src-googlemaps)).

To draw the all the `World ` elements in the Google Map framework we just need a few lines of code:

~~~java
// ...
private GoogleMap mMap;
private GoogleMapWorldPlugin mGoogleMapPlugin;
// ...

@Override
protected void onCreate(Bundle savedInstanceState) {
     // ...
     mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

     // We create the world...
     mWorld = new World(this);
     // As we want to use GoogleMaps, we are going to create the plugin and
     // attach it to the World
     mGoogleMapPlugin = new GoogleMapWorldPlugin(context);
     // Then we need to set the map in to the GoogleMapPlugin
     mGoogleMapPlugin.setGoogleMap(mMap);
     // Now that we have the plugin created let's add it in to our world
     mWorld.addPlugin(mGoogleMapPlugin);
     
     // Now we fill the world
     // ...
}
~~~
The `GoogleMapWorldPlugin ` will take care of drawing all the `GeoObjects ` in the `GoogleMap ` object. So we also can add the a listener to the map to get notify when a `Marker ` is click and then we can check which `GeoObject ` is the owner of that `Marker `:

~~~java
@Override
protected void onCreate(Bundle savedInstanceState) {
     // ...
     mMap.setOnMarkerClickListener(this);
     // ...
}

@Override
public boolean onMarkerClick(Marker marker) {
     // To get the GeoObject that owns the marker we use the following
     // method:
     GeoObject geoObject = mGoogleMapPlugin.getGeoObjectOwner(marker);
     if (geoObject != null) {
          Toast.makeText(this, "Click on a marker owned by a GeoOject with the name: " + geoObject.getName(), Toast.LENGTH_SHORT).show();
     }
     return false;
}
~~~	
## Add radar view plugin

If you want to add a radar view you could use the Radar plugin ([jar](android/libs/plugins) & [src](android/BeyondAR_Framework/src-radar)).

![radar](http://beyondar.com/pictures/radar.jpg)

To do that let's add the view in our layout file:

~~~xml
<FrameLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="right|top"
    android:background="@drawable/radar_bg_small" >

    <com.beyondar.android.plugin.radar.RadarView
        android:id="@+id/radarView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/radar_north_small" />
</FrameLayout>
~~~

Now it is time to get the view in our `Activity ` and play with it

~~~java
public void onCreate(Bundle savedInstanceState) {
    // ...
    radarView = (RadarView) findViewById(R.id.radarView);
    // Create the Radar plugin
    mRadarPlugin = new RadarWorldPlugin();
    // set the radar view in to our radar plugin
    mRadarPlugin.setRadarView(mRadarView);
    // Set how far (in meters) we want to display in the view
    mRadarPlugin.setMaxDistance(100);
    // and finally let's add the plugin
    mWorld.addPlugin(mRadarPlugin);
    // ...
}

~~~

## Creating your own plugin

BeyondAR architecture allows you to create your own plugins that can be attached to the framework. For instance to have a better access to the `World ` object in order to perform other extra task like using Google Maps, or a radar view.

The first thing that we need to do is understand what do we need to implement in order to create our plugin. The interfaces are located in `com.beyondar.android.plugin `:

* `WorldPlugin `: This interface will allow your plugin to be notified when some events occur, like when the position has changed, a new object has been added/removed, all the `World ` is cleaned, etc. ([here](android/BeyondAR_Framework/src/com/beyondar/android/plugin/WorldPlugin.java) you will find the code).
* `BeyondarObjectPlugin `: This interface allows your plugin to get notified when there are changes in a specific `BeyondarObject ` ([here](android/BeyondAR_Framework/src/com/beyondar/android/plugin/BeyondarObjectPlugin.java) you will find the code).
* `GeoObjectPlugin `: This interface extends `BeyondarObjectPlugin ` and it have some extra code to make easier the control of the geo position of this kind of objects ([here](android/BeyondAR_Framework/src/com/beyondar/android/plugin/BeyondarObjectPlugin.java) you will find the code).

One of the main goals of `WorldPlugin ` is to add `BeyondarObjectPlugin `/`GeoObjectPlugin ` to all the `BeyondarObject `/`GeoObject ` in the `World ` object. To do that make sure to add you own plugin in when `setup(World world) ` is called and when a new `BeyondarObject ` is added:

~~~java
@Override
public void onBeyondarObjectAdded(BeyondarObject beyondarObject, BeyondarObjectList beyondarObjectList) {
     if (beyondarObject instanceof GeoObject) { // Check if it is a GeoObject
        // We need to check if there is any of our own plugin already attached
        if (!beyondarObject.containsAnyPlugin(RadarPointPlugin.class)) { 
            // Then we just create it and add it
            RadarPointPlugin plugin = new RadarPointPlugin(this, beyondarObject);
            beyondarObject.addPlugin(plugin);
        }
    }
}
~~~

Once we have created the plugin we need to add it to the `World ` class, for that we just use the method `myWorld.addPlugin(myPlugin) `.

~~~java
World myWorld = new World(context);
// add the plugin
myWorld.addPlugin(myPlugin);
~~~



