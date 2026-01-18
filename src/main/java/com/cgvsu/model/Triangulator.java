package com.cgvsu.model;

import java.util.ArrayList;

public class Triangulator {

    public static void triangulateModel(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>();

        for (Polygon polygon : model.polygons) {
            newPolygons.addAll(triangulatePolygon(polygon));
        }

        model.polygons = newPolygons;
    }

    private static ArrayList<Polygon> triangulatePolygon(Polygon polygon) {
        ArrayList<Polygon> triangles = new ArrayList<>();

        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        ArrayList<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();

        if (vertexIndices.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 vertices");
        }

        // Триангуляция по диагонали
        for (int i = 1; i < vertexIndices.size() - 1; i++) {
            Polygon triangle = new Polygon();

            // Вершины
            ArrayList<Integer> triangleVertexIndices = new ArrayList<>();
            triangleVertexIndices.add(vertexIndices.get(0));
            triangleVertexIndices.add(vertexIndices.get(i));
            triangleVertexIndices.add(vertexIndices.get(i + 1));
            triangle.setVertexIndices(triangleVertexIndices);

            // Текстурные вершины
            if (textureVertexIndices != null && !textureVertexIndices.isEmpty()) {
                ArrayList<Integer> triangleTextureVertexIndices = new ArrayList<>();
                triangleTextureVertexIndices.add(textureVertexIndices.get(0));
                triangleTextureVertexIndices.add(textureVertexIndices.get(i));
                triangleTextureVertexIndices.add(textureVertexIndices.get(i + 1));
                triangle.setTextureVertexIndices(triangleTextureVertexIndices);
            }

            // Нормали
            if (normalIndices != null && !normalIndices.isEmpty()) {
                ArrayList<Integer> triangleNormalIndices = new ArrayList<>();
                triangleNormalIndices.add(normalIndices.get(0));
                triangleNormalIndices.add(normalIndices.get(i));
                triangleNormalIndices.add(normalIndices.get(i + 1));
                triangle.setNormalIndices(triangleNormalIndices);
            }

            triangles.add(triangle);
        }

        return triangles;
    }
}