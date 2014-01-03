package com.beyondar.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LayoutToImageConvertor {
	public static String convert(Context context, View view, LayoutType layoutType, String fileName) throws FileNotFoundException{
		
		
		Bitmap bitmap = getBitmapFromView(view, layoutType);
		bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(Environment.getExternalStorageDirectory()+"/"+fileName));
		return Environment.getExternalStorageDirectory()+"/"+fileName;
	}

	private static Bitmap getBitmapFromView(View v, LayoutType layoutType) {

		switch (layoutType) {
		case LINEAR_LAYOUT:
			v.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
			break;
		case RELATIVE_LAYOUT:
			v.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
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


