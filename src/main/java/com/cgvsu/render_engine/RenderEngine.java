package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Model;
import com.cgvsu.scene.SceneManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class RenderEngine {
    private static ZBuffer zBuffer;
    private static Texture currentTexture;
    private static Light currentLight;
    private static RenderModes renderMode = RenderModes.SOLID_COLOR;
    private static Color modelColor = Color.LIGHTGRAY;

    public static void setRenderMode(RenderModes mode) {
        renderMode = mode;
    }

    public static void setModelColor(Color color) {
        modelColor = color;
    }

    public static void setTexture(Texture texture) {
        currentTexture = texture;
    }

    public static void setLight(Light light) {
        currentLight = light;
    }

    public static void renderScene(
            final Canvas canvas,
            final Camera camera,
            final SceneManager sceneManager) {

        GraphicsContext gc = canvas.getGraphicsContext2D();
        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();

        // Очищаем канвас
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        // Инициализируем Z-буфер
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
        }
        zBuffer.clear();

        // Вывод отладочной информации
        gc.setFill(Color.WHITE);
        gc.fillText("3D Viewer - Debug Mode", 10, 20);
        gc.fillText("Screen: " + width + "x" + height, 10, 40);
        gc.fillText("Camera Pos: (" + String.format("%.1f", camera.getPositionMath().x) +
                ", " + String.format("%.1f", camera.getPositionMath().y) +
                ", " + String.format("%.1f", camera.getPositionMath().z) + ")", 10, 60);

        // Создаем свет, привязанный к камере (если еще не создан)
        if (currentLight == null) {
            currentLight = Light.createCameraLight(camera);
        }

        // Рендерим тестовый куб для отладки
        renderDebugCube(gc, camera, width, height);

        // Рендерим все видимые модели из сцены
        if (sceneManager != null && sceneManager.getModels() != null) {
            for (SceneManager.SceneModel sceneModel : sceneManager.getModels()) {
                if (sceneModel != null && sceneModel.isVisible()) {
                    Model model = sceneModel.getModel();
                    if (model != null) {
                        renderModelWireframe(gc, camera, model, width, height);
                    }
                }
            }
        }
    }

    private static void renderDebugCube(GraphicsContext gc, Camera camera, int width, int height) {
        try {
            // Создаем простой куб
            Model cube = new Model();
            cube.setName("Debug Cube");

            // Вершины куба
            float size = 1.0f;
            float[] vertices = {
                    -size, -size, -size,  // 0
                    size, -size, -size,  // 1
                    size,  size, -size,  // 2
                    -size,  size, -size,  // 3
                    -size, -size,  size,  // 4
                    size, -size,  size,  // 5
                    size,  size,  size,  // 6
                    -size,  size,  size   // 7
            };

            // Добавляем вершины
            for (int i = 0; i < vertices.length; i += 3) {
                cube.vertices.add(new Vector3f(vertices[i], vertices[i+1], vertices[i+2]));
            }

            // Добавляем полигоны (треугольники)
            int[][] triangles = {
                    {0,1,2}, {0,2,3}, // задняя грань
                    {4,5,6}, {4,6,7}, // передняя грань
                    {0,3,7}, {0,7,4}, // левая грань
                    {1,5,6}, {1,6,2}, // правая грань
                    {3,2,6}, {3,6,7}, // верхняя грань
                    {0,4,5}, {0,5,1}  // нижняя грань
            };

            for (int[] tri : triangles) {
                com.cgvsu.model.Polygon poly = new com.cgvsu.model.Polygon();
                java.util.ArrayList<Integer> indices = new java.util.ArrayList<>();
                indices.add(tri[0]);
                indices.add(tri[1]);
                indices.add(tri[2]);
                poly.setVertexIndices(indices);
                cube.polygons.add(poly);
            }

            // Рассчитываем нормали
            cube.calculateNormals();

            // Рендерим куб
            cube.setPosition(0, 0, 0);
            renderModelWireframe(gc, camera, cube, width, height);

        } catch (Exception e) {
            System.err.println("Error rendering debug cube: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void renderModelWireframe(
            final GraphicsContext gc,
            final Camera camera,
            final Model model,
            final int width,
            final int height) {

        try {
            // Готовим модель
            model.triangulate();

            // Получаем матрицы
            Matrix4f modelMatrix = model.getTransformMatrix();
            Matrix4f viewMatrix = camera.getViewMatrix();
            Matrix4f projectionMatrix = camera.getProjectionMatrix();

            // Правильный порядок: P * V * M
            Matrix4f mvpMatrix = projectionMatrix.multiply(viewMatrix.multiply(modelMatrix));

            // Рисуем полигоны как сетку
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(1);

            for (com.cgvsu.model.Polygon polygon : model.getPolygons()) {
                ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
                if (vertexIndices.size() < 3) continue;

                // Получаем и преобразуем вершины полигона
                java.util.ArrayList<javax.vecmath.Point2f> points = new java.util.ArrayList<>();

                for (Integer vertexIndex : vertexIndices) {
                    if (vertexIndex >= model.getVertices().size()) continue;

                    Vector3f vertex = model.getVertices().get(vertexIndex);
                    Vector4f clipSpace = GraphicConveyor.multiplyMatrix4ByVector4(mvpMatrix,
                            new Vector4f(vertex.x, vertex.y, vertex.z, 1.0f));
                    javax.vecmath.Point2f point = GraphicConveyor.vertexToPoint(clipSpace, width, height);
                    if (point != null) {
                        points.add(point);
                    }
                }

                // Рисуем линии полигона
                if (points.size() >= 3) {
                    for (int i = 0; i < points.size(); i++) {
                        javax.vecmath.Point2f current = points.get(i);
                        javax.vecmath.Point2f next = points.get((i + 1) % points.size());
                        gc.strokeLine(current.x, current.y, next.x, next.y);
                    }
                }
            }

            // Рисуем вершины как точки
            gc.setFill(Color.YELLOW);
            for (int i = 0; i < model.getVertices().size(); i++) {
                Vector3f vertex = model.getVertices().get(i);
                Vector4f clipSpace = GraphicConveyor.multiplyMatrix4ByVector4(mvpMatrix,
                        new Vector4f(vertex.x, vertex.y, vertex.z, 1.0f));
                javax.vecmath.Point2f point = GraphicConveyor.vertexToPoint(clipSpace, width, height);
                if (point != null) {
                    gc.fillOval(point.x - 2, point.y - 2, 4, 4);
                }
            }

        } catch (Exception e) {
            System.err.println("Error rendering model wireframe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Color getModelColor() {
        return modelColor;
    }

    public static Texture getCurrentTexture() {
        return currentTexture;
    }

    public static Light getCurrentLight() {
        return currentLight;
    }

    public static RenderModes getRenderMode() {
        return renderMode;
    }

    public static void cleanup() {
        zBuffer = null;
        currentTexture = null;
        currentLight = null;
    }
}