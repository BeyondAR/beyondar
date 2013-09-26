package com.beyondar.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener {

	private ListView mLisViewt;
	private String[] values = new String[] { "Simple AR camera", "BeyondAR World + Google maps" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLisViewt = (ListView) findViewById(R.id.examplesList);

		fillList();
	}

	private void fillList() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				values);
		mLisViewt.setAdapter(adapter);
		mLisViewt.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		switch (pos) {
		case 0:
			openActivity(SimpleCameraActivity.class);
			break;
		case 1:
			openActivity(GoogleMapActivity.class);
			break;
		default:
			break;
		}

	}

	private void openActivity(Class<? extends Activity> ActivityClass) {
		Intent intent = new Intent(this, ActivityClass);
		startActivity(intent);
	}

}
