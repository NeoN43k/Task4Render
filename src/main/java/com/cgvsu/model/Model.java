package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import java.util.ArrayList;

import java.util.*;

public class Model {
    private String name;
    private boolean selected = false;
    private String filePath;

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();




    // ВНИМАНИЕ: Второй студент добавит сюда матрицу трансформаций!
    // private Matrix4f transformMatrix;

    public Model() {
        this.vertices = new ArrayList<>();
        this.polygons = new ArrayList<>();
        this.name = "Unnamed Model";
    }

    // === Конструктор для копирования ===
    public Model(Model other) {
        this.name = other.name + " (copy)";
        this.vertices = new ArrayList<>(other.vertices);
        this.polygons = new ArrayList<>(other.polygons);
        this.filePath = other.filePath;
    }

    // === Геттеры и сеттеры ===
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Vector3f> getVertices() { return vertices; }
    public void setVertices(List<Vector3f> vertices) { this.vertices = (ArrayList<Vector3f>) vertices; }

    public List<Polygon> getPolygons() { return polygons; }
    public void setPolygons(List<Polygon> polygons) { this.polygons = (ArrayList<Polygon>) polygons; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    // === Утилитные методы ===
    public int getVertexCount() { return vertices.size(); }
    public int getPolygonCount() { return polygons.size(); }

    // TODO: Второй студент добавит методы для работы с трансформациями
    // public Matrix4f getTransformMatrix() { ... }
    // public void applyTransformation(Matrix4f matrix) { ... }
    // public List<Vector3f> getTransformedVertices() { ... }
}