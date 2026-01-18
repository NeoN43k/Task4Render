package com.cgvsu.model;

import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;

public class ModelSettings {
    private boolean drawLines;
    private boolean drawTexture;
    private boolean useLight;
    private Color color;
    private BufferedImage texture;

    // Конструктор
    public ModelSettings(boolean drawLines, boolean drawTexture, boolean useLight, Color color, BufferedImage texture) {
        this.drawLines = drawLines;
        this.drawTexture = drawTexture;
        this.useLight = useLight;
        this.color = color;
        this.texture = texture;
    }

    // Геттеры и сеттеры
    public boolean isDrawLines() {
        return drawLines;
    }

    public void setDrawLines(boolean drawLines) {
        this.drawLines = drawLines;
    }

    public boolean isDrawTexture() {
        return drawTexture;
    }

    public void setDrawTexture(boolean drawTexture) {
        this.drawTexture = drawTexture;
    }

    public boolean isUseLight() {
        return useLight;
    }

    public void setUseLight(boolean useLight) {
        this.useLight = useLight;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public void setTexture(BufferedImage texture) {
        this.texture = texture;
    }
}

