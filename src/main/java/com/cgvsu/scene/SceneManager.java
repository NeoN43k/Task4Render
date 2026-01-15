package com.cgvsu.scene;

import com.cgvsu.model.Model;

public class SceneManager {
    private Scene currentScene;

    public SceneManager() {
        this.currentScene = new Scene();
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(Scene scene) {
        this.currentScene = scene;
    }

    public void addModel(Model model) {
        currentScene.addModel(model);
    }

    public void removeModel(Model model) {
        currentScene.removeModel(model);
    }

    public void selectModel(Model model) {
        currentScene.selectModel(model);
    }

    // === Методы для работы с файлами (заглушки) ===
    public void loadSceneFromFile(String filePath) {
        System.out.println("Загрузка сцены из файла: " + filePath);
        // TODO: Реализовать загрузку сцены
    }

    public void saveSceneToFile(String filePath) {
        System.out.println("Сохранение сцены в файл: " + filePath);
        // TODO: Реализовать сохранение сцены
    }
}