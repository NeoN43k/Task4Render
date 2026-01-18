package com.cgvsu.math;

import java.util.Arrays;

public class Matrix4f {
    private final float[][] matrix;

    public Matrix4f() {
        matrix = new float[4][4];
        setIdentity();
    }

    public Matrix4f(float[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, matrix[i], 0, 4);
        }
    }

    public static Matrix4f identity() {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        return result;
    }

    public void setIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
    }

    public Vector4f multiply(Vector4f vector) {
        float x = matrix[0][0] * vector.x + matrix[0][1] * vector.y + matrix[0][2] * vector.z + matrix[0][3] * vector.w;
        float y = matrix[1][0] * vector.x + matrix[1][1] * vector.y + matrix[1][2] * vector.z + matrix[1][3] * vector.w;
        float z = matrix[2][0] * vector.x + matrix[2][1] * vector.y + matrix[2][2] * vector.z + matrix[2][3] * vector.w;
        float w = matrix[3][0] * vector.x + matrix[3][1] * vector.y + matrix[3][2] * vector.z + matrix[3][3] * vector.w;
        return new Vector4f(x, y, z, w);
    }

    public javax.vecmath.Vector4f multiply(javax.vecmath.Vector4f vector) {
        float x = matrix[0][0] * vector.x + matrix[0][1] * vector.y + matrix[0][2] * vector.z + matrix[0][3] * vector.w;
        float y = matrix[1][0] * vector.x + matrix[1][1] * vector.y + matrix[1][2] * vector.z + matrix[1][3] * vector.w;
        float z = matrix[2][0] * vector.x + matrix[2][1] * vector.y + matrix[2][2] * vector.z + matrix[2][3] * vector.w;
        float w = matrix[3][0] * vector.x + matrix[3][1] * vector.y + matrix[3][2] * vector.z + matrix[3][3] * vector.w;
        return new javax.vecmath.Vector4f(x, y, z, w);
    }

    public Vector4f multiplyLeft(Vector4f vector) {
        float x = vector.x * matrix[0][0] + vector.y * matrix[1][0] + vector.z * matrix[2][0] + vector.w * matrix[3][0];
        float y = vector.x * matrix[0][1] + vector.y * matrix[1][1] + vector.z * matrix[2][1] + vector.w * matrix[3][1];
        float z = vector.x * matrix[0][2] + vector.y * matrix[1][2] + vector.z * matrix[2][2] + vector.w * matrix[3][2];
        float w = vector.x * matrix[0][3] + vector.y * matrix[1][3] + vector.z * matrix[2][3] + vector.w * matrix[3][3];
        return new Vector4f(x, y, z, w);
    }

    public Matrix4f multiply(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += matrix[i][k] * other.matrix[k][j];
                }
                result[i][j] = sum;
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f transpose() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = matrix[j][i];
            }
        }
        return new Matrix4f(result);
    }

    public float get(int row, int col) {
        return matrix[row][col];
    }

    public void set(int row, int col, float value) {
        matrix[row][col] = value;
    }

    public float[][] getMatrix() {
        float[][] copy = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, 4);
        }
        return copy;
    }

    public static Matrix4f translate(float x, float y, float z) {
        Matrix4f result = identity();
        result.set(0, 3, x);
        result.set(1, 3, y);
        result.set(2, 3, z);
        return result;
    }

    public static Matrix4f scale(float x, float y, float z) {
        Matrix4f result = identity();
        result.set(0, 0, x);
        result.set(1, 1, y);
        result.set(2, 2, z);
        return result;
    }

    public static Matrix4f rotateX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        Matrix4f result = identity();
        result.set(1, 1, cos);
        result.set(1, 2, -sin);
        result.set(2, 1, sin);
        result.set(2, 2, cos);
        return result;
    }

    public static Matrix4f rotateY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        Matrix4f result = identity();
        result.set(0, 0, cos);
        result.set(0, 2, sin);
        result.set(2, 0, -sin);
        result.set(2, 2, cos);
        return result;
    }

    public static Matrix4f rotateZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        Matrix4f result = identity();
        result.set(0, 0, cos);
        result.set(0, 1, -sin);
        result.set(1, 0, sin);
        result.set(1, 1, cos);
        return result;
    }

    // ИСПРАВЛЕННЫЙ lookAt - теперь правильно вычисляет направление
    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = target.subtract(eye).normalize(); // ИСПРАВЛЕНО: target - eye
        Vector3f xAxis = up.cross(zAxis).normalize();
        Vector3f yAxis = zAxis.cross(xAxis).normalize();

        Matrix4f result = identity();
        result.set(0, 0, xAxis.x);
        result.set(0, 1, xAxis.y);
        result.set(0, 2, xAxis.z);
        result.set(1, 0, yAxis.x);
        result.set(1, 1, yAxis.y);
        result.set(1, 2, yAxis.z);
        result.set(2, 0, zAxis.x);
        result.set(2, 1, zAxis.y);
        result.set(2, 2, zAxis.z);
        result.set(0, 3, -xAxis.dot(eye));
        result.set(1, 3, -yAxis.dot(eye));
        result.set(2, 3, -zAxis.dot(eye));

        return result;
    }

    public static Matrix4f perspective(float fov, float aspect, float near, float far) {
        float tanHalfFov = (float) Math.tan(fov / 2.0f);
        float range = far - near;

        Matrix4f result = new Matrix4f();
        result.set(0, 0, 1.0f / (aspect * tanHalfFov));
        result.set(1, 1, 1.0f / tanHalfFov);
        result.set(2, 2, -(far + near) / range);
        result.set(2, 3, -2.0f * far * near / range);
        result.set(3, 2, -1.0f);
        result.set(3, 3, 0.0f);

        return result;
    }

    public javax.vecmath.Matrix4f toVecmathMatrix() {
        javax.vecmath.Matrix4f result = new javax.vecmath.Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.setElement(i, j, matrix[i][j]);
            }
        }
        return result;
    }

    public static Matrix4f fromVecmathMatrix(javax.vecmath.Matrix4f mat) {
        float[][] matrixData = new float[4][4];

        matrixData[0][0] = mat.m00;
        matrixData[0][1] = mat.m01;
        matrixData[0][2] = mat.m02;
        matrixData[0][3] = mat.m03;

        matrixData[1][0] = mat.m10;
        matrixData[1][1] = mat.m11;
        matrixData[1][2] = mat.m12;
        matrixData[1][3] = mat.m13;

        matrixData[2][0] = mat.m20;
        matrixData[2][1] = mat.m21;
        matrixData[2][2] = mat.m22;
        matrixData[2][3] = mat.m23;

        matrixData[3][0] = mat.m30;
        matrixData[3][1] = mat.m31;
        matrixData[3][2] = mat.m32;
        matrixData[3][3] = mat.m33;

        return new Matrix4f(matrixData);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix4f matrix4f = (Matrix4f) obj;
        return Arrays.deepEquals(matrix, matrix4f.matrix);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%.2f", matrix[i][j]));
                if (j < 3) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}