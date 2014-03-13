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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;

public class RadarView extends ImageView {

	private Paint mPaint;
	private RadarWorldModule mRadarModule;

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
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawRadarPoints(canvas);
	}

	private void drawRadarPoints(Canvas canvas) {
		if (mRadarModule == null)
			return;
		double maxDistance = mRadarModule.getMaxDistance();
		for (int i = 0; i < mRadarModule.getWorld().getBeyondarObjectLists().size(); i++) {
			BeyondarObjectList list = mRadarModule.getWorld().getBeyondarObjectList(i);
			for (int j = 0; j < list.size(); j++) {
				BeyondarObject beyondarObject = list.get(j);
				RadarPointModule radarPointModule = (RadarPointModule) beyondarObject
						.getFirstModule(RadarPointModule.class);
				if (radarPointModule != null) {
					if (radarPointModule.getGeoObject().getDistanceFromUser() < mRadarModule.getMaxDistance()
							&& radarPointModule.getGeoObject().isVisible()) {
						updateRadarPointPosition(radarPointModule, maxDistance);

						mPaint.setColor(radarPointModule.getColor());

						canvas.drawCircle(radarPointModule.getX(), radarPointModule.getY(),
								radarPointModule.getRaduis(), mPaint);
					}
				}
			}
		}
	}

	private void updateRadarPointPosition(RadarPointModule radarPointModule, double maxDistance) {

		float x = (float) ((getMeasuredWidth() / 2) / maxDistance * radarPointModule.getGeoObject()
				.getPosition().x);
		float y = (float) ((getMeasuredHeight() / 2) / maxDistance * radarPointModule.getGeoObject()
				.getPosition().y);
		x = x + (getMeasuredWidth() / 2);
		y = -y + (getMeasuredHeight() / 2);
		radarPointModule.setX(x);
		radarPointModule.setY(y);

	}

	void setRadarModule(RadarWorldModule radarModule) {
		mRadarModule = radarModule;
	}
}
