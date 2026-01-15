package com.cgvsu;

import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.scene.SceneManager;
import com.cgvsu.dialogs.ErrorDialog;
import com.cgvsu.dialogs.SaveOptionsDialog;
import com.cgvsu.model.Model;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    @FXML
    private BorderPane mainPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem exitMenuItem;

    @FXML
    private CheckMenuItem darkThemeMenuItem;

    @FXML
    private AnchorPane viewportPane;

    @FXML
    private TreeView<String> modelTreeView;

    @FXML
    private VBox transformationPanel;

    @FXML
    private ToggleButton selectVertexButton;

    @FXML
    private ToggleButton selectPolygonButton;

    @FXML
    private Button deleteSelectedButton;

    private SceneManager sceneManager;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneManager = new SceneManager();
        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        // Инициализация TreeView для моделей
        TreeItem<String> rootItem = new TreeItem<>("Сцена");
        rootItem.setExpanded(true);
        modelTreeView.setRoot(rootItem);
        modelTreeView.setShowRoot(false);

        // Слушатель изменений в списке моделей
        sceneManager.getModels().addListener((ListChangeListener<SceneManager.SceneModel>) c -> {
            rootItem.getChildren().clear();
            for (int i = 0; i < sceneManager.getModels().size(); i++) {
                SceneManager.SceneModel sceneModel = sceneManager.getModels().get(i);
                TreeItem<String> modelItem = new TreeItem<>(sceneModel.getName());
                modelItem.setExpanded(true);

                // Добавляем чекбокс для видимости
                CheckBox visibilityCheck = new CheckBox();
                visibilityCheck.setSelected(sceneModel.isVisible());
                visibilityCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    sceneModel.setVisible(newVal);
                    // Обновить отрисовку
                });

                rootItem.getChildren().add(modelItem);
            }
        });
    }

    private void setupEventHandlers() {
        openMenuItem.setOnAction(event -> openModel());
        saveMenuItem.setOnAction(event -> saveModel());
        exitMenuItem.setOnAction(event -> System.exit(0));

        darkThemeMenuItem.setOnAction(event -> toggleTheme());

        deleteSelectedButton.setOnAction(event -> deleteSelectedElements());

        // Множественный выбор в TreeView
        modelTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        modelTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int index = modelTreeView.getRoot().getChildren().indexOf(newVal);
                if (index >= 0) {
                    sceneManager.selectModel(index);
                }
            }
        });
    }

    @FXML
    private void openModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть 3D модель");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                Model model = ObjReader.read(file.getAbsolutePath());
                String modelName = file.getName().replace(".obj", "");
                sceneManager.addModel(model, modelName);

                // Обновить отрисовку
                renderScene();

            } catch (Exception e) {
                ErrorDialog.show("Ошибка загрузки",
                        "Не удалось загрузить модель: " + e.getMessage());
            }
        }
    }

    @FXML
    private void saveModel() {
        if (sceneManager.getSelectedModels().isEmpty()) {
            ErrorDialog.show("Нет выбранных моделей",
                    "Выберите модель для сохранения");
            return;
        }

        SaveOptionsDialog.SaveOptions options = SaveOptionsDialog.show(primaryStage);
        if (options == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить модель");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                SceneManager.SceneModel selectedModel = sceneManager.getSelectedModels().get(0);
                Model modelToSave = selectedModel.getModel();

                if (options.applyTransformations()) {
                    // Здесь применить трансформации к модели
                    // (будет реализовано математиком)
                }

                ObjWriter.write(modelToSave, file.getAbsolutePath());

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Успех");
                successAlert.setHeaderText("Модель сохранена");
                successAlert.setContentText("Файл: " + file.getName());
                successAlert.showAndWait();

            } catch (IOException e) {
                ErrorDialog.show("Ошибка сохранения",
                        "Не удалось сохранить модель: " + e.getMessage());
            }
        }
    }

    @FXML
    private void toggleTheme() {
        if (darkThemeMenuItem.isSelected()) {
            mainPane.getStylesheets().remove("com/cgvsu/theme/LightTheme.css");
            mainPane.getStylesheets().add("com/cgvsu/theme/DarkTheme.css");
        } else {
            mainPane.getStylesheets().remove("com/cgvsu/theme/DarkTheme.css");
            mainPane.getStylesheets().add("com/cgvsu/theme/LightTheme.css");
        }
    }

    @FXML
    private void deleteSelectedElements() {
        if (selectVertexButton.isSelected()) {
            // Удалить выбранные вершины
        } else if (selectPolygonButton.isSelected()) {
            // Удалить выбранные полигоны
        } else {
            // Удалить выбранные модели
            for (int index : sceneManager.getSelectedIndices()) {
                sceneManager.removeModel(index);
            }
        }
        renderScene();
    }

    private void renderScene() {
        // Здесь будет вызов рендерера для отрисовки всех видимых моделей
        // (реализуется третьим студентом)
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
}