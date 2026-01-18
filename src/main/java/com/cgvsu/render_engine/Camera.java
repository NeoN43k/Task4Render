package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

import javax.vecmath.*;

public class Camera {
    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float distance = 10.0f;
    private Vector3f up = new Vector3f(0, 1, 0);

    public Camera(Vector3f position, Vector3f target, float fov, float aspectRatio, float nearPlane, float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        calculateDistanceAndAngles();
    }

    public Camera(javax.vecmath.Vector3f position, javax.vecmath.Vector3f target,
                  float fov, float aspectRatio, float nearPlane, float farPlane) {
        this(new Vector3f(position.x, position.y, position.z),
                new Vector3f(target.x, target.y, target.z),
                fov, aspectRatio, nearPlane, farPlane);
    }

    private void calculateDistanceAndAngles() {
        Vector3f dir = target.subtract(position);
        distance = dir.length();

        if (distance > 0) {
            dir = dir.normalize();
            yaw = (float) Math.atan2(dir.x, dir.z);
            pitch = (float) Math.asin(dir.y);
        }
    }

    public javax.vecmath.Vector3f getPosition() {
        return new javax.vecmath.Vector3f(position.x, position.y, position.z);
    }

    public javax.vecmath.Vector3f getTarget() {
        return new javax.vecmath.Vector3f(target.x, target.y, target.z);
    }

    public Vector3f getPositionMath() {
        return position;
    }

    public Vector3f getTargetMath() {
        return target;
    }

    public void setPosition(javax.vecmath.Vector3f position) {
        this.position = new Vector3f(position.x, position.y, position.z);
        calculateDistanceAndAngles();
    }

    public void setTarget(javax.vecmath.Vector3f target) {
        this.target = new Vector3f(target.x, target.y, target.z);
        calculateDistanceAndAngles();
    }

    public void setPositionMath(Vector3f position) {
        this.position = position;
        calculateDistanceAndAngles();
    }

    public void setTargetMath(Vector3f target) {
        this.target = target;
        calculateDistanceAndAngles();
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    // ИСПРАВЛЕНО: Убрал инверсию
    public void rotate(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch += deltaPitch;

        pitch = Math.max(-(float)Math.PI/2 + 0.1f,
                Math.min((float)Math.PI/2 - 0.1f, pitch));

        updatePosition();
    }

    public void zoom(float delta) {
        distance = Math.max(1.0f, Math.min(100.0f, distance * (1.0f - delta * 0.1f)));
        updatePosition();
    }

    // ИСПРАВЛЕНО: Правильный pan
    public void pan(float deltaX, float deltaY) {
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = up.cross(forward).normalize();
        Vector3f actualUp = forward.cross(right).normalize();

        float panSpeed = distance * 0.002f;

        target = target.add(right.multiply(-deltaX * panSpeed));
        target = target.add(actualUp.multiply(deltaY * panSpeed));

        updatePosition();
    }

    private void updatePosition() {
        float horizontalDistance = distance * (float)Math.cos(pitch);
        float verticalDistance = distance * (float)Math.sin(pitch);

        float offsetX = horizontalDistance * (float)Math.sin(yaw);
        float offsetZ = horizontalDistance * (float)Math.cos(yaw);
        float offsetY = verticalDistance;

        position = new Vector3f(
                target.x + offsetX,
                target.y + offsetY,
                target.z + offsetZ
        );
    }

    public Matrix4f getViewMatrix() {
        return Matrix4f.lookAt(position, target, up);
    }

    public Matrix4f getProjectionMatrix() {
        return Matrix4f.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    public javax.vecmath.Matrix4f getViewMatrixOld() {
        return getViewMatrix().toVecmathMatrix();
    }

    public javax.vecmath.Matrix4f getProjectionMatrixOld() {
        return getProjectionMatrix().toVecmathMatrix();
    }

    public void moveTarget(javax.vecmath.Vector3f translation) {
        target = target.add(new Vector3f(translation.x, translation.y, translation.z));
        updatePosition();
    }

    public void movePosition(javax.vecmath.Vector3f translation) {
        position = position.add(new Vector3f(translation.x, translation.y, translation.z));
        target = target.add(new Vector3f(translation.x, translation.y, translation.z));
    }

    public float getFov() {
        return fov;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    // ДОБАВЛЕНО геттеры для отладки
    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getDistance() {
        return distance;
    }
}