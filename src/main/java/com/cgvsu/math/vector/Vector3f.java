package com.cgvsu.math.vector;

import com.cgvsu.math.Global;
import com.cgvsu.math.matrix.Matrix4f;

public class Vector3f {
    private float x;
    private float y;
    private float z;

    public Vector3f(float x, float y, float z) {
        if (x == -0.0) {
            x = 0;
        }
        if (y == -0.0) {
            y = 0;
        }
        if (z == -0.0) {
            z = 0;
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public float get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
        }
        throw new IllegalArgumentException("index out of bounds");
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

    public void add(Vector3f v) {
        this.x += v.getX();
        this.y += v.getY();
        this.z += v.getZ();
    }

    public void addVectorThis(Vector3f v) {
        this.x += v.getX();
        this.y += v.getY();
        this.z += v.getZ();
    }

    public void sub(Vector3f v1, Vector3f v2) {
        x = v1.getX() - v2.getX();
        y = v1.getY() - v2.getY();
        z = v1.getZ() - v2.getZ();
    }

    public Vector3f multiplyScalar(float a) {
        return new Vector3f(x * a, y * a, z * a);
    }

    public Vector3f divScalar(float a) {
        if (Math.abs(a) < Global.eps) {
            throw new ArithmeticException("Division by zero");
        } else {
            return new Vector3f(x / a, y / a, z / a);
        }
    }

    // Возвращает величину (длину) вектора
    public float getLength() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    // Возвращает нормализованный вектор (с длиной 1)
    public void normalize() {
        float magnitude = this.getLength();
        this.x /= magnitude;
        this.y /= magnitude;
        this.z /= magnitude;
    }

    // скалярное произведение векторов
    public float dot(Vector3f v) {
        return this.x * v.getX() + this.y * v.getY() + this.z * v.getZ();
    }

    public Vector3f vectorMultiply(Vector3f v) {
        float newX = this.y * v.getZ() - this.z * v.getY();
        float newY = this.z * v.getX() - this.x * v.getZ();
        float newZ = this.x * v.getY() - this.y * v.getX();
        return new Vector3f(newX, newY, newZ);
    }

    public Vector4f translationToVector4f() {
        return new Vector4f(getX(), getY(), getZ(), 1);
    }

    public boolean equals(Vector3f other) {
        return Math.abs(x - other.x) < Global.eps && Math.abs(y - other.y) < Global.eps && Math.abs(z - other.z) < Global.eps;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(Matrix4f matrix, Vector3f vertex) {
        float x = matrix.getCell(0, 0) * vertex.getX() + matrix.getCell(0, 1) * vertex.getY()
                + matrix.getCell(0, 2) * vertex.getZ() + matrix.getCell(0, 3);
        float y = matrix.getCell(1, 0) * vertex.getX() + matrix.getCell(1, 1) * vertex.getY()
                + matrix.getCell(1, 2) * vertex.getZ() + matrix.getCell(1, 3);
        float z = matrix.getCell(2, 0) * vertex.getX() + matrix.getCell(2, 1) * vertex.getY()
                + matrix.getCell(2, 2) * vertex.getZ() + matrix.getCell(2, 3);
        float w = matrix.getCell(3, 0) * vertex.getX() + matrix.getCell(3, 1) * vertex.getY()
                + matrix.getCell(3, 2) * vertex.getZ() + matrix.getCell(3, 3);

        // Деление на W для преобразования в нормализованное устройство
        return new Vector3f(x / w, y / w, z / w);
    }
    // Возвращает вектор, перпендикулярный двум переданным (векторное произведение)
    /*public static Vector3f cross(Vector3f v1, Vector3f v2) {
        float vNormalX = ((v1.y * v2.z) - (v1.z * v2.y));
        float vNormalY = ((v1.z * v2.x) - (v1.x * v2.z));
        float vNormalZ = ((v1.x * v2.y) - (v1.y * v2.x));
        return new Vector3f(vNormalX, vNormalY, vNormalZ);
    }*/

    public void cross(Vector3f v1, Vector3f v2) {
        this.x = ((v1.y * v2.z) - (v1.z * v2.y));
        this.y = ((v1.z * v2.x) - (v1.x * v2.z));
        this.z = ((v1.x * v2.y) - (v1.y * v2.x));
    }

    // Возвращает вектор между 2мя точками
    public static Vector3f vector(Vector3f point1, Vector3f point2) {
        float vectorX = (point1.x - point2.x);
        float vectorY = (point1.y - point2.y);
        float vectorZ = (point1.z - point2.z);
        return new Vector3f(vectorX, vectorY, vectorZ);
    }
    public static Vector3f normalPolygon(Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f newV1 = vector(v1, v0);
        Vector3f newV2 = vector(v2, v0);

        Vector3f vNormal = new Vector3f();
        vNormal.cross(newV1, newV2);
        vNormal.normalize();
        return vNormal;
    }

    // Укорачивает значения координат вектора до 4 знаков после запятой
    public void shortenTo4 (){
        float scale = (float) Math.pow(10, 4); // 10^4 для 4 знаков после запятой
        x = Math.round(x * scale) / scale;
        y = Math.round(y * scale) / scale;
        z = Math.round(z * scale) / scale;
    }

    // Деление вектора на скаляр
    public Vector3f divide(float scalar) {
        return new Vector3f(x / scalar, y / scalar, z / scalar);
    }

    @Override
    public String toString() {
        return "Vector3f{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }

    public void setX(float d) {
        this.x = d;
    }

    public void setY(float d) {
        this.y = d;
    }

    public void setZ(float d) {
        this.z = d;
    }
}
