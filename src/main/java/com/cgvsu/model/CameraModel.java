package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

public class CameraModel extends Model {
    public CameraModel() {
        super();
        setName("Camera");

        // Создаем простую пирамиду для визуализации камеры
        // Вершины пирамиды (фрустума камеры)
        vertices.add(new Vector3f(0, 0, 0));      // 0 - вершина пирамиды
        vertices.add(new Vector3f(-1, -1, -2));   // 1 - левый нижний дальний
        vertices.add(new Vector3f(1, -1, -2));    // 2 - правый нижний дальний
        vertices.add(new Vector3f(1, 1, -2));     // 3 - правый верхний дальний
        vertices.add(new Vector3f(-1, 1, -2));    // 4 - левый верхний дальний

        // Полигоны для отрисовки пирамиды
        Polygon poly;

        // Боковые грани
        poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygons.add(poly);

        poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 2, 3)));
        polygons.add(poly);

        poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 3, 4)));
        polygons.add(poly);

        poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 4, 1)));
        polygons.add(poly);

        // Основание (дальняя плоскость)
        poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
        polygons.add(poly);

        // Рассчитываем нормали
        calculateNormals();
    }
}