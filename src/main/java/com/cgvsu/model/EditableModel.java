package com.cgvsu.model;

import java.util.ArrayList;
import java.util.List;

public class EditableModel extends Model {
    private transient List<Polygon> selectedPolygons = new ArrayList<>();
    private transient List<Integer> selectedVertices = new ArrayList<>();

    public EditableModel() {
        super();
    }

    public void selectPolygon(Polygon polygon) {
        if (!selectedPolygons.contains(polygon)) {
            selectedPolygons.add(polygon);
        }
    }

    public void deselectPolygon(Polygon polygon) {
        selectedPolygons.remove(polygon);
    }

    public void selectVertex(int vertexIndex) {
        if (!selectedVertices.contains(vertexIndex)) {
            selectedVertices.add(vertexIndex);
        }
    }

    public void deselectVertex(int vertexIndex) {
        selectedVertices.remove(Integer.valueOf(vertexIndex));
    }

    public void clearSelection() {
        selectedPolygons.clear();
        selectedVertices.clear();
    }

    public void deleteSelectedVertices() {
        // Удаление выбранных вершин (нужно обновить индексы в полигонах)
        // Это сложная операция, требует перестроения модели
        // В учебных целях можно просто пометить вершины как удаленные
    }

    public void deleteSelectedPolygons() {
        polygons.removeAll(selectedPolygons);
        selectedPolygons.clear();
    }

    public List<Polygon> getSelectedPolygons() {
        return new ArrayList<>(selectedPolygons);
    }

    public List<Integer> getSelectedVertices() {
        return new ArrayList<>(selectedVertices);
    }
}