package com.cgvsu.scene;

import com.cgvsu.model.Model;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Model> selectedModels = new ArrayList<>();
    private String name = "Untitled Scene";

    // === Геттеры и сеттеры ===
    public List<Model> getModels() { return new ArrayList<>(models); }
    public void addModel(Model model) { models.add(model); }
    public void removeModel(Model model) { models.remove(model); }
    public void clearModels() { models.clear(); }

    public List<Model> getSelectedModels() { return new ArrayList<>(selectedModels); }
    public void selectModel(Model model) {
        selectedModels.clear();
        selectedModels.add(model);
    }
    public void addToSelection(Model model) {
        if (!selectedModels.contains(model)) {
            selectedModels.add(model);
        }
    }
    public void clearSelection() { selectedModels.clear(); }

    public Model getActiveModel() {
        return selectedModels.isEmpty() ? null : selectedModels.get(0);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}