package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import javax.vecmath.*;
import java.util.ArrayList;

public class TriangleRasterizer {

    // НОВЫЙ метод для работы с clip space координатами
    public static void renderTriangleClipSpace(
            GraphicsContext gc,
            ZBuffer zBuffer,
            com.cgvsu.model.Model model,
            Polygon polygon,
            ArrayList<Vector4f> clipSpaceVertices,  // Изменено на Vector4f
            ArrayList<Vector3f> transformedNormals,
            ArrayList<Vector2f> textureCoords,
            Color baseColor,
            Texture texture,
            Light light,
            RenderModes renderMode) {

        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        if (vertexIndices.size() != 3) return;

        // Получаем вершины треугольника в clip space
        Vector4f v0 = clipSpaceVertices.get(vertexIndices.get(0));
        Vector4f v1 = clipSpaceVertices.get(vertexIndices.get(1));
        Vector4f v2 = clipSpaceVertices.get(vertexIndices.get(2));

        // Преобразуем в экранные координаты с перспективным делением
        Point2f p0 = GraphicConveyor.vertexToPoint(v0, zBuffer.getWidth(), zBuffer.getHeight());
        Point2f p1 = GraphicConveyor.vertexToPoint(v1, zBuffer.getWidth(), zBuffer.getHeight());
        Point2f p2 = GraphicConveyor.vertexToPoint(v2, zBuffer.getHeight(), zBuffer.getHeight());

        if (p0 == null || p1 == null || p2 == null) {
            return; // Треугольник не видим
        }

        // Находим ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(p0.x, Math.min(p1.x, p2.x))));
        int maxX = (int) Math.min(zBuffer.getWidth() - 1, Math.ceil(Math.max(p0.x, Math.max(p1.x, p2.x))));
        int minY = (int) Math.max(0, Math.floor(Math.min(p0.y, Math.min(p1.y, p2.y))));
        int maxY = (int) Math.min(zBuffer.getHeight() - 1, Math.ceil(Math.max(p0.y, Math.max(p1.y, p2.y))));

        // Вектора сторон для вычисления барицентрических координат
        float area = edgeFunction(p0, p1, p2);
        if (Math.abs(area) < 0.0001f) return;

        PixelWriter pixelWriter = gc.getPixelWriter();
        float invArea = 1.0f / area;

        // Вычисляем глубины после перспективного деления для каждой вершины
        float z0 = v0.z / v0.w;
        float z1 = v1.z / v1.w;
        float z2 = v2.z / v2.w;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x + 0.5f, y + 0.5f);

                // Вычисляем барицентрические координаты
                float w0 = edgeFunction(p1, p2, p);
                float w1 = edgeFunction(p2, p0, p);
                float w2 = edgeFunction(p0, p1, p);

                // Если точка внутри треугольника
                if (w0 >= -0.0001f && w1 >= -0.0001f && w2 >= -0.0001f) {
                    // Нормализуем
                    w0 *= invArea;
                    w1 *= invArea;
                    w2 *= invArea;

                    // Интерполируем Z-координату в NDC пространстве
                    float z = w0 * z0 + w1 * z1 + w2 * z2;

                    // Проверяем Z-буфер (в NDC Z от -1 до 1, где -1 - ближе)
                    if (zBuffer.testAndSet(x, y, z)) {
                        // Для вычислений используем исходные позиции вершин
                        Vector3f v0pos = new Vector3f(v0.x / v0.w, v0.y / v0.w, v0.z / v0.w);
                        Vector3f v1pos = new Vector3f(v1.x / v1.w, v1.y / v1.w, v1.z / v1.w);
                        Vector3f v2pos = new Vector3f(v2.x / v2.w, v2.y / v2.w, v2.z / v2.w);

                        Color color = calculatePixelColor(
                                w0, w1, w2,
                                polygon,
                                textureCoords,
                                baseColor,
                                texture,
                                light,
                                transformedNormals,
                                v0pos, v1pos, v2pos,
                                renderMode
                        );

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

    // Старый метод для обратной совместимости
    public static void renderTriangle(
            GraphicsContext gc,
            ZBuffer zBuffer,
            com.cgvsu.model.Model model,
            Polygon polygon,
            ArrayList<Vector3f> transformedVertices,
            ArrayList<Vector3f> transformedNormals,
            ArrayList<Vector2f> textureCoords,
            Color baseColor,
            Texture texture,
            Light light,
            RenderModes renderMode) {

        // Конвертируем Vector3f в Vector4f для нового метода
        ArrayList<Vector4f> clipSpaceVertices = new ArrayList<>();
        for (Vector3f vertex : transformedVertices) {
            // Предполагаем, что transformedVertices уже в clip space с w=1
            clipSpaceVertices.add(new Vector4f(vertex.x, vertex.y, vertex.z, 1.0f));
        }

        renderTriangleClipSpace(
                gc, zBuffer, model, polygon,
                clipSpaceVertices, transformedNormals,
                textureCoords, baseColor,
                texture, light, renderMode
        );
    }

    private static Color calculatePixelColor(
            float w0, float w1, float w2,
            Polygon polygon,
            ArrayList<Vector2f> textureCoords,
            Color baseColor,
            Texture texture,
            Light light,
            ArrayList<Vector3f> transformedNormals,
            Vector3f v0, Vector3f v1, Vector3f v2,
            RenderModes renderMode) {

        Color color = baseColor;

        // Если есть текстура и режим текстурирования
        if (texture != null && texture.isLoaded() &&
                (renderMode == RenderModes.TEXTURED ||
                        renderMode == RenderModes.LIT_TEXTURED ||
                        renderMode == RenderModes.FULL)) {

            ArrayList<Integer> texIndices = polygon.getTextureVertexIndices();
            if (!texIndices.isEmpty() && texIndices.size() >= 3) {
                Vector2f tex0 = textureCoords.get(texIndices.get(0));
                Vector2f tex1 = textureCoords.get(texIndices.get(1));
                Vector2f tex2 = textureCoords.get(texIndices.get(2));

                // Перспективная коррекция текстурных координат
                float z0 = v0.z;
                float z1 = v1.z;
                float z2 = v2.z;

                // Защита от деления на ноль
                if (Math.abs(z0) < 0.0001f) z0 = 0.0001f;
                if (Math.abs(z1) < 0.0001f) z1 = 0.0001f;
                if (Math.abs(z2) < 0.0001f) z2 = 0.0001f;

                // Интерполируем с учетом перспективы
                float u = (w0 * tex0.x / z0 + w1 * tex1.x / z1 + w2 * tex2.x / z2) /
                        (w0 / z0 + w1 / z1 + w2 / z2);
                float v = (w0 * tex0.y / z0 + w1 * tex1.y / z1 + w2 * tex2.y / z2) /
                        (w0 / z0 + w1 / z1 + w2 / z2);

                color = texture.getColor(u, v);
            }
        }

        // Если есть освещение
        if (light != null &&
                (renderMode == RenderModes.LIT_SOLID ||
                        renderMode == RenderModes.LIT_TEXTURED ||
                        renderMode == RenderModes.FULL)) {

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

                // Позиция пикселя (интерполированная)
                float px = w0 * v0.x + w1 * v1.x + w2 * v2.x;
                float py = w0 * v0.y + w1 * v1.y + w2 * v2.y;
                float pz = w0 * v0.z + w1 * v1.z + w2 * v2.z;

                Vector3f lightDir = new Vector3f(
                        lightPos.x - px,
                        lightPos.y - py,
                        lightPos.z - pz
                );

                // Нормализуем направление к свету
                length = (float) Math.sqrt(
                        lightDir.x * lightDir.x +
                                lightDir.y * lightDir.y +
                                lightDir.z * lightDir.z
                );
                if (length > 0) {
                    lightDir.x /= length;
                    lightDir.y /= length;
                    lightDir.z /= length;
                }

                // Скалярное произведение (косинус угла)
                float dot = Math.max(0, nx * lightDir.x + ny * lightDir.y + nz * lightDir.z);

                // Применяем освещение
                float ambient = 0.2f; // Фоновое освещение
                float diffuse = dot * light.getIntensity();

                float lightFactor = Math.min(1.0f, ambient + diffuse);

                color = Color.color(
                        Math.min(1.0, color.getRed() * lightFactor),
                        Math.min(1.0, color.getGreen() * lightFactor),
                        Math.min(1.0, color.getBlue() * lightFactor)
                );
            }
        }

        return color;
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