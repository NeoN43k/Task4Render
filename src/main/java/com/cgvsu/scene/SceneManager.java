package com.cgvsu.scene;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.math.Vector3f;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.vecmath.Vector3f as JVector3f;
import java.util.ArrayList;
import java.util.Arrays;
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
        // Добавляем камеру по умолчанию с правильными параметрами
        JVector3f defaultPosition = new JVector3f(0, 0, 0);
        JVector3f defaultTarget = new JVector3f(0, 0, -1);
        Camera defaultCamera = new Camera(
                defaultPosition,
                defaultTarget,
                (float) Math.toRadians(60),  // fov в радианах (60 градусов)
                1.0F,    // aspectRatio
                0.1F,    // nearPlane
                100.0F   // farPlane
        );
        cameras.add(defaultCamera);

        // Создаем модель для визуализации камеры по умолчанию
        Model cameraModel = createCameraModel(defaultCamera, "Camera 1");
        cameraModels.add(cameraModel);
    }

    private Model createCameraModel(Camera camera, String name) {
        Model model = new Model();
        model.setName(name);

        // Получаем параметры камеры
        Vector3f cameraPos = new Vector3f(
                camera.getPosition().x,
                camera.getPosition().y,
                camera.getPosition().z
        );

        Vector3f cameraTarget = new Vector3f(
                camera.getTarget().x,
                camera.getTarget().y,
                camera.getTarget().z
        );

        // Вычисляем направление камеры
        Vector3f direction = new Vector3f(
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

        // Вершины пирамиды камеры
        float scale = 0.2f; // Меньший масштаб для лучшей видимости

        // Создаем вершины пирамиды
        // Вершина пирамиды (перед камеры) - начало направления
        Vector3f pyramidTop = new Vector3f(
                cameraPos.x,
                cameraPos.y,
                cameraPos.z
        );

        // Основание пирамиды (смещено по направлению взгляда)
        Vector3f baseCenter = new Vector3f(
                cameraPos.x + direction.x * 1.5f,
                cameraPos.y + direction.y * 1.5f,
                cameraPos.z + direction.z * 1.5f
        );

        // Векторы для создания основания
        // Вектор "вверх" для камеры
        Vector3f upVector = new Vector3f(0, 1, 0);

        // Вектор "вправо" - перпендикулярно direction и up
        Vector3f rightVector = new Vector3f(
                direction.y * upVector.z - direction.z * upVector.y,
                direction.z * upVector.x - direction.x * upVector.z,
                direction.x * upVector.y - direction.y * upVector.x
        );

        // Нормализуем rightVector
        float rightLength = (float) Math.sqrt(
                rightVector.x * rightVector.x +
                        rightVector.y * rightVector.y +
                        rightVector.z * rightVector.z
        );
        if (rightLength > 0) {
            rightVector.x /= rightLength;
            rightVector.y /= rightLength;
            rightVector.z /= rightLength;
        }

        // Создаем векторы основания
        Vector3f baseUp = new Vector3f(
                upVector.x * scale,
                upVector.y * scale,
                upVector.z * scale
        );

        Vector3f baseRight = new Vector3f(
                rightVector.x * scale,
                rightVector.y * scale,
                rightVector.z * scale
        );

        // 4 вершины основания
        Vector3f[] baseVertices = {
                // Левая нижняя
                new Vector3f(
                        baseCenter.x - baseRight.x - baseUp.x,
                        baseCenter.y - baseRight.y - baseUp.y,
                        baseCenter.z - baseRight.z - baseUp.z
                ),
                // Правая нижняя
                new Vector3f(
                        baseCenter.x + baseRight.x - baseUp.x,
                        baseCenter.y + baseRight.y - baseUp.y,
                        baseCenter.z + baseRight.z - baseUp.z
                ),
                // Правая верхняя
                new Vector3f(
                        baseCenter.x + baseRight.x + baseUp.x,
                        baseCenter.y + baseRight.y + baseUp.y,
                        baseCenter.z + baseRight.z + baseUp.z
                ),
                // Левая верхняя
                new Vector3f(
                        baseCenter.x - baseRight.x + baseUp.x,
                        baseCenter.y - baseRight.y + baseUp.y,
                        baseCenter.z - baseRight.z + baseUp.z
                )
        };

        // Добавляем вершины в модель
        model.vertices.add(pyramidTop); // Индекс 0
        for (Vector3f vertex : baseVertices) {
            model.vertices.add(vertex); // Индексы 1-4
        }

        // Создаем полигоны для пирамиды
        // Боковые грани (треугольники от вершины к основанию)
        for (int i = 0; i < 4; i++) {
            com.cgvsu.model.Polygon poly = new com.cgvsu.model.Polygon();
            int nextIndex = (i + 1) % 4;
            poly.setVertexIndices(new ArrayList<>(Arrays.asList(
                    0,                     // Вершина пирамиды
                    1 + i,                 // Текущая вершина основания
                    1 + nextIndex          // Следующая вершина основания
            )));
            model.polygons.add(poly);
        }

        // Основание пирамиды (квадрат)
        com.cgvsu.model.Polygon basePoly = new com.cgvsu.model.Polygon();
        basePoly.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
        model.polygons.add(basePoly);

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
        // Создаем новую камеру со смещенной позицией
        JVector3f position;
        if (!cameras.isEmpty()) {
            Camera current = cameras.get(currentCameraIndex);
            position = new JVector3f(
                    current.getPosition().x + 2,
                    current.getPosition().y,
                    current.getPosition().z + 2
            );
        } else {
            position = new JVector3f(2, 0, 2);
        }

        JVector3f target = new JVector3f(0, 0, -1);

        Camera newCamera = new Camera(
                position,
                target,
                (float) Math.toRadians(60),  // fov
                1.0F,    // aspectRatio
                0.1F,    // nearPlane
                100.0F   // farPlane
        );
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
            // Создаем камеру по умолчанию
            JVector3f position = new JVector3f(0, 0, 0);
            JVector3f target = new JVector3f(0, 0, -1);
            return new Camera(position, target,
                    (float) Math.toRadians(60), 1.0F, 0.1F, 100.0F);
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