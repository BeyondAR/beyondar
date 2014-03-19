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
import java.util.Iterator;

import com.beyondar.android.opengl.texture.Texture;

/**
 * This class allow the user to store according to type, so it is useful when a
 * search is required
 * 
 */
public class BeyondarObjectList implements Iterable<BeyondarObject> {

	private int mType;

	private Texture mTexture;

	private boolean mVisibility;

	private ArrayList<BeyondarObject> mContainer;
	private ArrayList<BeyondarObject> mToRemoveQueue;

	private String mDefaultBitmapURI;

	private World mWorld;

	private Object mLock;

	BeyondarObjectList(int type, World world) {
		mType = type;
		mVisibility = true;
		mContainer = new ArrayList<BeyondarObject>();
		mToRemoveQueue = new ArrayList<BeyondarObject>();
		mWorld = world;
		mTexture = new Texture();
		mLock = new Object();
	}

	public boolean isVisible() {
		return mVisibility;
	}

	/**
	 * Set the visibility of all this GeoObjects
	 * 
	 * @param visibility
	 */
	public void setVisibility(boolean visibility) {
		mVisibility = visibility;
	}

	/**
	 * Add a geoObject to the list if it does not exist. To add an object use the {@link World} instance
	 * 
	 * @param object
	 *            the object to add
	 * @return True if the object has been added (it does not exist), false
	 *         otherwise
	 */
	boolean add(BeyondarObject object) {
		if (!mContainer.contains(object)) {
			mContainer.add(object);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the element ate the specific position
	 * 
	 * @param index
	 * @return
	 */
	public BeyondarObject get(int index) {
		return mContainer.get(index);
	}

	/**
	 * Return the amount of elements in the list
	 * 
	 * @return
	 */
	public int size() {
		return mContainer.size();
	}

	/**
	 * This method will place the object in a queue to be removed and hide it.
	 * To force the queue to process all the object in the removal queue call
	 * {@link BeyondarObjectList#forceRemoveObjectsInQueue()}.
	 * 
	 * @param object
	 */
	synchronized void remove(BeyondarObject object) {
		synchronized (mLock) {
			mToRemoveQueue.add(object);
			object.setVisible(false);
		}
	}

	/**
	 * This method will place the object in a queue to be removed and hide it.
	 * To force the queue to process all the object in the removal queue call
	 * {@link BeyondarObjectList#forceRemoveObjectsInQueue()}.
	 * 
	 * @param index
	 */
	synchronized void remove(int index) {
		remove(mContainer.get(index));
	}

	/**
	 * Get the type of this list
	 * 
	 * @return
	 */
	public int getType() {
		return mType;
	}

	public void setDefaultBitmapURI(String uri) {
		mDefaultBitmapURI = uri;
		mTexture = new Texture();
	}

	/**
	 * Get the default bitmap. <br>
	 * DO not use this method directly form the {@link BeyondarObjectList} instance, the best way to get the default image is using the {@link World} instance:<br>
	 * {@link World#getDefaultBitmap(int)}
	 * 
	 * @return
	 */
	public String getDefaultBitmapURI() {
		if (mDefaultBitmapURI != null) {

			return mDefaultBitmapURI;
		} else {
			return mWorld.getDefaultBitmap();
		}
	}

	public void setTexture(Texture texture) {
		if (texture == null) {
			texture = new Texture();
		}
		mTexture = texture;
	}

	/**
	 * Get the texture object for this list
	 * 
	 * @return
	 */
	public Texture getTexture() {
		return mTexture;
	}

	/**
	 * This method force to remove all the elements that are queued to be
	 * removed
	 */
	public synchronized void forceRemoveObjectsInQueue() {
		synchronized (mLock) {
			for (int i = 0; i < mToRemoveQueue.size(); i++) {
				mContainer.remove(mToRemoveQueue.get(i));
			}
			mToRemoveQueue.clear();
		}
	}

	@Override
	public Iterator<BeyondarObject> iterator() {
		return mContainer.iterator();
	}

}
