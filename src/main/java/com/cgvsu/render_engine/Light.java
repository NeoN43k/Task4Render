package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;

public class Light {
    private Vector3f position;
    private Vector3f color;
    private float intensity;

    public Light(Vector3f position, Vector3f color, float intensity) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }

    // Источник света, привязанный к камере
    public static Light createCameraLight(Camera camera) {
        // Свет чуть выше и сзади камеры
        javax.vecmath.Vector3f cameraPos = camera.getPosition();
        javax.vecmath.Vector3f cameraTarget = camera.getTarget();

        // Направление от камеры к цели
        Vector3f direction = new Vector3f(
                cameraTarget.x - cameraPos.x,
                cameraTarget.y - cameraPos.y,
                cameraTarget.z - cameraPos.z
        );

        // Нормализуем направление
        float length = (float) Math.sqrt(direction.x * direction.x +
                direction.y * direction.y +
                direction.z * direction.z);
        direction.x /= length;
        direction.y /= length;
        direction.z /= length;

        // Позиция света - немного выше камеры
        Vector3f lightPos = new Vector3f(
                cameraPos.x - direction.x * 2,
                cameraPos.y - direction.y * 2 + 1,
                cameraPos.z - direction.z * 2
        );

        return new Light(lightPos, new Vector3f(1, 1, 1), 1.0f);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }
}