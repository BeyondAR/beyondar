package com.beyondar.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LayoutToImageConvertor {
	public static String convert(Context context, int layoutID, LayoutType layoutType, String fileName) throws FileNotFoundException{
		
		LayoutInflater inflater = ((SimpleCameraActivity) context).getLayoutInflater();
		View v = inflater.inflate(layoutID, null);
		Bitmap bitmap = getBitmapFromView(v, layoutType);
		bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(Environment.getExternalStorageDirectory()+"/"+fileName));
		return Environment.getExternalStorageDirectory()+"/"+fileName;
	}

	private static Bitmap getBitmapFromView(View v, LayoutType layoutType) {

		switch (layoutType) {
		case LINEAR_LAYOUT:
			v.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			break;
		case RELATIVE_LAYOUT:
			v.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
			break;

		default:
			break;
		}

		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

		Canvas c = new Canvas(b);
		v.draw(c);
		return b;
	}
}


