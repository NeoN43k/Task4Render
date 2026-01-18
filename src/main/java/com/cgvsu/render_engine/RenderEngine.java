package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Model;
import com.cgvsu.scene.SceneManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
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

        // Рендерим все видимые модели
        for (SceneManager.SceneModel sceneModel : sceneManager.getModels()) {
            if (sceneModel.isVisible()) {
                Model model = sceneModel.getModel();
                renderModel(gc, camera, model, width, height);
            }
        }

        // Рендерим камеры если включено
        if (sceneManager.getCameraModels() != null && !sceneManager.getCameraModels().isEmpty()) {
            for (Model cameraModel : sceneManager.getCameraModels()) {
                // Камеры рисуем только сеткой
                RenderModes oldMode = renderMode;
                setRenderMode(RenderModes.WIREFRAME);
                renderModel(gc, camera, cameraModel, width, height);
                setRenderMode(oldMode);
            }
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

        // Получаем матрицы
        Matrix4f modelMatrix = model.getTransformMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // M * V * P (для вершин)
        Matrix4f mvpMatrix = projectionMatrix.multiply(viewMatrix.multiply(modelMatrix));

        // M * V (для нормалей)
        Matrix4f mvMatrix = viewMatrix.multiply(modelMatrix);

        // Получаем трансформированные вершины и нормали
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();
        ArrayList<Vector3f> transformedNormals = new ArrayList<>();

        // Трансформируем вершины через M * V * P
        for (Vector3f vertex : model.getVertices()) {
            Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(mvpMatrix, vertex);
            transformedVertices.add(transformed);
        }

        // Трансформируем нормали (только M * V)
        for (Vector3f normal : model.getTransformedNormals()) {
            // Для нормалей используем w=0 (вектор направления)
            Vector4f normal4 = new Vector4f(normal.x, normal.y, normal.z, 0.0f);
            Vector4f transformed4 = mvMatrix.multiply(normal4);
            Vector3f transformedNormal = new Vector3f(
                    transformed4.x, transformed4.y, transformed4.z
            ).normalize();
            transformedNormals.add(transformedNormal);
        }

        // Рендерим каждый полигон
        for (com.cgvsu.model.Polygon polygon : model.getPolygons()) {
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

        Matrix4f modelMatrix = model.getTransformMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f mvpMatrix = projectionMatrix.multiply(viewMatrix.multiply(modelMatrix));

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        for (com.cgvsu.model.Polygon polygon : model.getPolygons()) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            final int nVertices = vertexIndices.size();

            ArrayList<Point2f> points = new ArrayList<>();
            for (int i = 0; i < nVertices; i++) {
                Vector3f vertex = model.getVertices().get(vertexIndices.get(i));
                Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(mvpMatrix, vertex);
                Point2f point = GraphicConveyor.vertexToPoint(transformed, width, height);
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

    // Метод для отрисовки каркаса (старый способ для совместимости)
    public static void renderWireframeOld(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {

        javax.vecmath.Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        javax.vecmath.Matrix4f viewMatrix = camera.getViewMatrixOld();
        javax.vecmath.Matrix4f projectionMatrix = camera.getProjectionMatrixOld();

        javax.vecmath.Matrix4f modelViewProjectionMatrix = new javax.vecmath.Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                // Получаем нашу вершину
                com.cgvsu.math.Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                // Конвертируем в javax.vecmath.Vector3f
                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.x, vertex.y, vertex.z);

                Point2f resultPoint = GraphicConveyor.vertexToPoint(
                        new com.cgvsu.math.Vector3f(
                                GraphicConveyor.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath).x,
                                GraphicConveyor.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath).y,
                                GraphicConveyor.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath).z
                        ),
                        width, height
                );
                resultPoints.add(resultPoint);
            }

            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y);
            }

            if (nVerticesInPolygon > 0)
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y);
        }
    }

    // Вспомогательный метод для получения текущего цвета
    public static Color getModelColor() {
        return modelColor;
    }

    // Вспомогательный метод для получения текущей текстуры
    public static Texture getCurrentTexture() {
        return currentTexture;
    }

    // Вспомогательный метод для получения текущего света
    public static Light getCurrentLight() {
        return currentLight;
    }

    // Вспомогательный метод для получения текущего режима рендеринга
    public static RenderModes getRenderMode() {
        return renderMode;
    }

    // Метод для очистки ресурсов
    public static void cleanup() {
        zBuffer = null;
        currentTexture = null;
        currentLight = null;
    }
}