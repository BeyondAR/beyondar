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
package com.beyondar.android.util.math.geom;

/**
 * Represent a 3D Matrix that can be used with OpenGL. It has functions to
 * rotate, scale and translate
 * 
 */
public class Matrix3 {
	/**
	 * Dimension for the matrix array
	 */
	private static final int DIMENSION = 16;

	private float m[];

	/**
	 * Constructs a matrix with the identity
	 * 
	 */
	public Matrix3() {
		m = new float[DIMENSION];
		loadIdentity(m);
	}

	/**
	 * Returns the float's array representing the matrix in the form:
	 * <p>
	 * {@code [ m0 m4 m8  m12 ]}
	 * </p>
	 * <p>
	 * {@code [ m1 m5 m9  m13 ]}
	 * </p>
	 * <p>
	 * {@code [ m2 m6 m10 m14 ]}
	 * </p>
	 * <p>
	 * {@code [ m3 m7 m11 m15 ]}
	 * </p>
	 * 
	 * @return the float's array representing the matrix
	 */
	public float[] getMatrix() {
		return m;
	}

	/**
	 * Sets the identity for this matrix
	 */
	public void setIdentity() {
		loadIdentity(m);
	}

	/**
	 * Loads the identity matrix in an array
	 * 
	 * @param m
	 *            the array
	 */
	private void loadIdentity(float m[]) {
		for (int i = 0; i < DIMENSION; i++) {
			m[i] = 0;
		}
		m[0] = m[5] = m[10] = m[15] = 1;
	}

	/**
	 * Translates with the given parameters
	 * 
	 * @param x
	 *            translation in x axis
	 * @param y
	 *            translation in y axis
	 * @param z
	 *            translation in z axis
	 */
	public void translate(float x, float y, float z) {
		float mt[] = new float[DIMENSION];
		loadIdentity(mt);
		mt[12] = x;
		mt[13] = y;
		mt[14] = z;
		postMultiply(mt);
	}

	/**
	 * Multiplies m1*m2, and deposit the result at mresult
	 * 
	 * @param m1
	 *            first matrix
	 * @param m2
	 *            second matrix
	 * @param mresult
	 *            result matrix
	 */
	private void multiplyMatrix(float m1[], float m2[], float mresult[]) {
		float matrix[] = new float[DIMENSION];
		int row = 0, column = 0;

		for (int i = 0; i < 16; i++) {
			row = i % 4;
			column = i / 4;
			matrix[i] = 0;
			for (int j = 0; j < 4; j++) {
				matrix[i] += m1[row + j * 4] * m2[column * 4 + j];
			}
		}

		for (int i = 0; i < 16; i++)
			mresult[i] = matrix[i];
	}

	/**
	 * Postmultiplies the current matrix with the given one
	 * 
	 * @param matrix
	 *            matrix to postmultiply
	 */
	public void postMultiply(float matrix[]) {
		multiplyMatrix(m, matrix, m);
	}

	/**
	 * Premultiplies the current matrix with the given one
	 * 
	 * @param matrix
	 *            matrix to premultiply
	 */
	public void pareMultiply(float matrix[]) {
		multiplyMatrix(matrix, m, m);
	}

	/**
	 * Scales the matrix with the given factors
	 * 
	 * @param x
	 *            scale in x axis
	 * @param y
	 *            scale in y axis
	 * @param z
	 *            scale in z axis
	 */
	public void scale(float x, float y, float z) {
		float matrix[] = new float[DIMENSION];
		loadIdentity(matrix);
		matrix[0] = x;
		matrix[5] = y;
		matrix[10] = z;
		postMultiply(matrix);
	}

	/**
	 * Rotates the matrix with the given rotations (in radians)
	 * 
	 * @param x
	 *            rotation in x axis
	 * @param y
	 *            rotation in y axis
	 * @param z
	 *            rotation in z axis
	 */
	public void rotate(float x, float y, float z) {
		float mx[] = new float[DIMENSION];
		float my[] = new float[DIMENSION];
		float mz[] = new float[DIMENSION];
		float mi[] = new float[DIMENSION];
		float mr[] = new float[DIMENSION];
		initMatrixRotationX(mx, x);
		initRotationMatrixY(my, y);
		initMatrixRotationZ(mz, z);

		multiplyMatrix(mx, my, mi);
		multiplyMatrix(mi, mz, mr);
		postMultiply(mr);
	}

	/**
	 * Creates in m the matrix for the given rotation in x
	 * 
	 * @param m
	 *            where the result will be stored
	 * @param radians
	 *            radians to rotate
	 */
	private void initMatrixRotationX(float m[], float radians) {
		loadIdentity(m);
		float cosR = (float) Math.cos(radians);
		float sinR = (float) Math.sin(radians);
		m[5] = m[10] = cosR;
		m[9] = -sinR;
		m[6] = sinR;
	}

	/**
	 * Creates in m the matrix for the given rotation in y
	 * 
	 * @param m
	 *            where the result will be stored
	 * @param radians
	 *            radians to rotate
	 */
	private void initRotationMatrixY(float m[], float radians) {
		loadIdentity(m);
		float cosR = (float) Math.cos(radians);
		float sinR = (float) Math.sin(radians);
		m[0] = m[10] = cosR;
		m[2] = -sinR;
		m[8] = sinR;
	}

	/**
	 * Creates in m the matrix for the given rotation in z
	 * 
	 * @param m
	 *            where the result will be stored
	 * @param radians
	 *            radians to rotate
	 */
	private void initMatrixRotationZ(float m[], float radians) {
		loadIdentity(m);
		float cosR = (float) Math.cos(radians);
		float sinR = (float) Math.sin(radians);
		m[0] = m[5] = cosR;
		m[4] = -sinR;
		m[1] = sinR;
	}

	public void set(float[] matrix) {
		if ( matrix != null && matrix.length == 16 ){
			m = matrix;
		}
	}
	
	public void transform( Point3 p ){
		float x = m[0] * p.x + m[4]* p.y + m[8]*p.z + m[12];
		float y = m[1] * p.x + m[5]* p.y + m[9]*p.z + m[13];
		float z = m[2] * p.x + m[6]* p.y + m[10]*p.z + m[14];
		p.set(x, y, z);
	}

}
