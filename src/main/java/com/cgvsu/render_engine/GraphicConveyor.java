package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;

import javax.vecmath.Point2f;

public class GraphicConveyor {

    // ИСПРАВЛЕНО: Правильный порядок матриц - T * R * S
    public static Matrix4f getModelMatrix(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4f scaleMatrix = Matrix4f.scale(scale.x, scale.y, scale.z);
        Matrix4f rotationMatrix = Matrix4f.rotateX(rotation.x)
                .multiply(Matrix4f.rotateY(rotation.y))
                .multiply(Matrix4f.rotateZ(rotation.z));
        Matrix4f translationMatrix = Matrix4f.translate(translation.x, translation.y, translation.z);

        // Объединяем: T * R * S
        return translationMatrix.multiply(rotationMatrix.multiply(scaleMatrix));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        return Matrix4f.lookAt(eye, target, up);
    }

    public static Matrix4f perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        return Matrix4f.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    // УПРОЩЕННЫЙ метод для отладки
    public static Point2f vertexToPoint(Vector4f vertexClipSpace, int width, int height) {
        if (vertexClipSpace == null || Math.abs(vertexClipSpace.w) < 1e-6f) {
            return null;
        }

        // Перспективное деление
        float invW = 1.0f / vertexClipSpace.w;
        float x = vertexClipSpace.x * invW;
        float y = vertexClipSpace.y * invW;

        // Преобразование в экранные координаты
        float screenX = (x + 1.0f) * 0.5f * width;
        float screenY = (1.0f - y) * 0.5f * height;

        return new Point2f(screenX, screenY);
    }

    // Старый метод для совместимости
    public static Point2f vertexToPointOld(Vector3f vertex, int width, int height) {
        if (vertex == null) return null;

        // Преобразование из NDC [-1, 1] в экранные координаты
        float x = (vertex.x + 1.0f) * 0.5f * width;
        float y = (1.0f - vertex.y) * 0.5f * height;

        return new Point2f(x, y);
    }

    public static Vector3f multiplyMatrix4ByVector3(Matrix4f matrix, Vector3f vertex) {
        Vector4f vertex4 = new Vector4f(vertex.x, vertex.y, vertex.z, 1.0f);
        Vector4f result4 = matrix.multiply(vertex4);
        return new Vector3f(result4.x, result4.y, result4.z);
    }

    public static Vector4f multiplyMatrix4ByVector4(Matrix4f matrix, Vector4f vector) {
        return matrix.multiply(vector);
    }

    // Для нормалей (w=0) с нормализацией
    public static Vector3f multiplyMatrix4ByVector3ForNormal(Matrix4f matrix, Vector3f normal) {
        Vector4f normal4 = new Vector4f(normal.x, normal.y, normal.z, 0.0f);
        Vector4f result4 = matrix.multiply(normal4);
        Vector3f result = new Vector3f(result4.x, result4.y, result4.z);
        return result.normalize();
    }

    public static javax.vecmath.Vector3f multiplyMatrix4ByVector3(
            javax.vecmath.Matrix4f matrix, javax.vecmath.Vector3f vertex) {
        Matrix4f ourMatrix = Matrix4f.fromVecmathMatrix(matrix);
        Vector3f ourVertex = new Vector3f(vertex.x, vertex.y, vertex.z);
        Vector3f result = multiplyMatrix4ByVector3(ourMatrix, ourVertex);
        return new javax.vecmath.Vector3f(result.x, result.y, result.z);
    }

    public static javax.vecmath.Matrix4f rotateScaleTranslate() {
        javax.vecmath.Matrix4f matrix = new javax.vecmath.Matrix4f();
        matrix.setIdentity();
        return matrix;
    }
}