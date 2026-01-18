package com.cgvsu.math.vector;

public class Vector2f {
    private static final float eps = 1e-4f;
    private float x;
    private float y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
        }
        throw new IllegalArgumentException();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Vector2f add(Vector2f other) {
        return new Vector2f(x + other.getX(), y + other.getY());
    }

    public Vector2f sub(Vector2f other) {
        return new Vector2f(x - other.getX(), y - other.getY());
    }

    public Vector2f multiplyScalar(float scalar) {
        return new Vector2f(x * scalar, y * scalar);
    }

    public Vector2f divScalar(float scalar) {
        if (Math.abs(scalar) < eps) {
            throw new ArithmeticException("Делить на 0 нельзя");
        }
        return new Vector2f(x / scalar, y / scalar);
    }

    public float getLength() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public void normalize() {
        float magnitude = this.getLength();
        this.x /= magnitude;
        this.y /= magnitude;
    }

    public float dotProduct(Vector2f v) {
        return this.x * v.getX() + this.y * v.getY();
    }

    @Override
    public String toString() {
        return "Vector2f{" + "x=" + x + ", y=" + y + '}';
    }

    public boolean equals(Vector2f other) {
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
    }
}
