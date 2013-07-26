package com.beyondar.android.world.objects;

import com.beyondar.android.opengl.colision.IMeshCollider;
import com.beyondar.android.opengl.colision.SquareCollisionDetector;
import com.beyondar.android.opengl.renderable.IRenderable;
import com.beyondar.android.opengl.renderable.SquareRenderable;
import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.util.cache.BitmapCache;
import com.beyondar.android.util.math.geom.Point3;

public abstract class BeyondarObject {

	private String mName;

	private boolean mVisibility;
	private String mBitmapUri;
	protected IRenderable mOpenGLObject;
	protected Point3 mPosition;
	protected Point3 mAngle;
	private long mId;
	protected boolean mFaceToCamera;
	private int mTypeList;
	private Float mOrientation;
	private Texture mTexture;
	protected IMeshCollider meshCollider;

	/**
	 * Create an instance of a {@link BeyondarObject} with an unique ID
	 * 
	 * @param id
	 *            Unique ID
	 */
	public BeyondarObject(long id) {
		mId = id;
		mPosition = new Point3();
		mAngle = new Point3();
		mFaceToCamera = true;
		mTexture = new Texture();
		mVisibility = true;
	}

	public long getId() {
		return mId;
	}

	public Point3 getAngle() {
		return mAngle;
	}

	public void setAngle(float x, float y, float z) {
		mAngle.x = x;
		mAngle.y = y;
		mAngle.z = z;
	}

	public Point3 getPosition() {
		return mPosition;
	}

	public void setPosition(Point3 newVect) {
		mPosition = newVect;
	}

	public void setPosition(float x, float y, float z) {
		mPosition.x = x;
		mPosition.y = y;
		mPosition.z = z;
	}

	protected IRenderable createRenderable() {
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
	public IRenderable getOpenGLObject() {
		if (null == mOpenGLObject) {
			mOpenGLObject = createRenderable();
		}
		return mOpenGLObject;
	}

	public void setOpenGLObject(IRenderable openglObject) {
		mOpenGLObject = openglObject;
	}

	public String getBitmapUri() {
		return mBitmapUri;
	}

	public void faceToCamera(boolean faceToCamera) {
		mFaceToCamera = faceToCamera;

	}

	public boolean isFacingToCamera() {
		return mFaceToCamera;
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
	public IMeshCollider getMeshCollider() {
		Point3 topLeft = new Point3(mPosition.x + SquareRenderable.VERTICES[3], mPosition.y
				+ SquareRenderable.VERTICES[4], mPosition.z + SquareRenderable.VERTICES[5]);
		Point3 bottomLeft = new Point3(mPosition.x + SquareRenderable.VERTICES[0], mPosition.y
				+ SquareRenderable.VERTICES[1], mPosition.z + SquareRenderable.VERTICES[2]);
		Point3 bottomRight = new Point3(mPosition.x + SquareRenderable.VERTICES[6], mPosition.y
				+ SquareRenderable.VERTICES[7], mPosition.z + SquareRenderable.VERTICES[8]);
		Point3 topRight = new Point3(mPosition.x + SquareRenderable.VERTICES[9], mPosition.y
				+ SquareRenderable.VERTICES[10], mPosition.z + SquareRenderable.VERTICES[11]);

		// Rotate points
		topLeft.rotatePointDegrees_x(mAngle.x, mPosition);
		topLeft.rotatePointDegrees_y(mAngle.y, mPosition);
		topLeft.rotatePointDegrees_z(mAngle.z, mPosition);

		bottomLeft.rotatePointDegrees_x(mAngle.x, mPosition);
		bottomLeft.rotatePointDegrees_y(mAngle.y, mPosition);
		bottomLeft.rotatePointDegrees_z(mAngle.z, mPosition);

		bottomRight.rotatePointDegrees_x(mAngle.x, mPosition);
		bottomRight.rotatePointDegrees_y(mAngle.y, mPosition);
		bottomRight.rotatePointDegrees_z(mAngle.z, mPosition);

		topRight.rotatePointDegrees_x(mAngle.x, mPosition);
		topRight.rotatePointDegrees_y(mAngle.y, mPosition);
		topRight.rotatePointDegrees_z(mAngle.z, mPosition);

		// Generate the collision detector
		meshCollider = new SquareCollisionDetector(topLeft, bottomLeft, bottomRight, topRight);
		return meshCollider;
	}
}
