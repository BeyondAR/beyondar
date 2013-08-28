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
package com.beyondar.android.world.objects;

import com.beyondar.android.opengl.colision.MeshCollider;
import com.beyondar.android.opengl.colision.SquareMeshCollider;
import com.beyondar.android.opengl.renderable.Renderable;
import com.beyondar.android.opengl.renderable.SquareRenderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.math.geom.Point3;

public abstract class BeyondarObject {

	private String mName;
	private long mId;
	private boolean mVisibility;
	private String mBitmapUri;
	private int mTypeList;
	private Float mOrientation;
	private Texture mTexture;

	protected Renderable openGLObject;
	protected Point3 position;
	protected Point3 angle;
	protected boolean faceToCamera;
	protected MeshCollider meshCollider;

	/**
	 * Create an instance of a {@link BeyondarObject} with an unique ID
	 * 
	 * @param id
	 *            Unique ID
	 */
	public BeyondarObject(long id) {
		mId = id;
		position = new Point3();
		angle = new Point3();
		faceToCamera = true;
		mTexture = new Texture();
		mVisibility = true;
	}

	public long getId() {
		return mId;
	}

	public Point3 getAngle() {
		return angle;
	}

	public void setAngle(float x, float y, float z) {
		angle.x = x;
		angle.y = y;
		angle.z = z;
	}

	public Point3 getPosition() {
		return position;
	}

	public void setPosition(Point3 newVect) {
		position = newVect;
	}

	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}

	protected Renderable createRenderable() {
		return SquareRenderable.getInstance();
	}

	public Texture getTexture() {
		return mTexture;
	}

	public void setTexturePointer(int texturePointer) {
		mTexture.setTexturePointer(texturePointer);
	}

	public void setTexture(Texture texture) {
		if (texture == null) {
			texture = new Texture();
		}
		mTexture = texture;
	}

	/**
	 * get the GL Object
	 * 
	 * @return
	 */
	public Renderable getOpenGLObject() {
		if (null == openGLObject) {
			openGLObject = createRenderable();
		}
		return openGLObject;
	}

	public void setOpenGLObject(Renderable openglObject) {
		openGLObject = openglObject;
	}

	public String getBitmapUri() {
		return mBitmapUri;
	}

	public void faceToCamera(boolean faceToCamera) {
		this.faceToCamera = faceToCamera;
	}

	public boolean isFacingToCamera() {
		return faceToCamera;
	}

	/**
	 * Set the visibility of this object. if it is false, the engine will not
	 * show it.
	 * 
	 * @param visibility
	 */
	public void setVisibile(boolean visibility) {
		mVisibility = visibility;
	}

	public boolean isVisible() {
		return mVisibility;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	/**
	 * Set the image uri
	 * 
	 * @param url
	 */
	public void setImageUri(String uri) {
		mBitmapUri = uri;
	}

	public void setImageResource(int resID) {
		// mResID = resID;
		mBitmapUri = BitmapCache.generateUri(resID);
	}

	/**
	 * Set the list type of the this object. This type is configured once the
	 * object is added to the World.
	 * 
	 * @param worldListType
	 */
	public void setWorldListType(int worldListType) {
		mTypeList = worldListType;
	}

	public int getWorldListType() {
		return mTypeList;
	}

	/**
	 * Get the orientation (degrees). Get null if the orientation has not set
	 * 
	 * @return
	 */
	public Float getOrientation() {
		return mOrientation;
	}

	/**
	 * Set the orientation do draw this object. Set null if you want no
	 * orientation (always face to the camera)
	 * 
	 * @param degrees
	 */
	public void setOrientation(Float degrees) {
		this.mOrientation = degrees;
	}

	// TODO: Improve the mesh collider!!
	public MeshCollider getMeshCollider() {
		Point3 topLeft = new Point3(position.x + SquareRenderable.VERTICES[3], position.y
				+ SquareRenderable.VERTICES[4], position.z + SquareRenderable.VERTICES[5]);
		Point3 bottomLeft = new Point3(position.x + SquareRenderable.VERTICES[0], position.y
				+ SquareRenderable.VERTICES[1], position.z + SquareRenderable.VERTICES[2]);
		Point3 bottomRight = new Point3(position.x + SquareRenderable.VERTICES[6], position.y
				+ SquareRenderable.VERTICES[7], position.z + SquareRenderable.VERTICES[8]);
		Point3 topRight = new Point3(position.x + SquareRenderable.VERTICES[9], position.y
				+ SquareRenderable.VERTICES[10], position.z + SquareRenderable.VERTICES[11]);

		// Rotate points
		topLeft.rotatePointDegrees_x(angle.x, position);
		topLeft.rotatePointDegrees_y(angle.y, position);
		topLeft.rotatePointDegrees_z(angle.z, position);

		bottomLeft.rotatePointDegrees_x(angle.x, position);
		bottomLeft.rotatePointDegrees_y(angle.y, position);
		bottomLeft.rotatePointDegrees_z(angle.z, position);

		bottomRight.rotatePointDegrees_x(angle.x, position);
		bottomRight.rotatePointDegrees_y(angle.y, position);
		bottomRight.rotatePointDegrees_z(angle.z, position);

		topRight.rotatePointDegrees_x(angle.x, position);
		topRight.rotatePointDegrees_y(angle.y, position);
		topRight.rotatePointDegrees_z(angle.z, position);

		// Generate the collision detector
		meshCollider = new SquareMeshCollider(topLeft, bottomLeft, bottomRight, topRight);
		return meshCollider;
	}
}
