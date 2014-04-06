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
package com.beyondar.android.opengl.texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Texture object for rendering using OpenGL.
 * 
 */
public class Texture {

	public static final float TEMPLATE_VERTICES[] = {
			//
			-1.0f, 0.0f, -1.0f, // V1 - bottom left
			-1.0f, 0.0f, 1.0f, // V2 - top left
			1.0f, 0.0f, -1.0f, // V3 - bottom right
			1.0f, 0.0f, 1.0f // V4 - top right
	};

	public final static float TEMPLATE_TEXTURE[] = {
			// Mapping coordinates for the vertices
			0.0f, 1.0f, // top left (V2)
			0.0f, 0.0f, // bottom left (V1)
			1.0f, 1.0f, // top right (V4)
			1.0f, 0.0f // bottom right (V3)
	};

	// buffer holding the texture coordinates
	private FloatBuffer mTextureBuffer;
	// buffer holding the vertices
	private FloatBuffer mVertexBuffer;

	private int mWidth, mHeight;
	private float mWidthRate, mHeightRate;
	private int mTexture;
	private boolean mIsLoaded;
	private double mTimeStamp;
	private int mCounterLoaded;
	private float[] mTextureMap;
	private float[] mVertices;

	/**
	 * Constructor of a texture with a defined texture reference.
	 * 
	 * @param textureReference
	 *            Loaded texture reference.
	 */
	public Texture(int textureReference) {
		setTexturePointer(textureReference);

		mVertices = new float[TEMPLATE_VERTICES.length];
		System.arraycopy(TEMPLATE_VERTICES, 0, mVertices, 0, TEMPLATE_VERTICES.length);

		mTextureMap = new float[TEMPLATE_TEXTURE.length];
		System.arraycopy(TEMPLATE_TEXTURE, 0, mTextureMap, 0, TEMPLATE_TEXTURE.length);
	}

	/**
	 * Constructor of a texture with a no texture reference.
	 */
	public Texture() {
		this(0);
	}

	/**
	 * Set the image size in pixels. This method is used to support non power of
	 * two images.
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public Texture setImageSize(int width, int height) {
		mWidth = width;
		mHeight = height;
		calculateImageSizeRate();
		return this;
	}

	private void calculateImageSizeRate() {
		if (mWidth < mHeight) {
			mWidthRate = ((float) mWidth / (float) mHeight);
			mHeightRate = 1;
		} else {
			mHeightRate = ((float) mHeight / (float) mWidth);
			mWidthRate = 1;
		}

		for (int i = 0; i < mVertices.length; i++) {
			if ((i + 1) % 3 == 0) {
				mVertices[i] = mVertices[i] * mHeightRate;
			} else {
				mVertices[i] = mVertices[i] * mWidthRate;
			}
		}

		// a float has 4 bytes so we allocate for each coordinate 4 bytes
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		// allocates the memory from the byte buffer
		mVertexBuffer = byteBuffer.asFloatBuffer();
		// fill the vertexBuffer with the vertices
		mVertexBuffer.put(mVertices);
		// set the cursor position to the beginning of the buffer
		mVertexBuffer.position(0);

		ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(mTextureMap.length * 4);
		byteBuffer2.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuffer2.asFloatBuffer();
		mTextureBuffer.put(mTextureMap);
		mTextureBuffer.position(0);

	}

	public FloatBuffer getTextureBuffer() {
		return mTextureBuffer;
	}

	public FloatBuffer getVerticesBuffer() {
		return mVertexBuffer;
	}

	public float[] getVertices() {
		return mVertices;
	}

	public float[] getTextureMap() {
		return mTextureMap;
	}

	public float getWithRate() {
		return mWidthRate;
	}

	public float getHeightRate() {
		return mHeightRate;
	}

	public int getImageWidth() {
		return mWidth;
	}

	public int getImageHeight() {
		return mHeight;
	}

	public int getTexturePointer() {
		return mTexture;
	}

	public Texture setTexturePointer(int texture) {
		mTexture = texture;
		if (mTexture == 0) {
			mIsLoaded = false;
		} else {
			mIsLoaded = true;
		}
		mCounterLoaded = 0;
		return this;
	}

	public Texture setLoaded(boolean isLoaded) {
		mIsLoaded = isLoaded;
		return this;
	}

	public boolean isLoaded() {
		return mIsLoaded;
	}

	public Texture setTimeStamp(double time) {
		mTimeStamp = time;
		return this;
	}

	public double getTimeStamp() {
		return mTimeStamp;
	}

	public Texture setLoadTryCounter(int counter) {
		mCounterLoaded = counter;
		return this;
	}

	public int getLoadTryCounter() {
		return mCounterLoaded;
	}

	public Texture clone() {
		Texture clone = new Texture();
		return clone.setLoaded(isLoaded()).setTexturePointer(getTexturePointer())
				.setTimeStamp(getTimeStamp()).setLoadTryCounter(getLoadTryCounter())
				.setImageSize(mWidth, mHeight);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("loaded: ");
		sb.append(isLoaded());
		sb.append("    pointer: ");
		sb.append(getTexturePointer());
		sb.append("    timestamp: ");
		sb.append(getTimeStamp());
		return sb.toString();
	}

}
