package com.cgvsu.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelVertexRemover {

    public static void removeVertices(Model model, List<Integer> verticesToRemove) {
        if (model == null || verticesToRemove == null) {
            throw new IllegalArgumentException("Модель и список вершин для удаления не могут быть null.");
        }

        // Удаляет вершины, текстурные координаты и нормали
        removeVerticesFromList(model.vertices, verticesToRemove);
        if (!model.textureVertices.isEmpty()) {
            removeVerticesFromList(model.textureVertices, verticesToRemove);
        }
        if (!model.normals.isEmpty()) {
            removeVerticesFromList(model.normals, verticesToRemove);
        }

        // Обновляет полигоны
        updatePolygons(model.polygons, verticesToRemove);
    }

    // Удаляет элементы из списка по индексам
    private static <T> void removeVerticesFromList(ArrayList<T> list, List<Integer> verticesToRemove) {
        Set<Integer> verticesToRemoveSet = new HashSet<>(verticesToRemove);
        for (int i = list.size() - 1; i >= 0; i--) {
            if (verticesToRemoveSet.contains(i)) {
                list.remove(i);
            }
        }
    }

    private static void updatePolygons(ArrayList<Polygon> polygons, List<Integer> verticesToRemove) {
        Set<Integer> verticesToRemoveSet = new HashSet<>(verticesToRemove);

        for (Polygon polygon : polygons) {
            // Обновляет индексы вершин
            List<Integer> newVertexIndices = updateIndexes(polygon.getVertexIndices(), verticesToRemoveSet);
            polygon.setVertexIndices(new ArrayList<>(newVertexIndices));

            // Обновляет индексы текстурных координат (если есть)
            if (!polygon.getTextureVertexIndices().isEmpty()) {
                List<Integer> newTextureVertexIndices = updateIndexes(polygon.getTextureVertexIndices(), verticesToRemoveSet);
                polygon.setTextureVertexIndices(new ArrayList<>(newTextureVertexIndices));
            }

            // Обновляет индексы нормалей (если есть)
            if (!polygon.getNormalIndices().isEmpty()) {
                List<Integer> newNormalIndices = updateIndexes(polygon.getNormalIndices(), verticesToRemoveSet);
                polygon.setNormalIndices(new ArrayList<>(newNormalIndices));
            }
        }

        // Удаляет полигоны, которые стали некорректными (менее 3 вершин)
        polygons.removeIf(polygon -> polygon.getVertexIndices().size() < 3);
    }

    private static List<Integer> updateIndexes(List<Integer> indexes, Set<Integer> verticesToRemoveSet) {
        List<Integer> newIndices = new ArrayList<>();
        for (int index : indexes) {
            if (!verticesToRemoveSet.contains(index)) {
                // Корректирует индекс, если вершины были удалены
                int newIndex = index - (int) verticesToRemoveSet.stream().filter(i -> i < index).count();
                newIndices.add(newIndex);
            }
        }
        return newIndices;
    }

    // Парсит строку с индексами вершин
    public static List<Integer> parseVertexIndexes(String input) throws NumberFormatException {
        String[] parts = input.split("\\s+");
        List<Integer> indexes = new ArrayList<>();
        for (String part : parts) {
            indexes.add(Integer.parseInt(part));
        }
        return indexes;
    }
}