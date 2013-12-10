package com.beyondar.android.view;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.beyondar.android.world.BeyondarObject;

public abstract class BeyondarViewAdapter {

	private Queue<View> reusedViews;
	private Queue<View> newViews;

	private ViewGroup mParentView;

	public BeyondarViewAdapter(Context mcontext) {
		reusedViews = new LinkedList<View>();
		newViews = new LinkedList<View>();
	}

	void processList(final List<BeyondarObject> list, ViewGroup parent) {
		mParentView = parent;
		
		mParentView.post(new Runnable() {
			
			@Override
			public void run() {
				android.view.ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				for (BeyondarObject beyondarObject : list) {
					
					if (beyondarObject.getScreenPositionCenter().z >1){
						continue;
					}

					View recycledView = reusedViews.poll();

					View view = getView(beyondarObject, recycledView, mParentView);
					if (recycledView != view && recycledView != null) {
						mParentView.removeViewInLayout(recycledView);
					}
					if (view != null) {
						newViews.add(view);
						if (view.getParent() != mParentView){
							mParentView.addView(view, params);
						}
					}
				}

				removeUnusedViews();
				Queue<View> tmp = newViews;
				newViews = reusedViews;
				reusedViews = tmp;
				
			}
		});

	}

	protected void removeUnusedViews() {
		while (!reusedViews.isEmpty()) {
			View view = reusedViews.poll();
			mParentView.removeView(view);
		}
	}

	public abstract View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent);

}
