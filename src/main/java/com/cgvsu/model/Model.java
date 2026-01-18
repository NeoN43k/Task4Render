package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.render_engine.GraphicConveyor;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private String name;
    private boolean selected = false;
    private String filePath;

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    // Матрица трансформации модели
    private Matrix4f transformMatrix;
    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0); // В радианах
    private Vector3f scale = new Vector3f(1, 1, 1);

    public Model() {
        this.vertices = new ArrayList<>();
        this.polygons = new ArrayList<>();
        this.name = "Unnamed Model";
        updateTransformMatrix();
    }

    // Конструктор для копирования
    public Model(Model other) {
        this.name = other.name + " (copy)";
        this.vertices = new ArrayList<>(other.vertices);
        this.polygons = new ArrayList<>(other.polygons);
        this.filePath = other.filePath;
        this.position = new Vector3f(other.position.x, other.position.y, other.position.z);
        this.rotation = new Vector3f(other.rotation.x, other.rotation.y, other.rotation.z);
        this.scale = new Vector3f(other.scale.x, other.scale.y, other.scale.z);
        updateTransformMatrix();
    }

    // === Трансформации ===
    public void updateTransformMatrix() {
        this.transformMatrix = GraphicConveyor.getModelMatrix(position, rotation, scale);
    }

    public void setPosition(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
        updateTransformMatrix();
    }

    public void setRotation(float x, float y, float z) {
        this.rotation = new Vector3f(x, y, z);
        updateTransformMatrix();
    }

    public void setScale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
        updateTransformMatrix();
    }

    public void translate(float dx, float dy, float dz) {
        this.position = new Vector3f(
                position.x + dx,
                position.y + dy,
                position.z + dz
        );
        updateTransformMatrix();
    }

    public void rotate(float dx, float dy, float dz) {
        this.rotation = new Vector3f(
                rotation.x + dx,
                rotation.y + dy,
                rotation.z + dz
        );
        updateTransformMatrix();
    }

    public void scale(float sx, float sy, float sz) {
        this.scale = new Vector3f(
                scale.x * sx,
                scale.y * sy,
                scale.z * sz
        );
        updateTransformMatrix();
    }

    public Matrix4f getTransformMatrix() {
        return transformMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    // Получение трансформированных вершин
    public List<Vector3f> getTransformedVertices() {
        List<Vector3f> transformed = new ArrayList<>();
        for (Vector3f vertex : vertices) {
            Vector3f transformedVertex = GraphicConveyor.multiplyMatrix4ByVector3(
                    transformMatrix, vertex
            );
            transformed.add(transformedVertex);
        }
        return transformed;
    }

    // Получение трансформированных нормалей (только вращение)
    public List<Vector3f> getTransformedNormals() {
        if (normals.isEmpty()) {
            calculateNormals();
        }

        List<Vector3f> transformed = new ArrayList<>();

        // Создаем матрицу только с вращением
        Matrix4f rotationMatrix = Matrix4f.rotateX(rotation.x)
                .multiply(Matrix4f.rotateY(rotation.y))
                .multiply(Matrix4f.rotateZ(rotation.z));

        for (Vector3f normal : normals) {
            Vector4f normal4 = new Vector4f(normal.x, normal.y, normal.z, 0);
            Vector4f transformed4 = rotationMatrix.multiply(normal4);
            Vector3f transformedNormal = new Vector3f(
                    transformed4.x, transformed4.y, transformed4.z
            ).normalize();
            transformed.add(transformedNormal);
        }
        return transformed;
    }

    // === Геттеры и сеттеры ===
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vector3f> vertices) {
        this.vertices = (ArrayList<Vector3f>) vertices;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(List<Polygon> polygons) {
        this.polygons = (ArrayList<Polygon>) polygons;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getPolygonCount() {
        return polygons.size();
    }

    // === Утилитные методы ===
    public void calculateNormals() {
        // Инициализируем список нормалей для вершин
        normals.clear();
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3f(0, 0, 0));
        }

        // Вычисляем нормали для каждого полигона и добавляем к вершинам
        for (Polygon polygon : polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() < 3) continue;

            // Берем первые три вершины полигона для вычисления нормали
            Vector3f v0 = vertices.get(vertexIndices.get(0));
            Vector3f v1 = vertices.get(vertexIndices.get(1));
            Vector3f v2 = vertices.get(vertexIndices.get(2));

            // Вектора сторон треугольника
            Vector3f edge1 = new Vector3f(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
            Vector3f edge2 = new Vector3f(v2.x - v0.x, v2.y - v0.y, v2.z - v0.z);

            // Векторное произведение для получения нормали
            Vector3f normal = new Vector3f(
                    edge1.y * edge2.z - edge1.z * edge2.y,
                    edge1.z * edge2.x - edge1.x * edge2.z,
                    edge1.x * edge2.y - edge1.y * edge2.x
            );

            // Нормализуем нормаль
            float length = (float) Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
            if (length > 0) {
                normal.x /= length;
                normal.y /= length;
                normal.z /= length;
            }

            // Добавляем нормаль ко всем вершинам полигона
            for (Integer vertexIndex : vertexIndices) {
                Vector3f currentNormal = normals.get(vertexIndex);
                currentNormal.x += normal.x;
                currentNormal.y += normal.y;
                currentNormal.z += normal.z;
            }
        }

        // Нормализуем все нормали вершин
        for (Vector3f normal : normals) {
            float length = (float) Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);
            if (length > 0) {
                normal.x /= length;
                normal.y /= length;
                normal.z /= length;
            }
        }
    }

    public void triangulate() {
        ArrayList<Polygon> triangulatedPolygons = new ArrayList<>();

        for (Polygon polygon : polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();

            // Если полигон уже треугольник - просто добавляем
            if (vertexIndices.size() == 3) {
                triangulatedPolygons.add(polygon);
                continue;
            }

            // Триангуляция веером от первой вершины
            for (int i = 1; i < vertexIndices.size() - 1; i++) {
                Polygon triangle = new Polygon();

                ArrayList<Integer> triVertices = new ArrayList<>();
                triVertices.add(vertexIndices.get(0));
                triVertices.add(vertexIndices.get(i));
                triVertices.add(vertexIndices.get(i + 1));
                triangle.setVertexIndices(triVertices);

                // Если есть текстурные координаты
                if (!textureIndices.isEmpty() && textureIndices.size() == vertexIndices.size()) {
                    ArrayList<Integer> triTextures = new ArrayList<>();
                    triTextures.add(textureIndices.get(0));
                    triTextures.add(textureIndices.get(i));
                    triTextures.add(textureIndices.get(i + 1));
                    triangle.setTextureVertexIndices(triTextures);
                }

                // Если есть нормали
                if (!normalIndices.isEmpty() && normalIndices.size() == vertexIndices.size()) {
                    ArrayList<Integer> triNormals = new ArrayList<>();
                    triNormals.add(normalIndices.get(0));
                    triNormals.add(normalIndices.get(i));
                    triNormals.add(normalIndices.get(i + 1));
                    triangle.setNormalIndices(triNormals);
                }

                triangulatedPolygons.add(triangle);
            }
        }

        polygons = triangulatedPolygons;
    }

    // Сохранение модели с учетом трансформаций
    public Model getTransformedModel() {
        Model transformed = new Model();
        transformed.setName(this.name + " (transformed)");

        // Копируем полигоны
        transformed.polygons = new ArrayList<>(this.polygons);
        transformed.textureVertices = new ArrayList<>(this.textureVertices);

        // Применяем трансформации к вершинам
        for (Vector3f vertex : this.vertices) {
            Vector3f transformedVertex = GraphicConveyor.multiplyMatrix4ByVector3(
                    this.transformMatrix, vertex
            );
            transformed.vertices.add(transformedVertex);
        }

        // Применяем трансформации к нормалям
        for (Vector3f normal : this.normals) {
            // Создаем матрицу только с вращением
            Matrix4f rotationMatrix = Matrix4f.rotateX(rotation.x)
                    .multiply(Matrix4f.rotateY(rotation.y))
                    .multiply(Matrix4f.rotateZ(rotation.z));

            Vector4f normal4 = new Vector4f(normal.x, normal.y, normal.z, 0);
            Vector4f transformed4 = rotationMatrix.multiply(normal4);
            Vector3f transformedNormal = new Vector3f(
                    transformed4.x, transformed4.y, transformed4.z
            ).normalize();
            transformed.normals.add(transformedNormal);
        }

        return transformed;
    }
}