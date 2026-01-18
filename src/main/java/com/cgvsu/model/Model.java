package com.cgvsu.model;

import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;
import java.util.*;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    private String name;
    private BufferedImage texture;
    private String textureName;

    // Новые параметры для управления рендерингом
    private boolean drawLines;
    private boolean drawTexture;
    private boolean useLight;
    private Color color;

    public Model() {
        this.name = null;
        this.drawLines = false;
        this.drawTexture = false;
        this.useLight = false;
        this.color = Color.WHITE;
    }

    // Метод для глубокого копирования модели
    public Model (Model other) {
        for (Vector3f vertex : other.vertices) {
            this.vertices.add(new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ()));
        }
        for (Vector2f textureVertex : other.textureVertices) {
            this.textureVertices.add(new Vector2f(textureVertex.getX(), textureVertex.getY()));
        }
        for (Vector3f normal : other.normals) {
            this.normals.add(new Vector3f(normal.getX(), normal.getY(), normal.getZ()));
        }
        for (Polygon polygon : other.polygons) {
            this.polygons.add(new Polygon(polygon));
        }
        this.name = null;
        this.drawLines = false;
        this.drawTexture = false;
        this.useLight = false;
        this.color = Color.WHITE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public void setTexture(BufferedImage texture) {
        this.texture = texture;
    }

    public String getTextureName() {
        return textureName;
    }

    public void setTextureName(String textureName) {
        this.textureName = textureName;
    }

    public boolean hasTexture() {
        return texture != null;
    }

    // Геттеры и сеттеры для новых параметров

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
}
