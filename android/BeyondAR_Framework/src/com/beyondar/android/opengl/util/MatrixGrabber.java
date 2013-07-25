/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.beyondar.android.opengl.util;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class MatrixGrabber {
	public MatrixGrabber() {
		mModelView = new float[16];
		mProjection = new float[16];
	}

	/**
	 * Record the current modelView and projection matrix state. Has the side
	 * effect of setting the current matrix state to GL_MODELVIEW
	 * 
	 * @param gl
	 */
	public void getCurrentState(GL10 gl) {
		getCurrentProjection(gl);
		getCurrentModelView(gl);
	}

	/**
	 * Record the current modelView matrix state. Has the side effect of setting
	 * the current matrix state to GL_MODELVIEW
	 * 
	 * @param gl
	 */
	public void getCurrentModelView(GL10 gl) {
		getMatrix(gl, GL10.GL_MODELVIEW, mModelView);
	}

	/**
	 * Record the current projection matrix state. Has the side effect of
	 * setting the current matrix state to GL_PROJECTION
	 * 
	 * @param gl
	 */
	public void getCurrentProjection(GL10 gl) {
		getMatrix(gl, GL10.GL_PROJECTION, mProjection);
	}

	private void getMatrix(GL10 gl, int mode, float[] mat) {
		MatrixTrackingGL gl2;
		try {
			gl2 = (MatrixTrackingGL) gl;
			IntBuffer matrixMode = IntBuffer.allocate(1);
			gl2.glGetIntegerv(GL11.GL_MATRIX_MODE, matrixMode);
			gl2.glMatrixMode(mode);
			gl2.getMatrix(mat, 0);
			gl2.glMatrixMode(matrixMode.get(0));
		} catch (Exception e) {
			// Log.e(Constants.TAG, "Cast exception!\n " + e.getMessage());
			// e.printStackTrace();
		}

		// MatrixTrackingGL gl2;
		// try{
		// gl2 = (MatrixTrackingGL) gl;
		// gl2.glMatrixMode(mode);
		// gl2.getMatrix(mat, 0);
		// }catch (Exception e) {
		// Log.e(Constants.TAG, "Cast exception!\n " + e.getMessage() );
		// e.printStackTrace();
		// }

	}

	public float[] mModelView;
	public float[] mProjection;
}