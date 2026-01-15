package com.cgvsu;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    // === Левая панель ===
    @FXML private TreeView<String> sceneTree;
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;

    // === Центральная панель ===
    @FXML private Pane renderPane;

    // === Правая панель ===
    @FXML private Slider sliderTranslateX;
    @FXML private TextField fieldTranslateX;
    @FXML private Button btnApplyTranslation;

    // === Меню ===
    @FXML private MenuItem menuOpen;
    @FXML private MenuItem menuSave;
    @FXML private MenuItem menuExit;

    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        initSceneTree();
    }

    private void setupEventHandlers() {
        // === Связывание слайдера и текстового поля ===
        if (sliderTranslateX != null && fieldTranslateX != null) {
            fieldTranslateX.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    double value = Double.parseDouble(newVal);
                    if (value >= sliderTranslateX.getMin() && value <= sliderTranslateX.getMax()) {
                        sliderTranslateX.setValue(value);
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем неверный ввод
                }
            });

            sliderTranslateX.valueProperty().addListener((obs, oldVal, newVal) -> {
                fieldTranslateX.setText(String.format("%.2f", newVal.doubleValue()));
            });
        }

        // === Кнопки ===
        if (btnApplyTranslation != null) {
            btnApplyTranslation.setOnAction(e -> {
                System.out.println("Применить перемещение: " +
                        (fieldTranslateX != null ? fieldTranslateX.getText() : "0.0"));
            });
        }

        if (btnAdd != null) {
            btnAdd.setOnAction(e -> System.out.println("Добавить модель"));
        }

        if (btnDelete != null) {
            btnDelete.setOnAction(e -> System.out.println("Удалить модель"));
        }

        // === Меню ===
        if (menuOpen != null) {
            menuOpen.setOnAction(e -> System.out.println("Меню: Открыть"));
        }

        if (menuSave != null) {
            menuSave.setOnAction(e -> System.out.println("Меню: Сохранить"));
        }

        if (menuExit != null) {
            menuExit.setOnAction(e -> {
                if (primaryStage != null) {
                    primaryStage.close();
                }
            });
        }
    }

    private void initSceneTree() {
        if (sceneTree != null) {
            TreeItem<String> root = new TreeItem<>("Сцена");
            root.setExpanded(true);

            TreeItem<String> models = new TreeItem<>("Модели");
            models.setExpanded(true);

            // Примерные модели для демонстрации
            TreeItem<String> model1 = new TreeItem<>("Куб (cube.obj)");
            TreeItem<String> model2 = new TreeItem<>("Сфера (sphere.obj)");

            models.getChildren().addAll(model1, model2);

            TreeItem<String> cameras = new TreeItem<>("Камеры");
            TreeItem<String> camera1 = new TreeItem<>("Основная камера");
            cameras.getChildren().add(camera1);

            root.getChildren().addAll(models, cameras);
            sceneTree.setRoot(root);

            // Обработка выбора в дереве
            sceneTree.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            System.out.println("Выбрано: " + newVal.getValue());
                        }
                    }
            );
        }
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
}