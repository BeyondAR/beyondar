package com.beyondar.android.opengl.renderer;

import java.util.List;

import com.beyondar.android.world.BeyondarObject;

public interface OnBeyondarObjectRenderedListener {

	public void onBeyondarObjectsRendered(List<BeyondarObject> renderedBeyondarObjects);
}
