package com.cgvsu.components;

import com.cgvsu.model.Model;
import com.cgvsu.scene.SceneManager;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class TransformationPanel extends VBox {

    private SceneManager sceneManager;
    private Slider transX, transY, transZ;
    private Slider rotX, rotY, rotZ;
    private Slider scaleSlider;
    private CheckBox uniformScale;

    public TransformationPanel(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        setMinWidth(250);

        // Перемещение
        TitledPane translationPane = new TitledPane("Перемещение", createTranslationPanel());
        translationPane.setExpanded(false);

        // Вращение
        TitledPane rotationPane = new TitledPane("Вращение", createRotationPanel());
        rotationPane.setExpanded(false);

        // Масштаб
        TitledPane scalePane = new TitledPane("Масштаб", createScalePanel());
        scalePane.setExpanded(false);

        getChildren().addAll(translationPane, rotationPane, scalePane);
    }

    private VBox createTranslationPanel() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));

        HBox transXBox = createSliderBox("X:", -10, 10, 0);
        HBox transYBox = createSliderBox("Y:", -10, 10, 0);
        HBox transZBox = createSliderBox("Z:", -10, 10, 0);

        // Получаем слайдеры из HBox
        transX = (Slider) transXBox.getChildren().get(1);
        transY = (Slider) transYBox.getChildren().get(1);
        transZ = (Slider) transZBox.getChildren().get(1);

        Button applyBtn = new Button("Применить");
        applyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setOnAction(e -> applyTranslation());

        box.getChildren().addAll(transXBox, transYBox, transZBox, applyBtn);
        return box;
    }

    private VBox createRotationPanel() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));

        HBox rotXBox = createSliderBox("X (град):", -180, 180, 0);
        HBox rotYBox = createSliderBox("Y (град):", -180, 180, 0);
        HBox rotZBox = createSliderBox("Z (град):", -180, 180, 0);

        // Получаем слайдеры из HBox
        rotX = (Slider) rotXBox.getChildren().get(1);
        rotY = (Slider) rotYBox.getChildren().get(1);
        rotZ = (Slider) rotZBox.getChildren().get(1);

        Button applyBtn = new Button("Применить");
        applyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setOnAction(e -> applyRotation());

        box.getChildren().addAll(rotXBox, rotYBox, rotZBox, applyBtn);
        return box;
    }

    private VBox createScalePanel() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));

        HBox scaleBox = createSliderBox("Масштаб:", 0.1, 5, 1);

        // Получаем слайдер из HBox
        scaleSlider = (Slider) scaleBox.getChildren().get(1);

        uniformScale = new CheckBox("Сохранять пропорции");
        uniformScale.setSelected(true);

        Button applyBtn = new Button("Применить");
        applyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setOnAction(e -> applyScale());

        box.getChildren().addAll(scaleBox, uniformScale, applyBtn);
        return box;
    }

    private HBox createSliderBox(String label, double min, double max, double value) {
        HBox hbox = new HBox(5);
        Label lbl = new Label(label);
        lbl.setMinWidth(50);

        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 4);
        slider.setMinWidth(150);

        hbox.getChildren().addAll(lbl, slider);
        return hbox;
    }

    private void setupEventHandlers() {
        // Обновляем слайдеры при выборе модели
        // Используем InvalidationListener вместо ListChangeListener
        sceneManager.getModels().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateSliders();
            }
        });
    }

    public void updateSliders() {
        if (!sceneManager.getSelectedModels().isEmpty()) {
            Model model = sceneManager.getSelectedModels().get(0).getModel();
            com.cgvsu.math.Vector3f pos = model.getPosition();
            com.cgvsu.math.Vector3f rot = model.getRotation();
            com.cgvsu.math.Vector3f scale = model.getScale();

            if (transX != null) transX.setValue(pos.x);
            if (transY != null) transY.setValue(pos.y);
            if (transZ != null) transZ.setValue(pos.z);

            if (rotX != null) rotX.setValue(Math.toDegrees(rot.x));
            if (rotY != null) rotY.setValue(Math.toDegrees(rot.y));
            if (rotZ != null) rotZ.setValue(Math.toDegrees(rot.z));

            if (scaleSlider != null) scaleSlider.setValue(scale.x);
        }
    }

    private void applyTranslation() {
        for (SceneManager.SceneModel sceneModel : sceneManager.getSelectedModels()) {
            Model model = sceneModel.getModel();
            model.setPosition(
                    (float) transX.getValue(),
                    (float) transY.getValue(),
                    (float) transZ.getValue()
            );
        }
    }

    private void applyRotation() {
        for (SceneManager.SceneModel sceneModel : sceneManager.getSelectedModels()) {
            Model model = sceneModel.getModel();
            model.setRotation(
                    (float) Math.toRadians(rotX.getValue()),
                    (float) Math.toRadians(rotY.getValue()),
                    (float) Math.toRadians(rotZ.getValue())
            );
        }
    }

    private void applyScale() {
        float scaleValue = (float) scaleSlider.getValue();
        for (SceneManager.SceneModel sceneModel : sceneManager.getSelectedModels()) {
            Model model = sceneModel.getModel();
            if (uniformScale.isSelected()) {
                model.setScale(scaleValue, scaleValue, scaleValue);
            } else {
                model.setScale(scaleValue, scaleValue, scaleValue);
            }
        }
    }
}