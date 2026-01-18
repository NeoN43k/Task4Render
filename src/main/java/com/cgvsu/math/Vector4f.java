package com.cgvsu.math;

public class Vector4f {
    public float x, y, z, w;

    public Vector4f() {
        this(0, 0, 0, 1);
    }

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(Vector3f v, float w) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = w;
    }

    public Vector4f add(Vector4f other) {
        return new Vector4f(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    public Vector4f subtract(Vector4f other) {
        return new Vector4f(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    public Vector4f multiply(float scalar) {
        return new Vector4f(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    public Vector4f divide(float scalar) {
        if (Math.abs(scalar) < 1e-6f) {
            throw new ArithmeticException("Division by zero");
        }
        return new Vector4f(x / scalar, y / scalar, z / scalar, w / scalar);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public Vector4f normalize() {
        float len = length();
        if (len < 1e-6f) {
            return new Vector4f(0, 0, 0, 1);
        }
        return divide(len);
    }

    public float dot(Vector4f other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    // Проекция на однородные координаты (деление на w)
    public Vector3f project() {
        if (Math.abs(w) < 1e-6f) {
            return new Vector3f(x, y, z);
        }
        return new Vector3f(x / w, y / w, z / w);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f, %.2f)", x, y, z, w);
    }
}