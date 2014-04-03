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
package com.beyondar.android.world;

import java.util.ArrayList;
import java.util.List;

import com.beyondar.android.opengl.colision.MeshCollider;
import com.beyondar.android.opengl.colision.SquareMeshCollider;
import com.beyondar.android.opengl.renderable.Renderable;
import com.beyondar.android.opengl.renderable.SquareRenderable;
import com.beyondar.android.opengl.renderer.ARRenderer;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.plugin.BeyondarObjectPlugin;
import com.beyondar.android.plugin.Plugable;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.math.geom.Point3;

public class BeyondarObject implements Plugable<BeyondarObjectPlugin> {

	private Long mId;
	private int mTypeList;

	protected Texture texture;
	protected String bitmapUri;
	protected String name;
	protected boolean visible;
	protected Renderable renderable;
	protected Point3 position;
	protected Point3 angle;
	protected boolean faceToCamera;
	protected MeshCollider meshCollider;
	protected double distanceFromUser;
	/**
	 * This pointer is made to track the
	 * {@link com.beyondar.android.world.BeyondarObject BeyondarObject} position
	 * on the screen
	 */
	protected Point3 screenPositionTopLeft, screenPositionTopRight, screenPositionBottomLeft,
			screenPositionBottomRight, screenPositionCenter;

	protected Point3 topLeft, bottomLeft, bottomRight, topRight;

	/** This fields contains all the loaded plugins */
	protected List<BeyondarObjectPlugin> plugins;
	/** Use this lock to access the plugins field */
	protected Object lockPlugins = new Object();

	/**
	 * Create an instance of a {@link BeyondarObject} with an unique ID
	 * 
	 * @param id
	 *            Unique ID
	 */
	public BeyondarObject(long id) {
		mId = id;
		init();
	}

	public BeyondarObject() {
		init();
	}

	private void init() {
		plugins = new ArrayList<BeyondarObjectPlugin>(DEFAULT_PLUGINS_CAPACITY);
		position = new Point3();
		angle = new Point3();
		texture = new Texture();
		faceToCamera(true);
		setVisible(true);

		topLeft = new Point3();
		bottomLeft = new Point3();
		bottomRight = new Point3();
		topRight = new Point3();

		screenPositionTopLeft = new Point3();
		screenPositionTopRight = new Point3();
		screenPositionBottomLeft = new Point3();
		screenPositionBottomRight = new Point3();
		screenPositionCenter = new Point3();
	}

	public long getId() {
		if (mId == null) {
			mId = (long) hashCode();
		}
		return mId.longValue();
	}

	public void addPlugin(BeyondarObjectPlugin plugin) {
		synchronized (lockPlugins) {
			if (plugins.contains(plugin)) {
				return;
			}
			plugins.add(plugin);
		}
	}

	@Override
	public boolean removePlugin(BeyondarObjectPlugin plugin) {
		boolean removed = false;
		synchronized (lockPlugins) {
			removed = plugins.remove(plugin);
		}
		if (removed) {
			plugin.onDetached();
		}
		return removed;
	}

	@Override
	public void removeAllPlugins() {
		synchronized (lockPlugins) {
			plugins.clear();
		}
	}

	@Override
	public BeyondarObjectPlugin getFirstPlugin(Class<? extends BeyondarObjectPlugin> pluginClass) {
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					return plugin;
				}
			}
		}
		return null;
	}

	@Override
	public boolean containsAnyPlugin(Class<? extends BeyondarObjectPlugin> pluginClass) {
		return getFirstPlugin(pluginClass) != null;
	}

	@Override
	public boolean containsPlugin(BeyondarObjectPlugin plugin) {
		synchronized (lockPlugins) {
			return plugins.contains(plugin);
		}
	}

	@Override
	public List<BeyondarObjectPlugin> getAllPugins(Class<? extends BeyondarObjectPlugin> pluginClass) {
		ArrayList<BeyondarObjectPlugin> result = new ArrayList<BeyondarObjectPlugin>(5);
		return getAllPlugins(pluginClass, result);
	}

	@Override
	public List<BeyondarObjectPlugin> getAllPlugins(Class<? extends BeyondarObjectPlugin> pluginClass,
			List<BeyondarObjectPlugin> result) {
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				if (pluginClass.isInstance(plugin)) {
					result.add(plugin);
				}
			}
		}
		return result;
	}

	/**
	 * Get a {@link List} copy of the added plugins. Adding/removing plugins
	 * to this list will not affect the added plugins
	 * 
	 * @return
	 */
	@Override
	public List<BeyondarObjectPlugin> getAllPlugins() {
		synchronized (lockPlugins) {
			return new ArrayList<BeyondarObjectPlugin>(plugins);
		}
	}

	void onRemoved() {
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onDetached();
			}
		}
	}

	public Point3 getAngle() {
		return angle;
	}

	public void setAngle(float x, float y, float z) {
		if (angle.x == x && angle.y == y && angle.z == z)
			return;
		angle.x = x;
		angle.y = y;
		angle.z = z;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onAngleChanged(angle);
			}
		}
	}

	public Point3 getPosition() {
		return position;
	}

	public void setPosition(Point3 newVect) {
		if (newVect == position) return;
		position = newVect;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onPositionChanged(position);
			}
		}
	}

	public void setPosition(float x, float y, float z) {
		if (position.x == x && position.y == y && position.z == z)
			return;
		position.x = x;
		position.y = y;
		position.z = z;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onPositionChanged(position);
			}
		}
	}

	protected Renderable createRenderable() {
		return SquareRenderable.getInstance();
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexturePointer(int texturePointer) {
		if (texturePointer == texture.getTexturePointer())
			return;
		texture.setTexturePointer(texturePointer);
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onTextureChanged(texture);
			}
		}
	}

	public void setTexture(Texture texture) {
		if (texture == this.texture) {
			return;
		}
		if (texture == null) {
			texture = new Texture();
		}
		this.texture = texture;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onTextureChanged(this.texture);
			}
		}
	}

	/**
	 * get the GL Object
	 * 
	 * @return
	 */
	public Renderable getOpenGLObject() {
		if (null == renderable) {
			renderable = createRenderable();
		}
		return renderable;
	}

	public void setRenderable(Renderable renderable) {
		if (renderable == this.renderable)
			return;
		this.renderable = renderable;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onRenderableChanged(this.renderable);
			}
		}
	}

	public String getBitmapUri() {
		return bitmapUri;
	}

	public void faceToCamera(boolean faceToCamera) {
		if (faceToCamera == this.faceToCamera)
			return;
		this.faceToCamera = faceToCamera;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onFaceToCameraChanged(this.faceToCamera);
			}
		}
	}

	public boolean isFacingToCamera() {
		return faceToCamera;
	}

	/**
	 * Set the visibility of this object. if it is false, the engine will not
	 * show it.
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		if (visible == this.visible)
			return;
		this.visible = visible;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onVisibilityChanged(this.visible);
			}
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setName(String name) {
		if (name == this.name)
			return;
		this.name = name;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onNameChanged(this.name);
			}
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * Set the image uri
	 * 
	 * @param uri
	 */
	public void setImageUri(String uri) {
		if (uri == bitmapUri)
			return;
		bitmapUri = uri;
		synchronized (lockPlugins) {
			for (BeyondarObjectPlugin plugin : plugins) {
				plugin.onImageUriChanged(bitmapUri);
			}
		}
		setTexture(null);
	}

	public void setImageResource(int resID) {
		setImageUri(BitmapCache.generateUri(resID));
	}

	/**
	 * Set the list type of the this object. This type is configured once the
	 * object is added to the World.
	 * 
	 * @param worldListType
	 */
	void setWorldListType(int worldListType) {
		mTypeList = worldListType;
	}

	public int getWorldListType() {
		return mTypeList;
	}

	/**
	 * Get the Distance from the user in meters
	 * 
	 * @return Distance in meters
	 */
	public double getDistanceFromUser() {
		return distanceFromUser;
	}

	/**
	 * Set how far is the object from the user (meters).
	 * 
	 * This method is used by the {@link ARRenderer} to set this value.
	 * 
	 * @param distance
	 */
	public void setDistanceFromUser(double distance) {
		distanceFromUser = distance;
	}

	public Point3 getScreenPositionBottomLeft() {
		return screenPositionBottomLeft;
	}

	public Point3 getScreenPositionTopLeft() {
		return screenPositionTopLeft;
	}

	public Point3 getScreenPositionTopRight() {
		return screenPositionTopRight;
	}

	public Point3 getScreenPositionBottomRight() {
		return screenPositionBottomRight;
	}

	public Point3 getScreenPositionCenter() {
		return screenPositionCenter;
	}

	public Point3 getTopLeft() {
		topLeft.x = position.x + texture.getVertices()[3];
		topLeft.y = position.y + texture.getVertices()[4];
		topLeft.z = position.z + texture.getVertices()[5];

		topLeft.rotatePointDegrees_x(angle.x, position);
		topLeft.rotatePointDegrees_y(angle.y, position);
		topLeft.rotatePointDegrees_z(angle.z, position);
		return topLeft;
	}

	public Point3 getBottomLeft() {
		bottomLeft.x = position.x + texture.getVertices()[0];
		bottomLeft.y = position.y + texture.getVertices()[1];
		bottomLeft.z = position.z + texture.getVertices()[2];

		bottomLeft.rotatePointDegrees_x(angle.x, position);
		bottomLeft.rotatePointDegrees_y(angle.y, position);
		bottomLeft.rotatePointDegrees_z(angle.z, position);
		return bottomLeft;
	}

	public Point3 getBottomRight() {
		bottomRight.x = position.x + texture.getVertices()[6];
		bottomRight.y = position.y + texture.getVertices()[7];
		bottomRight.z = position.z + texture.getVertices()[8];

		bottomRight.rotatePointDegrees_x(angle.x, position);
		bottomRight.rotatePointDegrees_y(angle.y, position);
		bottomRight.rotatePointDegrees_z(angle.z, position);
		return bottomRight;
	}

	public Point3 getTopRight() {
		topRight.x = position.x + texture.getVertices()[9];
		topRight.y = position.y + texture.getVertices()[10];
		topRight.z = position.z + texture.getVertices()[11];

		topRight.rotatePointDegrees_x(angle.x, position);
		topRight.rotatePointDegrees_y(angle.y, position);
		topRight.rotatePointDegrees_z(angle.z, position);
		return topRight;
	}

	// TODO: Improve the mesh collider!!
	public MeshCollider getMeshCollider() {
		Point3 topLeft = getTopLeft();
		Point3 bottomLeft = getBottomLeft();
		Point3 bottomRight = getBottomRight();
		Point3 topRight = getTopRight();

		// Generate the collision detector
		meshCollider = new SquareMeshCollider(topLeft, bottomLeft, bottomRight, topRight);
		return meshCollider;
	}
}
