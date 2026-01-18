package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

import javax.vecmath.*;

public class Camera {

    private com.cgvsu.math.Vector3f position;
    private com.cgvsu.math.Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    // Для управления мышкой
    private float yaw = 0.0f;   // Поворот вокруг Y
    private float pitch = 0.0f; // Поворот вокруг X
    private float distance = 5.0f; // Дистанция от цели

    // Конструктор с нашими Vector3f
    public Camera(com.cgvsu.math.Vector3f position, com.cgvsu.math.Vector3f target,
                  float fov, float aspectRatio, float nearPlane, float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        updatePositionFromOrbit();
    }

    // Конструктор для совместимости со старым кодом (javax.vecmath.Vector3f)
    public Camera(javax.vecmath.Vector3f position, javax.vecmath.Vector3f target,
                  float fov, float aspectRatio, float nearPlane, float farPlane) {
        this(new com.cgvsu.math.Vector3f(position.x, position.y, position.z),
                new com.cgvsu.math.Vector3f(target.x, target.y, target.z),
                fov, aspectRatio, nearPlane, farPlane);
    }

    // Геттеры и сеттеры
    public javax.vecmath.Vector3f getPosition() {
        return new javax.vecmath.Vector3f(position.x, position.y, position.z);
    }

    public javax.vecmath.Vector3f getTarget() {
        return new javax.vecmath.Vector3f(target.x, target.y, target.z);
    }

    public com.cgvsu.math.Vector3f getPositionMath() {
        return position;
    }

    public com.cgvsu.math.Vector3f getTargetMath() {
        return target;
    }

    public void setPosition(javax.vecmath.Vector3f position) {
        this.position = new com.cgvsu.math.Vector3f(position.x, position.y, position.z);
    }

    public void setTarget(javax.vecmath.Vector3f target) {
        this.target = new com.cgvsu.math.Vector3f(target.x, target.y, target.z);
        updatePositionFromOrbit();
    }

    public void setPositionMath(com.cgvsu.math.Vector3f position) {
        this.position = position;
    }

    public void setTargetMath(com.cgvsu.math.Vector3f target) {
        this.target = target;
        updatePositionFromOrbit();
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    // Управление орбитальной камерой
    public void rotate(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch += deltaPitch;

        // Ограничиваем pitch чтобы не переворачивать камеру
        pitch = Math.max(-89.0f, Math.min(89.0f, pitch));

        updatePositionFromOrbit();
    }

    public void zoom(float delta) {
        distance = Math.max(1.0f, Math.min(50.0f, distance + delta));
        updatePositionFromOrbit();
    }

    public void pan(float deltaX, float deltaY) {
        // Вычисляем вектора right и up камеры
        com.cgvsu.math.Vector3f forward = target.subtract(position).normalize();
        com.cgvsu.math.Vector3f right = forward.cross(new com.cgvsu.math.Vector3f(0, 1, 0)).normalize();
        com.cgvsu.math.Vector3f up = right.cross(forward).normalize();

        // Двигаем цель
        target = target.add(right.multiply(-deltaX));
        target = target.add(up.multiply(deltaY));

        updatePositionFromOrbit();
    }

    private void updatePositionFromOrbit() {
        // Преобразуем сферические координаты в декартовы
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float x = distance * (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        float y = distance * (float) Math.sin(pitchRad);
        float z = distance * (float) (Math.cos(pitchRad) * Math.cos(yawRad));

        position = target.add(new com.cgvsu.math.Vector3f(x, y, z));
    }

    // Матрица вида
    public Matrix4f getViewMatrix() {
        com.cgvsu.math.Vector3f up = new com.cgvsu.math.Vector3f(0, 1, 0);
        return GraphicConveyor.lookAt(position, target, up);
    }

    // Матрица проекции
    public Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    // Старые методы для совместимости
    public javax.vecmath.Matrix4f getViewMatrixOld() {
        Matrix4f view = getViewMatrix();
        return convertMatrix(view);
    }

    public javax.vecmath.Matrix4f getProjectionMatrixOld() {
        Matrix4f proj = getProjectionMatrix();
        return convertMatrix(proj);
    }

    private javax.vecmath.Matrix4f convertMatrix(Matrix4f matrix) {
        javax.vecmath.Matrix4f result = new javax.vecmath.Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.setElement(i, j, matrix.get(i, j));
            }
        }
        return result;
    }

    // Исправленный метод moveTarget
    public void moveTarget(javax.vecmath.Vector3f translation) {
        target = target.add(new com.cgvsu.math.Vector3f(translation.x, translation.y, translation.z));
        updatePositionFromOrbit();
    }

    // Метод для перемещения камеры
    public void movePosition(javax.vecmath.Vector3f translation) {
        position = position.add(new com.cgvsu.math.Vector3f(translation.x, translation.y, translation.z));
        target = target.add(new com.cgvsu.math.Vector3f(translation.x, translation.y, translation.z));
    }
}