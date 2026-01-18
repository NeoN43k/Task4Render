package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;

import javax.vecmath.Point2f;

public class GraphicConveyor {

    // Матрица модели (M) - локальные -> мировые
    public static Matrix4f getModelMatrix(Vector3f translation, Vector3f rotation, Vector3f scale) {
        // Порядок: M = T * R * S (для векторов-столбцов)
        Matrix4f scaleMatrix = Matrix4f.scale(scale.x, scale.y, scale.z);
        Matrix4f rotationMatrix = Matrix4f.rotateX(rotation.x)
                .multiply(Matrix4f.rotateY(rotation.y))
                .multiply(Matrix4f.rotateZ(rotation.z));
        Matrix4f translationMatrix = Matrix4f.translate(translation.x, translation.y, translation.z);

        return translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
    }

    // LookAt матрица (V) - мировые -> камера
    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        return Matrix4f.lookAt(eye, target, up);
    }

    // Перспективная проекция (P)
    public static Matrix4f perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        return Matrix4f.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    // Преобразование 3D точки в 2D экранные координаты
    public static Point2f vertexToPoint(Vector3f vertex, int width, int height) {
        // vertex уже в однородных координатах после проекции (x/w, y/w, z/w)
        float x = (vertex.x + 1.0f) * 0.5f * width;
        float y = (1.0f - vertex.y) * 0.5f * height; // Инвертируем Y
        return new Point2f(x, y);
    }

    // Умножение матрицы 4x4 на вектор 3 (добавляет w=1)
    public static Vector3f multiplyMatrix4ByVector3(Matrix4f matrix, Vector3f vertex) {
        Vector4f result = matrix.multiply(new Vector4f(vertex, 1.0f));
        // Перспективное деление
        return result.project();
    }

    // Умножение матрицы 4x4 на вектор 4
    public static Vector4f multiplyMatrix4ByVector4(Matrix4f matrix, Vector4f vector) {
        return matrix.multiply(vector);
    }

    // Старый метод для совместимости (использует javax.vecmath)
    public static javax.vecmath.Vector3f multiplyMatrix4ByVector3(
            javax.vecmath.Matrix4f matrix, javax.vecmath.Vector3f vertex) {
        // Конвертация в нашу систему
        Matrix4f ourMatrix = Matrix4f.fromVecmathMatrix(matrix);
        Vector3f ourVertex = new Vector3f(vertex.x, vertex.y, vertex.z);
        Vector3f result = multiplyMatrix4ByVector3(ourMatrix, ourVertex);
        return new javax.vecmath.Vector3f(result.x, result.y, result.z);
    }

    // Для обратной совместимости (старый rotateScaleTranslate)
    public static javax.vecmath.Matrix4f rotateScaleTranslate() {
        // Возвращает единичную матрицу
        javax.vecmath.Matrix4f matrix = new javax.vecmath.Matrix4f();
        matrix.setIdentity();
        return matrix;
    }
}