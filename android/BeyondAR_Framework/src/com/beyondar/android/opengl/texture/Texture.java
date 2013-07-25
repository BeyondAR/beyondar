package com.beyondar.android.opengl.texture;

public class Texture {

	private int mTexture;
	private boolean mIsLoaded;
	private double mTimeStamp;

	public Texture(int textureReference) {
		mTexture = textureReference;
		mIsLoaded = true;
	}

	public Texture() {
		mIsLoaded = false;
	}

	public int getTexturePointer() {
		return mTexture;
	}

	public void setTexturePointer(int texture) {
		mTexture = texture;
		mIsLoaded = true;
	}

	public void setLoaded(boolean isLoaded) {
		mIsLoaded = isLoaded;
	}

	public boolean isLoaded() {
		return mIsLoaded;
	}

	public void setTimeStamp(double time) {
		mTimeStamp = time;
	}

	public double getTimeStamp() {
		return mTimeStamp;
	}

	public Texture clone() {
		Texture clone = new Texture();
		clone.setLoaded(isLoaded());
		clone.setTexturePointer(getTexturePointer());
		clone.setTimeStamp(getTimeStamp());
		return clone;
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
