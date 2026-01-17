package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.scene.SceneManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.vecmath.*;
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

        // Инициализируем Z-буфер
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
        }
        zBuffer.clear();

        // Создаем свет, привязанный к камере
        if (currentLight == null) {
            currentLight = Light.createCameraLight(camera);
        }

        // Рендерим все модели
        for (SceneManager.SceneModel sceneModel : sceneManager.getModels()) {
            Model model = sceneModel.getModel();
            renderModel(gc, camera, model, width, height);
        }
    }

    public static void renderModel(
            final GraphicsContext gc,
            final Camera camera,
            final Model model,
            final int width,
            final int height) {

        // Готовим модель к отрисовке
        model.triangulate();
        model.calculateNormals();

        // Матрицы преобразований
        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewMatrix = new Matrix4f(modelMatrix);
        modelViewMatrix.mul(viewMatrix);

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelViewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        // Преобразуем вершины и нормали
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();
        ArrayList<Vector3f> transformedNormals = new ArrayList<>();

        // Трансформируем вершины
        for (Vector3f vertex : model.vertices) {
            javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.x, vertex.y, vertex.z);
            javax.vecmath.Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(
                    modelViewProjectionMatrix, vertexVecmath);
            transformedVertices.add(new Vector3f(transformed.x, transformed.y, transformed.z));
        }

        // Трансформируем нормали (только модель-вью матрица, без проекции)
        for (Vector3f normal : model.normals) {
            javax.vecmath.Vector3f normalVecmath = new javax.vecmath.Vector3f(normal.x, normal.y, normal.z);
            javax.vecmath.Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(
                    modelViewMatrix, normalVecmath);

            // Нормализуем после преобразования
            float length = (float) Math.sqrt(
                    transformed.x * transformed.x +
                            transformed.y * transformed.y +
                            transformed.z * transformed.z);
            if (length > 0) {
                transformed.x /= length;
                transformed.y /= length;
                transformed.z /= length;
            }

            transformedNormals.add(new Vector3f(transformed.x, transformed.y, transformed.z));
        }

        // Рендерим каждый полигон
        for (Polygon polygon : model.polygons) {
            TriangleRasterizer.renderTriangle(
                    gc,
                    zBuffer,
                    model,
                    polygon,
                    transformedVertices,
                    transformedNormals,
                    model.textureVertices,
                    modelColor,
                    currentTexture,
                    currentLight,
                    renderMode
            );
        }

        // Если режим только сетка - рисуем поверх
        if (renderMode == RenderModes.WIREFRAME) {
            renderWireframe(gc, camera, model, width, height);
        }
    }

    private static void renderWireframe(
            final GraphicsContext gc,
            final Camera camera,
            final Model model,
            final int width,
            final int height) {

        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            final int nVertices = vertexIndices.size();

            ArrayList<Point2f> points = new ArrayList<>();
            for (int i = 0; i < nVertices; i++) {
                Vector3f vertex = model.vertices.get(vertexIndices.get(i));
                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(
                        vertex.x, vertex.y, vertex.z);
                Point2f point = GraphicConveyor.vertexToPoint(
                        GraphicConveyor.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath),
                        width, height);
                points.add(point);
            }

            // Рисуем линии полигона
            for (int i = 0; i < nVertices; i++) {
                Point2f current = points.get(i);
                Point2f next = points.get((i + 1) % nVertices);
                gc.strokeLine(current.x, current.y, next.x, next.y);
            }
        }
    }
}