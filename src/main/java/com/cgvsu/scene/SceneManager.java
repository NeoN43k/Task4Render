package com.cgvsu.scene;

import com.cgvsu.model.Model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private final ObservableList<SceneModel> models = FXCollections.observableArrayList();
    private final List<Integer> selectedModelIndices = new ArrayList<>();

    public static class SceneModel {
        private final Model model;
        private final String name;
        private boolean visible = true;
        private boolean wireframe = false;

        public SceneModel(Model model, String name) {
            this.model = model;
            this.name = name;
        }

        // Геттеры и сеттеры
        public Model getModel() { return model; }
        public String getName() { return name; }
        public boolean isVisible() { return visible; }
        public void setVisible(boolean visible) { this.visible = visible; }
        public boolean isWireframe() { return wireframe; }
        public void setWireframe(boolean wireframe) { this.wireframe = wireframe; }
    }

    public void addModel(Model model, String name) {
        models.add(new SceneModel(model, name));
    }

    public void removeModel(int index) {
        if (index >= 0 && index < models.size()) {
            models.remove(index);
            selectedModelIndices.remove(Integer.valueOf(index));
        }
    }

    public ObservableList<SceneModel> getModels() {
        return models;
    }

    public List<SceneModel> getVisibleModels() {
        return models.stream()
                .filter(SceneModel::isVisible)
                .toList();
    }

    public void selectModel(int index) {
        if (!selectedModelIndices.contains(index)) {
            selectedModelIndices.add(index);
        }
    }

    public void deselectModel(int index) {
        selectedModelIndices.remove(Integer.valueOf(index));
    }

    public void clearSelection() {
        selectedModelIndices.clear();
    }

    public List<Integer> getSelectedIndices() {
        return new ArrayList<>(selectedModelIndices);
    }

    public List<SceneModel> getSelectedModels() {
        return selectedModelIndices.stream()
                .map(index -> models.get(index))
                .toList();
    }

    public boolean isModelSelected(int index) {
        return selectedModelIndices.contains(index);
    }
}