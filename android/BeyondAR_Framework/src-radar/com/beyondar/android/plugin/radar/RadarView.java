/*
 * Copyright (C) 2014 BeyondAR
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
package com.beyondar.android.plugin.radar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.BeyondarObjectList;

public class RadarView extends ImageView {

	private Paint mPaint;
	private RadarWorldPlugin mRadarPlugin;

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
		if (mRadarPlugin == null)
			return;
		double maxDistance = mRadarPlugin.getMaxDistance();
		for (int i = 0; i < mRadarPlugin.getWorld().getBeyondarObjectLists().size(); i++) {
			BeyondarObjectList list = mRadarPlugin.getWorld().getBeyondarObjectList(i);
			for (int j = 0; j < list.size(); j++) {
				BeyondarObject beyondarObject = list.get(j);
				RadarPointPlugin radarPointPlugin = (RadarPointPlugin) beyondarObject
						.getFirstPlugin(RadarPointPlugin.class);
				if (radarPointPlugin != null) {
					if (radarPointPlugin.getGeoObject().getDistanceFromUser() < mRadarPlugin.getMaxDistance()
							&& radarPointPlugin.getGeoObject().isVisible()) {
						updateRadarPointPosition(radarPointPlugin, maxDistance);

						mPaint.setColor(radarPointPlugin.getColor());

						if (radarPointPlugin.getRaduisPixels() == -1) {
							radarPointPlugin.setRaduisPixels(dpToPixels(radarPointPlugin.getRaduis()));
						}
						canvas.drawCircle(radarPointPlugin.getX(), radarPointPlugin.getY(),
								radarPointPlugin.getRaduisPixels(), mPaint);
					}
				}
			}
		}
	}

	private void updateRadarPointPosition(RadarPointPlugin radarPointPlugin, double maxDistance) {

		float x = (float) ((getMeasuredWidth() / 2) / maxDistance * radarPointPlugin.getGeoObject()
				.getPosition().x);
		float y = (float) ((getMeasuredHeight() / 2) / maxDistance * radarPointPlugin.getGeoObject()
				.getPosition().y);
		x = x + (getMeasuredWidth() / 2);
		y = -y + (getMeasuredHeight() / 2);
		radarPointPlugin.setX(x);
		radarPointPlugin.setY(y);

	}

	void setRadarPlugin(RadarWorldPlugin radarPlugin) {
		mRadarPlugin = radarPlugin;
	}

	private float dpToPixels(float dp) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
		return px;
	}
}
