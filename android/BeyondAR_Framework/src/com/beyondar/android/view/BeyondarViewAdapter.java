package com.beyondar.android.view;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;

import com.beyondar.android.util.math.geom.Point2;
import com.beyondar.android.world.BeyondarObject;

/**
 * Adapter to attach views to the
 * {@link com.beyondar.android.world.BeyondarObject BeyondarObject}. This is an
 * example of how to use the adapter:
 * 
 * <pre>
 * <code>
 * private class CustomBeyondarViewAdapter extends BeyondarViewAdapter {
 * 
 * 		LayoutInflater inflater;
 * 
 * 		public CustomBeyondarViewAdapter(Context context) {
 * 			super(context);
 * 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 * 		}
 * 
 * 		@Override
 * 		public View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent) {
 * 			if (!showViewOn.contains(beyondarObject)) {
 * 				return null;
 * 			}
 * 			if (recycledView == null) {
 * 				recycledView = inflater.inflate(R.layout.beyondar_object_view, null);
 * 			}
 * 
 * 			TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
 * 			textView.setText(beyondarObject.getName());
 * 			Button button = (Button) recycledView.findViewById(R.id.button);
 * 			button.setOnClickListener(AttachViewToGeoObjectActivity.this);
 * 
 *          // Once the view is ready we specify the position
 * 			setPosition(beyondarObject.getScreenPositionTopRight());
 * 
 * 			return recycledView;
 * 		}
 * 	}
 * </code>
 * </pre>
 * 
 * Then when the adapter is ready we can set it in the
 * {@link com.beyondar.android.fragment.BeyondarFragment BeyondarFragment}:
 * 
 * <code>
 * <pre>
 * CustomBeyondarViewAdapter customBeyondarViewAdapter = new CustomBeyondarViewAdapter(this); 
 * mBeyondarFragment.setBeyondarViewAdapter(customBeyondarViewAdapter);
 * </code> </pre>
 */
public abstract class BeyondarViewAdapter {

	Queue<ViewGroup> mReusedViews;
	Queue<ViewGroup> mNewViews;

	ViewGroup mParentView;
	Point2 mNewPosition;
	Context mContext;

	final LayoutParams mLayoutParams;

	public BeyondarViewAdapter(Context context) {
		mReusedViews = new LinkedList<ViewGroup>();
		mNewViews = new LinkedList<ViewGroup>();
		mContext = context;
		mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
					CustomLayout recycledParent = (CustomLayout) mReusedViews.poll();
					glSurface.fillBeyondarObjectPositions(beyondarObject);
					View toRecycle = null;

					if (recycledParent != null && recycledParent.getChildCount() > 0) {
						toRecycle = recycledParent.getChildAt(0);
					}

					View view = getView(beyondarObject, toRecycle, mParentView);

					boolean added = false;
					// Check if the recyclable view has been used, otherwise add
					// it to the queue to recycle it
					if ((toRecycle != view || view == null) && toRecycle != null) {
						// Store it again to recycle it
						mReusedViews.add(recycledParent);
						added = true;
					}

					// Check if the view has a parent, if not create it
					if (view != null && (recycledParent == null || view.getParent() != recycledParent)) {
						CustomLayout parentLayout = new CustomLayout(mContext);
						parentLayout.addView(view, mLayoutParams);
						if (!added) {
							mReusedViews.add(recycledParent);
						}
						recycledParent = parentLayout;
					}

					if (view != null) {
						mNewViews.add(recycledParent);
						if (recycledParent.getParent() == null) {
							mParentView.addView(recycledParent, mLayoutParams);
						}
						recycledParent.setPosition((int) mNewPosition.x, (int) mNewPosition.y);
					}
				}

				removeUnusedViews();
				Queue<ViewGroup> tmp = mNewViews;
				mNewViews = mReusedViews;
				mReusedViews = tmp;
				mNewPosition = null;
			}
		});
	}

	/**
	 * Get {@link Context}.
	 * 
	 * @return
	 */
	protected Context getContext() {
		return mContext;
	}

	/**
	 * Set the screen position of the view. When the view is created use this
	 * method to specify the position on the screen.
	 * 
	 * 
	 * @param position
	 */
	protected void setPosition(Point2 position) {
		mNewPosition = position;
	}

	private void removeUnusedViews() {
		while (!mReusedViews.isEmpty()) {
			View view = mReusedViews.poll();
			mParentView.removeView(view);
		}
	}

	/**
	 * Override this method to create your own views from the
	 * {@link com.beyondar.android.world.BeyondarObject BeyondarObject}. The
	 * usage of this adapter is very similar to the {@link ListAdapter}.
	 * 
	 * <pre>
	 * <code>
	 * private class CustomBeyondarViewAdapter extends BeyondarViewAdapter {
	 * 
	 * 		LayoutInflater inflater;
	 * 
	 * 		public CustomBeyondarViewAdapter(Context context) {
	 * 			super(context);
	 * 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 * 		}
	 * 
	 * 		@Override
	 * 		public View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent) {
	 * 			if (!showViewOn.contains(beyondarObject)) {
	 * 				return null;
	 * 			}
	 * 			if (recycledView == null) {
	 * 				recycledView = inflater.inflate(R.layout.beyondar_object_view, null);
	 * 			}
	 * 
	 * 			TextView textView = (TextView) recycledView.findViewById(R.id.titleTextView);
	 * 			textView.setText(beyondarObject.getName());
	 * 			Button button = (Button) recycledView.findViewById(R.id.button);
	 * 			button.setOnClickListener(AttachViewToGeoObjectActivity.this);
	 * 
	 *          // Once the view is ready we specify the position
	 * 			setPosition(beyondarObject.getScreenPositionTopRight());
	 * 
	 * 			return recycledView;
	 * 		}
	 * 	}
	 * </code>
	 * </pre>
	 * 
	 * @param beyondarObject
	 * @param recycledView
	 * @param parent
	 * @return
	 */
	public abstract View getView(BeyondarObject beyondarObject, View recycledView, ViewGroup parent);
}
