package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import java.util.ArrayList;

public class DebugRenderer {

    // Рисует оси координат (X-красный, Y-зеленый, Z-синий)
    public static void drawAxes(GraphicsContext gc, Camera camera,
                                int width, int height, float length) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f vpMatrix = projectionMatrix.multiply(viewMatrix);

        // Центр осей
        Vector4f origin = new Vector4f(0, 0, 0, 1);

        // Концы осей
        Vector4f xAxis = new Vector4f(length, 0, 0, 1);
        Vector4f yAxis = new Vector4f(0, length, 0, 1);
        Vector4f zAxis = new Vector4f(0, 0, length, 1);

        // Преобразуем
        Vector4f oScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, origin);
        Vector4f xScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, xAxis);
        Vector4f yScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, yAxis);
        Vector4f zScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, zAxis);

        // Конвертируем в экранные координаты
        Point2f o = GraphicConveyor.vertexToPoint(oScreen, width, height);
        Point2f x = GraphicConveyor.vertexToPoint(xScreen, width, height);
        Point2f y = GraphicConveyor.vertexToPoint(yScreen, width, height);
        Point2f z = GraphicConveyor.vertexToPoint(zScreen, width, height);

        if (o != null && x != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeLine(o.x, o.y, x.x, x.y);
        }

        if (o != null && y != null) {
            gc.setStroke(Color.GREEN);
            gc.setLineWidth(2);
            gc.strokeLine(o.x, o.y, y.x, y.y);
        }

        if (o != null && z != null) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2);
            gc.strokeLine(o.x, o.y, z.x, z.y);
        }
    }

    // Рисует простой тестовый куб
    public static Model createTestCube() {
        Model cube = new Model();
        cube.setName("Test Cube");

        // Вершины куба от -1 до 1
        float[] vertices = {
                // Задняя грань
                -1, -1, -1,
                1, -1, -1,
                1,  1, -1,
                -1,  1, -1,

                // Передняя грань
                -1, -1,  1,
                1, -1,  1,
                1,  1,  1,
                -1,  1,  1
        };

        // Добавляем вершины
        for (int i = 0; i < vertices.length; i += 3) {
            cube.vertices.add(new Vector3f(
                    vertices[i], vertices[i+1], vertices[i+2]
            ));
        }

        // Создаем полигоны (треугольники)
        // Каждая грань = 2 треугольника
        int[][] triangleIndices = {
                // Задняя грань
                {0, 1, 2}, {0, 2, 3},
                // Передняя грань
                {4, 6, 5}, {4, 7, 6},
                // Левая грань
                {0, 3, 7}, {0, 7, 4},
                // Правая грань
                {1, 5, 6}, {1, 6, 2},
                // Верхняя грань
                {3, 2, 6}, {3, 6, 7},
                // Нижняя грань
                {0, 4, 5}, {0, 5, 1}
        };

        // Создаем полигоны
        for (int[] indices : triangleIndices) {
            Polygon poly = new Polygon();
            ArrayList<Integer> vertexIndices = new ArrayList<>();
            for (int idx : indices) {
                vertexIndices.add(idx);
            }
            poly.setVertexIndices(vertexIndices);
            cube.polygons.add(poly);
        }

        // Рассчитываем нормали
        cube.calculateNormals();

        return cube;
    }

    // Рисует сетку на плоскости XZ
    public static void drawGrid(GraphicsContext gc, Camera camera,
                                int width, int height, int size, float step) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f vpMatrix = projectionMatrix.multiply(viewMatrix);

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);

        // Линии вдоль оси X
        for (float z = -size; z <= size; z += step) {
            for (float x = -size; x < size; x += step) {
                Vector4f start = new Vector4f(x, 0, z, 1);
                Vector4f end = new Vector4f(x + step, 0, z, 1);

                Vector4f startScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, start);
                Vector4f endScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, end);

                Point2f p1 = GraphicConveyor.vertexToPoint(startScreen, width, height);
                Point2f p2 = GraphicConveyor.vertexToPoint(endScreen, width, height);

                if (p1 != null && p2 != null) {
                    gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // Линии вдоль оси Z
        for (float x = -size; x <= size; x += step) {
            for (float z = -size; z < size; z += step) {
                Vector4f start = new Vector4f(x, 0, z, 1);
                Vector4f end = new Vector4f(x, 0, z + step, 1);

                Vector4f startScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, start);
                Vector4f endScreen = GraphicConveyor.multiplyMatrix4ByVector4(vpMatrix, end);

                Point2f p1 = GraphicConveyor.vertexToPoint(startScreen, width, height);
                Point2f p2 = GraphicConveyor.vertexToPoint(endScreen, width, height);

                if (p1 != null && p2 != null) {
                    gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }
}