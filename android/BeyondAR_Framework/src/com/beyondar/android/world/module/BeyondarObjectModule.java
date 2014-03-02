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
package com.beyondar.android.world.module;

import com.beyondar.android.opengl.renderable.Renderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.world.BeyondarObject;

public interface BeyondarObjectModule extends BeyondarModule {

	/** This method is called when the */
	public void setup(BeyondarObject beyondarObject);

	public void onAngleChanged(Point3 angle);

	public void onPositionChanged(Point3 position);

	public void onTextureChanged(Texture texture);

	public void onRenderableChanged(Renderable openglObject);

	public void onFaceToCameraChanged(boolean faceToCamera);

	public void onVisibilityChanged(boolean visible);

	public void onNameChanged(String name);

	public void onImageUriChanged(String uri);
}
