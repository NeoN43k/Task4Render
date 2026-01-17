package com.cgvsu.render_engine;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class Texture {
    private Image image;
    private PixelReader pixelReader;
    private int width;
    private int height;

    public Texture(String filePath) {
        this.image = new Image("file:" + filePath);
        this.pixelReader = image.getPixelReader();
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();
    }

    public Color getColor(float u, float v) {
        // Приведение координат текстуры к диапазону [0, 1]
        while (u < 0) u += 1;
        while (u > 1) u -= 1;
        while (v < 0) v += 1;
        while (v > 1) v -= 1;

        // Инвертируем V, так как в OpenGL текстуры начинаются снизу
        v = 1 - v;

        int x = (int) (u * (width - 1));
        int y = (int) (v * (height - 1));

        return pixelReader.getColor(x, y);
    }

    public boolean isLoaded() {
        return image != null;
    }
}