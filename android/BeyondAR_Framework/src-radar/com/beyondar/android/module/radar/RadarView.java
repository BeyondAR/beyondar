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
/* This code is based on Yasir.Ali <ali.yasir0@gmail.com> work. More on
 *  https://github.com/yasiralijaved/GenRadar
 */
package com.beyondar.android.module.radar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RadarView extends ImageView {

	private Paint mPaint;
	private List<RadarPointModule> mRadarPoints;

	public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RadarView(Context context) {
		super(context);
		init();
	}

	private void init() {
		this.mRadarPoints = new ArrayList<RadarPointModule>();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < mRadarPoints.size(); i++) {
			mPaint.setColor(mRadarPoints.get(i).getColor());
			canvas.drawCircle(mRadarPoints.get(i).getX(), mRadarPoints.get(i).getY(), mRadarPoints.get(i)
					.getRaduis(), mPaint);
		}
	}

	public void updateUIWithNewRadarPoints(List<RadarPointModule> genRadarPoints) {
		this.mRadarPoints = new ArrayList<RadarPointModule>(genRadarPoints);
		this.invalidate();
	}
}
