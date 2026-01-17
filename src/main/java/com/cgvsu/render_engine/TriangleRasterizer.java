package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import javax.vecmath.*;
import java.util.ArrayList;

public class TriangleRasterizer {

    public static void renderTriangle(
            GraphicsContext gc,
            ZBuffer zBuffer,
            Model model,
            Polygon polygon,
            ArrayList<Vector3f> transformedVertices,
            ArrayList<Vector3f> transformedNormals,
            ArrayList<Vector2f> textureCoords,
            Color baseColor,
            Texture texture,
            Light light,
            RenderModes renderMode) {

        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        if (vertexIndices.size() != 3) return;

        // Получаем вершины треугольника
        Vector3f v0 = transformedVertices.get(vertexIndices.get(0));
        Vector3f v1 = transformedVertices.get(vertexIndices.get(1));
        Vector3f v2 = transformedVertices.get(vertexIndices.get(2));

        // Преобразуем в экранные координаты
        Point2f p0 = GraphicConveyor.vertexToPoint(new Point3f(v0.x, v0.y, v0.z),
                zBuffer.getWidth(), zBuffer.getHeight());
        Point2f p1 = GraphicConveyor.vertexToPoint(new Point3f(v1.x, v1.y, v1.z),
                zBuffer.getWidth(), zBuffer.getHeight());
        Point2f p2 = GraphicConveyor.vertexToPoint(new Point3f(v2.x, v2.y, v2.z),
                zBuffer.getWidth(), zBuffer.getHeight());

        // Находим ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(p0.x, Math.min(p1.x, p2.x))));
        int maxX = (int) Math.min(zBuffer.getWidth() - 1, Math.ceil(Math.max(p0.x, Math.max(p1.x, p2.x))));
        int minY = (int) Math.max(0, Math.floor(Math.min(p0.y, Math.min(p1.y, p2.y))));
        int maxY = (int) Math.min(zBuffer.getHeight() - 1, Math.ceil(Math.max(p0.y, Math.max(p1.y, p2.y))));

        // Вектора сторон для вычисления барицентрических координат
        float area = edgeFunction(p0, p1, p2);
        if (area == 0) return;

        PixelWriter pixelWriter = gc.getPixelWriter();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x + 0.5f, y + 0.5f);

                // Вычисляем барицентрические координаты
                float w0 = edgeFunction(p1, p2, p);
                float w1 = edgeFunction(p2, p0, p);
                float w2 = edgeFunction(p0, p1, p);

                // Если точка внутри треугольника
                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                    // Нормализуем
                    w0 /= area;
                    w1 /= area;
                    w2 /= area;

                    // Интерполируем Z-координату
                    float z = (float) (w0 * v0.z + w1 * v1.z + w2 * v2.z);

                    // Проверяем Z-буфер
                    if (zBuffer.testAndSet(x, y, z)) {
                        // Цвет по умолчанию
                        Color color = baseColor;

                        // Если есть текстура и режим текстурирования
                        if (texture != null && texture.isLoaded() &&
                                (renderMode == RenderModes.TEXTURED ||
                                        renderMode == RenderModes.LIT_TEXTURED ||
                                        renderMode == RenderModes.FULL)) {

                            // Интерполируем текстурные координаты
                            ArrayList<Integer> texIndices = polygon.getTextureVertexIndices();
                            if (!texIndices.isEmpty() && texIndices.size() >= 3) {
                                Vector2f tex0 = textureCoords.get(texIndices.get(0));
                                Vector2f tex1 = textureCoords.get(texIndices.get(1));
                                Vector2f tex2 = textureCoords.get(texIndices.get(2));

                                float u = w0 * tex0.x + w1 * tex1.x + w2 * tex2.x;
                                float v = w0 * tex0.y + w1 * tex1.y + w2 * tex2.y;

                                color = texture.getColor(u, v);
                            }
                        }

                        // Если есть освещение
                        if (light != null &&
                                (renderMode == RenderModes.LIT_SOLID ||
                                        renderMode == RenderModes.LIT_TEXTURED ||
                                        renderMode == RenderModes.FULL)) {

                            // Интерполируем нормаль
                            ArrayList<Integer> normalIndices = polygon.getNormalIndices();
                            if (!normalIndices.isEmpty() && normalIndices.size() >= 3 &&
                                    !transformedNormals.isEmpty()) {

                                Vector3f n0 = transformedNormals.get(normalIndices.get(0));
                                Vector3f n1 = transformedNormals.get(normalIndices.get(1));
                                Vector3f n2 = transformedNormals.get(normalIndices.get(2));

                                // Интерполируем нормаль
                                float nx = w0 * n0.x + w1 * n1.x + w2 * n2.x;
                                float ny = w0 * n0.y + w1 * n1.y + w2 * n2.y;
                                float nz = w0 * n0.z + w1 * n1.z + w2 * n2.z;

                                // Нормализуем
                                float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                                if (length > 0) {
                                    nx /= length;
                                    ny /= length;
                                    nz /= length;
                                }

                                // Вектор к свету
                                Vector3f lightPos = light.getPosition();
                                Vector3f pixelPos = new Vector3f(
                                        w0 * v0.x + w1 * v1.x + w2 * v2.x,
                                        w0 * v0.y + w1 * v1.y + w2 * v2.y,
                                        w0 * v0.z + w1 * v1.z + w2 * v2.z
                                );

                                Vector3f lightDir = new Vector3f(
                                        lightPos.x - pixelPos.x,
                                        lightPos.y - pixelPos.y,
                                        lightPos.z - pixelPos.z
                                );

                                // Нормализуем направление к свету
                                length = (float) Math.sqrt(lightDir.x * lightDir.x +
                                        lightDir.y * lightDir.y +
                                        lightDir.z * lightDir.z);
                                if (length > 0) {
                                    lightDir.x /= length;
                                    lightDir.y /= length;
                                    lightDir.z /= length;
                                }

                                // Скалярное произведение (косинус угла)
                                float dot = Math.max(0, nx * lightDir.x + ny * lightDir.y + nz * lightDir.z);

                                // Применяем освещение
                                float intensity = light.getIntensity();
                                float ambient = 0.2f; // Фоновое освещение
                                float diffuse = dot * intensity;

                                float lightFactor = Math.min(1.0f, ambient + diffuse);

                                color = Color.color(
                                        Math.min(1.0, color.getRed() * lightFactor),
                                        Math.min(1.0, color.getGreen() * lightFactor),
                                        Math.min(1.0, color.getBlue() * lightFactor)
                                );
                            }
                        }

                        // Рисуем пиксель
                        pixelWriter.setColor(x, y, color);
                    }
                }
            }
        }

        // Если нужно рисовать сетку поверх
        if (renderMode == RenderModes.FULL || renderMode == RenderModes.WIREFRAME) {
            drawWireframe(gc, p0, p1, p2, Color.BLACK);
        }
    }

    private static float edgeFunction(Point2f a, Point2f b, Point2f c) {
        return (c.x - a.x) * (b.y - a.y) - (c.y - a.y) * (b.x - a.x);
    }

    private static void drawWireframe(GraphicsContext gc, Point2f p0, Point2f p1, Point2f p2, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(1);

        gc.strokeLine(p0.x, p0.y, p1.x, p1.y);
        gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        gc.strokeLine(p2.x, p2.y, p0.x, p0.y);
    }
}