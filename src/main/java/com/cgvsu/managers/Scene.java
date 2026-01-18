package com.cgvsu.managers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import com.cgvsu.model.Model;

public class Scene {

    private final List<Model> models;

    public Scene() {
        this.models = new ArrayList<>();
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public void removeModel(Model model) {
        models.remove(model);
    }

    public List<Model> getModels() {
        return models;
    }

    public void setTextureForModel(Model model, BufferedImage texture) {
        if (models.contains(model)) {
            model.setTexture(texture);
        }
    }

    public void removeTextureFromModel(Model model) {
        if (models.contains(model)) {
            model.setTexture(null);
        }
    }

    public Model getActiveModel() {
        return models.isEmpty() ? null : models.get(0);
    }

    public void setActiveModel(Model model) {
        if (models.contains(model)) {
            models.remove(model);
            models.add(0, model);
        }
    }
}
