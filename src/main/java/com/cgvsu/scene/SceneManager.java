package com.cgvsu.scene;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class SceneManager {

    public static class SceneModel {
        private Model model;
        private String name;
        private boolean visible;

        public SceneModel(Model model, String name) {
            this.model = model;
            this.name = name;
            this.visible = true;
        }

        public Model getModel() {
            return model;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }

    private ObservableList<SceneModel> models = FXCollections.observableArrayList();
    private List<Integer> selectedModelIndices = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();
    private int currentCameraIndex = 0;
    private List<Model> cameraModels = new ArrayList<>();

    public SceneManager() {
        // Добавляем камеру по умолчанию
        Camera defaultCamera = new Camera();
        cameras.add(defaultCamera);

        // Создаем модель для визуализации камеры по умолчанию
        Model cameraModel = createCameraModel(defaultCamera, "Camera 1");
        cameraModels.add(cameraModel);
    }

    private Model createCameraModel(Camera camera, String name) {
        // Создаем простую пирамиду для визуализации камеры
        Model model = new Model();
        model.setName(name);

        // Получаем параметры камеры
        Vector3f cameraPos = camera.getPosition();
        Vector3f cameraTarget = camera.getTarget();

        // Вычисляем направление камеры
        com.cgvsu.math.Vector3f direction = new com.cgvsu.math.Vector3f(
                cameraTarget.x - cameraPos.x,
                cameraTarget.y - cameraPos.y,
                cameraTarget.z - cameraPos.z
        );

        // Нормализуем направление
        float length = (float) Math.sqrt(direction.x * direction.x +
                direction.y * direction.y +
                direction.z * direction.z);
        if (length > 0) {
            direction.x /= length;
            direction.y /= length;
            direction.z /= length;
        }

        // Вершины пирамиды камеры (масштабируем для лучшей видимости)
        float scale = 0.5f;
        com.cgvsu.math.Vector3f[] pyramidVertices = {
                // Вершина пирамиды (перед камеры)
                new com.cgvsu.math.Vector3f(cameraPos.x, cameraPos.y, cameraPos.z),
                // Базовые вершины (смещены назад от камеры)
                new com.cgvsu.math.Vector3f(
                        cameraPos.x - scale + direction.x * 2,
                        cameraPos.y - scale + direction.y * 2,
                        cameraPos.z - scale + direction.z * 2
                ),
                new com.cgvsu.math.Vector3f(
                        cameraPos.x + scale + direction.x * 2,
                        cameraPos.y - scale + direction.y * 2,
                        cameraPos.z + scale + direction.z * 2
                ),
                new com.cgvsu.math.Vector3f(
                        cameraPos.x + scale + direction.x * 2,
                        cameraPos.y + scale + direction.y * 2,
                        cameraPos.z + scale + direction.z * 2
                ),
                new com.cgvsu.math.Vector3f(
                        cameraPos.x - scale + direction.x * 2,
                        cameraPos.y + scale + direction.y * 2,
                        cameraPos.z - scale + direction.z * 2
                )
        };

        // Добавляем вершины
        for (com.cgvsu.math.Vector3f vertex : pyramidVertices) {
            model.vertices.add(vertex);
        }

        // Создаем полигоны для пирамиды
        com.cgvsu.model.Polygon poly;

        // Боковые грани
        poly = new com.cgvsu.model.Polygon();
        poly.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 1, 2)));
        model.polygons.add(poly);

        poly = new com.cgvsu.model.Polygon();
        poly.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 2, 3)));
        model.polygons.add(poly);

        poly = new com.cgvsu.model.Polygon();
        poly.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 3, 4)));
        model.polygons.add(poly);

        poly = new com.cgvsu.model.Polygon();
        poly.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(0, 4, 1)));
        model.polygons.add(poly);

        // Основание (дальняя плоскость)
        poly = new com.cgvsu.model.Polygon();
        poly.setVertexIndices(new ArrayList<>(java.util.Arrays.asList(1, 2, 3, 4)));
        model.polygons.add(poly);

        // Рассчитываем нормали
        model.calculateNormals();

        return model;
    }

    // === Методы для работы с моделями ===

    public void addModel(Model model, String name) {
        models.add(new SceneModel(model, name));
    }

    public void removeModel(int index) {
        if (index >= 0 && index < models.size()) {
            models.remove(index);
            // Обновляем индексы выбранных моделей
            selectedModelIndices.removeIf(i -> i == index);
            for (int i = 0; i < selectedModelIndices.size(); i++) {
                if (selectedModelIndices.get(i) > index) {
                    selectedModelIndices.set(i, selectedModelIndices.get(i) - 1);
                }
            }
        }
    }

    public void selectModel(int index) {
        if (index >= 0 && index < models.size()) {
            selectedModelIndices.clear();
            selectedModelIndices.add(index);
        }
    }

    public void selectModelAdditive(int index) {
        if (index >= 0 && index < models.size() && !selectedModelIndices.contains(index)) {
            selectedModelIndices.add(index);
        }
    }

    public void clearSelection() {
        selectedModelIndices.clear();
    }

    public ObservableList<SceneModel> getModels() {
        return models;
    }

    public List<SceneModel> getSelectedModels() {
        List<SceneModel> selected = new ArrayList<>();
        for (int index : selectedModelIndices) {
            if (index >= 0 && index < models.size()) {
                selected.add(models.get(index));
            }
        }
        return selected;
    }

    public List<Integer> getSelectedIndices() {
        return new ArrayList<>(selectedModelIndices);
    }

    // === Методы для работы с камерами ===

    public void addCamera() {
        Camera newCamera = new Camera();
        cameras.add(newCamera);

        // Создаем модель для новой камеры
        Model cameraModel = createCameraModel(newCamera, "Camera " + (cameras.size()));
        cameraModels.add(cameraModel);

        // Переключаемся на новую камеру
        currentCameraIndex = cameras.size() - 1;
    }

    public void removeCurrentCamera() {
        if (cameras.size() > 1) {
            cameras.remove(currentCameraIndex);
            cameraModels.remove(currentCameraIndex);

            if (currentCameraIndex >= cameras.size()) {
                currentCameraIndex = cameras.size() - 1;
            }
        }
    }

    public Camera getCurrentCamera() {
        if (cameras.isEmpty()) {
            return new Camera();
        }
        return cameras.get(currentCameraIndex);
    }

    public void setCurrentCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            currentCameraIndex = index;
        }
    }

    public List<Camera> getCameras() {
        return new ArrayList<>(cameras);
    }

    public List<Model> getCameraModels() {
        return new ArrayList<>(cameraModels);
    }

    public int getCurrentCameraIndex() {
        return currentCameraIndex;
    }

    public int getCameraCount() {
        return cameras.size();
    }

    // === Методы для обновления визуализации камер ===

    public void updateCameraModels() {
        cameraModels.clear();
        for (int i = 0; i < cameras.size(); i++) {
            Camera camera = cameras.get(i);
            Model cameraModel = createCameraModel(camera, "Camera " + (i + 1));
            cameraModels.add(cameraModel);
        }
    }
}