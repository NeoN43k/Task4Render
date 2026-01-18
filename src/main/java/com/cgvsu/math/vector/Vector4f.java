package com.cgvsu.math.vector;

import com.cgvsu.math.Global;

public class Vector4f {
    private static final float eps = 1e-4f;
    private float x;
    private float y;
    private float z;
    private float w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
            case 3:
                return w;
        }
        throw new IllegalArgumentException("index out of range");
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }

    public Vector4f add(Vector4f v) {
        return new Vector4f(x + v.getX(), y + v.getY(), z + v.getZ(), w + v.getW());
    }

    public Vector4f sub(Vector4f v) {
        return new Vector4f(x - v.getX(), y - v.getY(), z - v.getZ(), w - v.getW());
    }

    public Vector4f multiplyScalar(float scalar) {
        return new Vector4f(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    public Vector4f divScalar(float scalar) {
        if (Math.abs(scalar) < eps) {
            throw new ArithmeticException();
        } else {
            return new Vector4f(x / scalar, y / scalar, z / scalar, w / scalar);
        }
    }

    public float getLength() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public void normalize() {
        float magnitude = this.getLength();
        this.x /= magnitude;
        this.y /= magnitude;
        this.z /= magnitude;
        this.w /= magnitude;
    }

    public float dotProduct(Vector4f v) {
        return this.x * v.getX() + this.y * getY() + this.z * v.getZ() + this.w * v.getW();
    }

    public Vector3f translationVector3f() {
        return new Vector3f(getX(), getY(), getZ());
    }

    @Override
    public String toString() {
        return "Vector4f{" + "x=" + x + ", y=" + y + ", z=" + z + ", w=" + w + '}';
    }

    public boolean equals(Vector4f other) {
        return Math.abs(x - other.x) < Global.eps && Math.abs(y - other.y) < Global.eps &&
                Math.abs(z - other.z) < Global.eps && Math.abs(w - other.w) < Global.eps;
    }
}
