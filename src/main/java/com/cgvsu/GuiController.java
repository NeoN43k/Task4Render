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
    private ToggleButton selectModelButton;

    @FXML
    private ToggleButton selectVertexButton;

    @FXML
    private ToggleButton selectPolygonButton;

    @FXML
    private Button deleteSelectedButton;

    @FXML
    private Label modelCountLabel;

    private SceneManager sceneManager;
    private Stage primaryStage;
    private ToggleGroup selectionModeGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneManager = new SceneManager();
        selectionModeGroup = new ToggleGroup();

        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        // Настройка группы переключателей
        selectModelButton.setToggleGroup(selectionModeGroup);
        selectVertexButton.setToggleGroup(selectionModeGroup);
        selectPolygonButton.setToggleGroup(selectionModeGroup);
        selectModelButton.setSelected(true);

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

                // Добавляем контекстное меню для модели
                ContextMenu contextMenu = new ContextMenu();
                MenuItem deleteItem = new MenuItem("Удалить");
                deleteItem.setOnAction(e -> sceneManager.removeModel(i));

                MenuItem renameItem = new MenuItem("Переименовать");
                renameItem.setOnAction(e -> renameModel(i));

                contextMenu.getItems().addAll(renameItem, deleteItem);
                modelItem.setContextMenu(contextMenu);

                rootItem.getChildren().add(modelItem);
            }

            // Обновляем счетчик моделей
            updateModelCount();
        });

        // Слушатель выбора в TreeView
        modelTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && modelTreeView.getRoot() != null) {
                int index = modelTreeView.getRoot().getChildren().indexOf(newVal);
                if (index >= 0) {
                    sceneManager.selectModel(index);
                }
            }
        });
    }

    private void setupEventHandlers() {
        openMenuItem.setOnAction(event -> openModel());
        saveMenuItem.setOnAction(event -> saveModel());
        exitMenuItem.setOnAction(event -> System.exit(0));

        darkThemeMenuItem.setOnAction(event -> toggleTheme());

        deleteSelectedButton.setOnAction(event -> deleteSelectedElements());
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

        // Установить имя файла по умолчанию
        SceneManager.SceneModel selectedModel = sceneManager.getSelectedModels().get(0);
        fileChooser.setInitialFileName(selectedModel.getName() + ".obj");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
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
        try {
            if (darkThemeMenuItem.isSelected()) {
                mainPane.getStylesheets().remove(getClass().getResource("/com/cgvsu/theme/LightTheme.css").toExternalForm());
                mainPane.getStylesheets().add(getClass().getResource("/com/cgvsu/theme/DarkTheme.css").toExternalForm());
            } else {
                mainPane.getStylesheets().remove(getClass().getResource("/com/cgvsu/theme/DarkTheme.css").toExternalForm());
                mainPane.getStylesheets().add(getClass().getResource("/com/cgvsu/theme/LightTheme.css").toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при переключении темы: " + e.getMessage());
            // Создаем простые стили программно, если CSS файлы не найдены
            applyFallbackTheme();
        }
    }

    private void applyFallbackTheme() {
        if (darkThemeMenuItem.isSelected()) {
            // Простая темная тема
            mainPane.setStyle("-fx-background-color: #2b2b2b;");
            menuBar.setStyle("-fx-background-color: #3c3c3c;");
        } else {
            // Простая светлая тема
            mainPane.setStyle("-fx-background-color: #f5f5f5;");
            menuBar.setStyle("-fx-background-color: #ffffff;");
        }
    }

    @FXML
    private void deleteSelectedElements() {
        String mode = getSelectionMode();

        switch (mode) {
            case "MODEL":
                // Удалить выбранные модели
                for (int index : sceneManager.getSelectedIndices()) {
                    sceneManager.removeModel(index);
                }
                break;
            case "VERTEX":
                // Удалить выбранные вершины
                // (реализация требует работы с EditableModel)
                break;
            case "POLYGON":
                // Удалить выбранные полигоны
                // (реализация требует работы с EditableModel)
                break;
        }

        renderScene();
        updateModelCount();
    }

    private String getSelectionMode() {
        if (selectVertexButton.isSelected()) {
            return "VERTEX";
        } else if (selectPolygonButton.isSelected()) {
            return "POLYGON";
        } else {
            return "MODEL";
        }
    }

    private void renameModel(int index) {
        TextInputDialog dialog = new TextInputDialog(sceneManager.getModels().get(index).getName());
        dialog.setTitle("Переименовать модель");
        dialog.setHeaderText("Введите новое имя модели:");
        dialog.setContentText("Имя:");

        dialog.showAndWait().ifPresent(newName -> {
            // Здесь нужно обновить имя модели в SceneManager
            // Для этого нужно добавить метод setName в SceneManager.SceneModel
        });
    }

    private void updateModelCount() {
        modelCountLabel.setText("Моделей: " + sceneManager.getModels().size());
    }

    private void renderScene() {
        // Здесь будет вызов рендерера для отрисовки всех видимых моделей
        // (реализуется третьим студентом)
        System.out.println("Рендеринг сцены...");
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
}