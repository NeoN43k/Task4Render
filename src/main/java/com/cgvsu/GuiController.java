package com.cgvsu;

import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.*;
import com.cgvsu.scene.SceneManager;
import com.cgvsu.dialogs.ErrorDialog;
import com.cgvsu.dialogs.SaveOptionsDialog;
import com.cgvsu.model.Model;
import com.cgvsu.components.TransformationPanel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    private CheckMenuItem wireframeMenuItem;

    @FXML
    private CheckMenuItem textureMenuItem;

    @FXML
    private CheckMenuItem lightingMenuItem;

    @FXML
    private MenuItem addCameraMenuItem;

    @FXML
    private MenuItem removeCameraMenuItem;

    @FXML
    private MenuItem toggleCameraVisibilityItem;

    @FXML
    private AnchorPane viewportPane;

    @FXML
    private Canvas viewportCanvas;

    @FXML
    private TreeView<String> modelTreeView;

    @FXML
    private TreeView<String> cameraTreeView;

    @FXML
    private VBox transformationPanelContainer;

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

    @FXML
    private Label cameraCountLabel;

    @FXML
    private ComboBox<String> renderModeComboBox;

    @FXML
    private ColorPicker modelColorPicker;

    @FXML
    private Button loadTextureButton;

    @FXML
    private CheckBox showCamerasCheckBox;

    private SceneManager sceneManager;
    private Stage primaryStage;
    private ToggleGroup selectionModeGroup;
    private Texture currentTexture;
    private boolean showCameras = false;
    private TransformationPanel transformationPanel;

    // Для управления мышкой
    private double lastMouseX, lastMouseY;
    private boolean isMousePressed = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneManager = new SceneManager();
        selectionModeGroup = new ToggleGroup();

        setupUI();
        setupEventHandlers();
        setupRenderControls();
        setupMouseControls();
    }

    private void setupUI() {
        // Настройка группы переключателей
        selectModelButton.setToggleGroup(selectionModeGroup);
        selectVertexButton.setToggleGroup(selectionModeGroup);
        selectPolygonButton.setToggleGroup(selectionModeGroup);
        selectModelButton.setSelected(true);

        // Инициализация TreeView для моделей
        setupModelTreeView();

        // Инициализация TreeView для камер
        setupCameraTreeView();

        // Создаем и добавляем панель трансформаций
        transformationPanel = new TransformationPanel(sceneManager);
        transformationPanelContainer.getChildren().add(transformationPanel);

        // Слушатель изменений в списке моделей
        sceneManager.getModels().addListener((ListChangeListener<SceneManager.SceneModel>) c -> {
            updateModelTreeView();
            updateModelCount();
        });

        // Слушатель для камер
        showCamerasCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showCameras = newVal;
            renderScene();
        });
    }

    private void setupModelTreeView() {
        TreeItem<String> rootItem = new TreeItem<>("Модели сцены");
        rootItem.setExpanded(true);
        modelTreeView.setRoot(rootItem);
        modelTreeView.setShowRoot(true);

        modelTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getParent() != null) {
                if (newVal.getParent().getValue().equals("Модели сцены")) {
                    int index = modelTreeView.getRoot().getChildren().indexOf(newVal);
                    if (index >= 0) {
                        if (selectModelButton.isSelected()) {
                            sceneManager.selectModel(index);
                            transformationPanel.updateSliders();
                        }
                    }
                }
            }
        });
    }

    private void setupCameraTreeView() {
        TreeItem<String> rootItem = new TreeItem<>("Камеры");
        rootItem.setExpanded(true);
        cameraTreeView.setRoot(rootItem);
        cameraTreeView.setShowRoot(true);

        cameraTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getParent() != null) {
                if (newVal.getParent().getValue().equals("Камеры")) {
                    int index = cameraTreeView.getRoot().getChildren().indexOf(newVal);
                    if (index >= 0) {
                        sceneManager.setCurrentCamera(index);
                        renderScene();
                    }
                }
            }
        });
    }

    private void updateModelTreeView() {
        TreeItem<String> root = modelTreeView.getRoot();
        root.getChildren().clear();

        for (int i = 0; i < sceneManager.getModels().size(); i++) {
            SceneManager.SceneModel sceneModel = sceneManager.getModels().get(i);
            TreeItem<String> modelItem = new TreeItem<>(sceneModel.getName());

            // Добавляем информацию о модели
            TreeItem<String> verticesItem = new TreeItem<>("Вершин: " + sceneModel.getModel().getVertexCount());
            TreeItem<String> polygonsItem = new TreeItem<>("Полигонов: " + sceneModel.getModel().getPolygonCount());
            TreeItem<String> visibilityItem = new TreeItem<>("Видима: " + (sceneModel.isVisible() ? "Да" : "Нет"));

            modelItem.getChildren().addAll(verticesItem, polygonsItem, visibilityItem);
            root.getChildren().add(modelItem);
        }
    }

    private void updateCameraTreeView() {
        TreeItem<String> root = cameraTreeView.getRoot();
        root.getChildren().clear();

        for (int i = 0; i < sceneManager.getCameraCount(); i++) {
            String cameraName = "Камера " + (i + 1);
            if (i == sceneManager.getCurrentCameraIndex()) {
                cameraName += " (активная)";
            }
            TreeItem<String> cameraItem = new TreeItem<>(cameraName);
            root.getChildren().add(cameraItem);
        }
    }

    private void setupRenderControls() {
        // Инициализация ColorPicker - проверка на null
        if (modelColorPicker != null) {
            modelColorPicker.setValue(Color.LIGHTGRAY);
            modelColorPicker.setOnAction(event -> {
                RenderEngine.setModelColor(modelColorPicker.getValue());
                renderScene();
            });
        }

        // Инициализация ComboBox с режимами рендеринга
        if (renderModeComboBox != null) {
            renderModeComboBox.getItems().addAll(
                    "Только сетка",
                    "Заливка цветом",
                    "Только текстура",
                    "Освещение + цвет",
                    "Освещение + текстура",
                    "Все эффекты"
            );
            renderModeComboBox.setValue("Заливка цветом");
            renderModeComboBox.setOnAction(event -> {
                updateRenderModeFromComboBox();
            });
        }

        // Инициализация CheckMenuItems
        if (wireframeMenuItem != null) {
            wireframeMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateRenderModeFromCheckboxes();
            });
        }

        if (textureMenuItem != null) {
            textureMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateRenderModeFromCheckboxes();
            });
        }

        if (lightingMenuItem != null) {
            lightingMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateRenderModeFromCheckboxes();
            });
        }

        // Кнопка загрузки текстуры
        if (loadTextureButton != null) {
            loadTextureButton.setOnAction(event -> loadTexture());
        }

        // Управление камерами
        if (addCameraMenuItem != null) {
            addCameraMenuItem.setOnAction(event -> addCamera());
        }

        if (removeCameraMenuItem != null) {
            removeCameraMenuItem.setOnAction(event -> removeCamera());
        }

        if (toggleCameraVisibilityItem != null) {
            toggleCameraVisibilityItem.setOnAction(event -> toggleCameraVisibility());
        }

        // Инициализация состояния рендеринга
        RenderEngine.setRenderMode(RenderModes.SOLID_COLOR);
        RenderEngine.setModelColor(Color.LIGHTGRAY);
    }

    private void setupMouseControls() {
        viewportCanvas.setOnMousePressed(event -> {
            isMousePressed = true;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        viewportCanvas.setOnMouseReleased(event -> {
            isMousePressed = false;
        });

        viewportCanvas.setOnMouseDragged(event -> {
            if (isMousePressed) {
                double deltaX = event.getX() - lastMouseX;
                double deltaY = event.getY() - lastMouseY;
                lastMouseX = event.getX();
                lastMouseY = event.getY();

                Camera camera = sceneManager.getCurrentCamera();

                // Левый клик + drag = вращение
                if (event.isPrimaryButtonDown()) {
                    camera.rotate((float) deltaX * 0.5f, (float) deltaY * 0.5f);
                }
                // Правый клик + drag = панорамирование
                else if (event.isSecondaryButtonDown()) {
                    camera.pan((float) deltaX * 0.01f, (float) deltaY * -0.01f);
                }
                // Средний клик/колесо + drag = приближение/отдаление
                else if (event.isMiddleButtonDown()) {
                    camera.zoom((float) deltaY * 0.1f);
                }

                renderScene();
            }
        });

        viewportCanvas.setOnScroll(event -> {
            Camera camera = sceneManager.getCurrentCamera();
            camera.zoom((float) event.getDeltaY() * 0.1f);
            renderScene();
        });
    }

    private void setupEventHandlers() {
        openMenuItem.setOnAction(event -> openModel());
        saveMenuItem.setOnAction(event -> saveModel());
        exitMenuItem.setOnAction(event -> System.exit(0));

        darkThemeMenuItem.setOnAction(event -> toggleTheme());

        deleteSelectedButton.setOnAction(event -> deleteSelectedElements());
    }

    private void updateRenderModeFromComboBox() {
        String selected = renderModeComboBox.getValue();
        if (selected != null) {
            switch (selected) {
                case "Только сетка":
                    RenderEngine.setRenderMode(RenderModes.WIREFRAME);
                    wireframeMenuItem.setSelected(true);
                    textureMenuItem.setSelected(false);
                    lightingMenuItem.setSelected(false);
                    break;
                case "Заливка цветом":
                    RenderEngine.setRenderMode(RenderModes.SOLID_COLOR);
                    wireframeMenuItem.setSelected(false);
                    textureMenuItem.setSelected(false);
                    lightingMenuItem.setSelected(false);
                    break;
                case "Только текстура":
                    RenderEngine.setRenderMode(RenderModes.TEXTURED);
                    wireframeMenuItem.setSelected(false);
                    textureMenuItem.setSelected(true);
                    lightingMenuItem.setSelected(false);
                    break;
                case "Освещение + цвет":
                    RenderEngine.setRenderMode(RenderModes.LIT_SOLID);
                    wireframeMenuItem.setSelected(false);
                    textureMenuItem.setSelected(false);
                    lightingMenuItem.setSelected(true);
                    break;
                case "Освещение + текстура":
                    RenderEngine.setRenderMode(RenderModes.LIT_TEXTURED);
                    wireframeMenuItem.setSelected(false);
                    textureMenuItem.setSelected(true);
                    lightingMenuItem.setSelected(true);
                    break;
                case "Все эффекты":
                    RenderEngine.setRenderMode(RenderModes.FULL);
                    wireframeMenuItem.setSelected(true);
                    textureMenuItem.setSelected(true);
                    lightingMenuItem.setSelected(true);
                    break;
            }
            renderScene();
        }
    }

    private void updateRenderModeFromCheckboxes() {
        boolean wireframe = wireframeMenuItem.isSelected();
        boolean texture = textureMenuItem.isSelected();
        boolean lighting = lightingMenuItem.isSelected();

        if (wireframe && texture && lighting) {
            RenderEngine.setRenderMode(RenderModes.FULL);
            renderModeComboBox.setValue("Все эффекты");
        } else if (wireframe && !texture && !lighting) {
            RenderEngine.setRenderMode(RenderModes.WIREFRAME);
            renderModeComboBox.setValue("Только сетка");
        } else if (!wireframe && texture && !lighting) {
            RenderEngine.setRenderMode(RenderModes.TEXTURED);
            renderModeComboBox.setValue("Только текстура");
        } else if (!wireframe && !texture && lighting) {
            RenderEngine.setRenderMode(RenderModes.LIT_SOLID);
            renderModeComboBox.setValue("Освещение + цвет");
        } else if (!wireframe && texture && lighting) {
            RenderEngine.setRenderMode(RenderModes.LIT_TEXTURED);
            renderModeComboBox.setValue("Освещение + текстура");
        } else {
            RenderEngine.setRenderMode(RenderModes.SOLID_COLOR);
            renderModeComboBox.setValue("Заливка цветом");
        }

        renderScene();
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
            applyFallbackTheme();
        }
    }

    private void applyFallbackTheme() {
        if (darkThemeMenuItem.isSelected()) {
            mainPane.setStyle("-fx-background-color: #2b2b2b;");
            menuBar.setStyle("-fx-background-color: #3c3c3c;");
        } else {
            mainPane.setStyle("-fx-background-color: #f5f5f5;");
            menuBar.setStyle("-fx-background-color: #ffffff;");
        }
    }

    @FXML
    private void loadTexture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать текстуру");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                currentTexture = new Texture(file.getAbsolutePath());
                RenderEngine.setTexture(currentTexture);
                textureMenuItem.setSelected(true);
                updateRenderModeFromCheckboxes();
                renderScene();
            } catch (Exception e) {
                ErrorDialog.show("Ошибка загрузки текстуры", e.getMessage());
            }
        }
    }

    @FXML
    private void addCamera() {
        sceneManager.addCamera();
        updateCameraTreeView();
        updateCameraCount();
        renderScene();
    }

    @FXML
    private void removeCamera() {
        if (sceneManager.getCameraCount() > 1) {
            sceneManager.removeCurrentCamera();
            updateCameraTreeView();
            updateCameraCount();
            renderScene();
        } else {
            ErrorDialog.show("Ошибка", "Нельзя удалить последнюю камеру");
        }
    }

    @FXML
    private void toggleCameraVisibility() {
        showCameras = !showCameras;
        showCamerasCheckBox.setSelected(showCameras);
        renderScene();
    }

    @FXML
    private void deleteSelectedElements() {
        String mode = getSelectionMode();

        switch (mode) {
            case "MODEL":
                // Удалить выбранные модели
                ArrayList<Integer> indicesToRemove = new ArrayList<>(sceneManager.getSelectedIndices());
                indicesToRemove.sort((a, b) -> b - a); // Удаляем с конца
                for (int index : indicesToRemove) {
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

    private void updateModelCount() {
        modelCountLabel.setText("Моделей: " + sceneManager.getModels().size());
    }

    private void updateCameraCount() {
        cameraCountLabel.setText("Камер: " + sceneManager.getCameraCount());
        updateCameraTreeView();
    }

    private void renderScene() {
        if (viewportCanvas != null && sceneManager != null) {
            // Обновляем визуализацию камер
            if (showCameras) {
                sceneManager.updateCameraModels();
            }

            // Рендерим сцену
            RenderEngine.renderScene(viewportCanvas, sceneManager.getCurrentCamera(), sceneManager);
        }
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
}