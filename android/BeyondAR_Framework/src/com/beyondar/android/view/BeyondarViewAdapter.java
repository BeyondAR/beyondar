package com.beyondar.android.view;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.beyondar.android.util.math.geom.Point2;
import com.beyondar.android.world.BeyondarObject;

public abstract class BeyondarViewAdapter {

	private Queue<View> mReusedViews;
	private Queue<View> mNewViews;

	private ViewGroup mParentView;

	private Point2 mNewPosition;

	// private Context mContext;

	public BeyondarViewAdapter(Context context) {
		mReusedViews = new LinkedList<View>();
		mNewViews = new LinkedList<View>();
		// mContext = context;
	}

	void processList(final List<BeyondarObject> list, ViewGroup parent, final BeyondarGLSurfaceView glSurface) {
		mParentView = parent;

		mParentView.post(new Runnable() {

			@Override
			public void run() {
				for (BeyondarObject beyondarObject : list) {

					if (beyondarObject.getScreenPositionCenter().z > 1) {
						continue;
					}

					View recycledView = mReusedViews.poll();

					glSurface.fillBeyondarObjectPositions(beyondarObject);

					View view = getView(beyondarObject, recycledView, mParentView);

					if (recycledView != view && recycledView != null) {
						// Store it again to recycle it
						mReusedViews.add(recycledView);
					}

					if (view != null) {
						mNewViews.add(view);

						if (view.getParent() == null) {
							android.widget.RelativeLayout.LayoutParams paramsWrap = new android.widget.RelativeLayout.LayoutParams(
									ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

							mParentView.addView(view, paramsWrap);
						}
						if (mNewPosition != null) {
							if (Build.VERSION.SDK_INT >= 11) {
								view.setTranslationX(mNewPosition.x);
								view.setTranslationY(mNewPosition.y);
							} else {
								android.widget.RelativeLayout.LayoutParams existingParams = (android.widget.RelativeLayout.LayoutParams) view
										.getLayoutParams();
								existingParams.leftMargin = (int) mNewPosition.x;
								existingParams.topMargin = (int) mNewPosition.y;
							}
						}
					}
				}

				removeUnusedViews();
				Queue<View> tmp = mNewViews;
				mNewViews = mReusedViews;
				mReusedViews = tmp;
				mNewPosition = null;
			}
		});

	}

	protected void setPosition(Point2 position) {
		mNewPosition = position;
	}

	protected void removeUnusedViews() {
		while (!mReusedViews.isEmpty()) {
			View view = mReusedViews.poll();
			mParentView.removeView(view);
		}
	}

	public abstract View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent);
}
