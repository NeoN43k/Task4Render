package com.cgvsu.managers;

import com.cgvsu.affine_transformations.*;
import com.cgvsu.model.*;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.control.Alert.AlertType;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;

import com.cgvsu.math.vector.Vector3f;

import com.cgvsu.objreader.ObjReader;

import static com.cgvsu.model.ModelVertexRemover.parseVertexIndexes;

public class GuiController {
    private double mousePosX, mousePosY;
    private boolean isLeftDragging = false;
    private boolean isRightDragging = false;
    private final BooleanProperty drawLines = new SimpleBooleanProperty(false);
    private final BooleanProperty drawTexture = new SimpleBooleanProperty(false);
    private final BooleanProperty useLight = new SimpleBooleanProperty(false);
    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    Pane mainPane;

    @FXML
    private Canvas canvas;

    @FXML
    private ToggleButton themeToggleButton;

    @FXML
    private ListView<Camera> cameraListView;

    @FXML
    private ListView<Model> modelListView;

    @FXML
    private TextField vertexIndexesField;

    @FXML
    private Button deleteVertexes;

    @FXML
    private TextField rx, ry, rz; // Поля для вращения
    @FXML
    private TextField sx, sy, sz; // Поля для масштабирования
    @FXML
    private TextField tx, ty, tz; // Поля для перемещения
    @FXML
    private TextField px, py, pz; // Поля для позиции камеры
    @FXML
    private TextField tarx, tary, tarz; // Поля для таргета камеры

    @FXML
    private CheckBox drawLinesCheckBox;

    @FXML
    private CheckBox drawTextureCheckBox;

    @FXML
    private CheckBox useLightCheckBox;

    @FXML
    private ColorPicker colorPicker;

    private boolean isDarkTheme = true;

    private List<Model> models = new ArrayList<>();
    private Model currentModel = null;
    private HashMap<Model,Model> originalModels = new HashMap<>();

    private Camera camera = null;
    private List<Camera> cameras = new ArrayList<>();

    private Timeline timeline;

    @FXML
    private void initialize() {
        Camera mainCamera = new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100
        );
        cameras.add(mainCamera);
        this.camera = mainCamera;

        cameraListView.setCellFactory(param -> new CameraCell(cameraListView, this));
        cameraListView.getItems().addAll(cameras);
        cameraListView.getSelectionModel().selectFirst();
        cameraListView.getSelectionModel().selectedItemProperty().addListener((obs, oldCamera, newCamera) -> {
            if (newCamera != null) {
                this.camera = newCamera;
                timeline.play();
            }
        });

        modelListView.setCellFactory(param -> new ModelCell(modelListView, this));
        modelListView.getItems().addAll(models);
        modelListView.getSelectionModel().selectedItemProperty().addListener((obs, oldModel, newModel) -> {
            if (newModel != null) {
                this.currentModel = newModel;
                timeline.play();
            }
        });

        drawLines.bind(drawLinesCheckBox.selectedProperty());
        drawTexture.bind(drawTextureCheckBox.selectedProperty());
        useLight.bind(useLightCheckBox.selectedProperty());

        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue() - mainPane.getWidth()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(100), event -> renderScene());
        deleteVertexes.setOnAction(event -> handleDeleteVertexes());
        canvas.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        canvas.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                for (File file : dragboard.getFiles()) {
                    if (file.getName().toLowerCase().endsWith(".obj")) {
                        loadModelFromFile(file); // Загружаем модель
                        success = true;
                    } else if (isImageFile(file)) {
                        if (currentModel != null) {
                            loadTextureForModel(currentModel, file); // Загружаем текстуру для текущей модели
                            success = true;
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Сначала выберите модель для загрузки текстуры.");
                        }
                    }
                }
                if (!success) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Поддерживаются только файлы .obj и изображения (.png, .jpg, .jpeg)");
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        themeToggleButton.setOnAction(event -> {
            isDarkTheme = !isDarkTheme;
            setTheme(isDarkTheme);
        });

        setTheme(isDarkTheme);

        timeline.getKeyFrames().add(frame);
        timeline.play();
        updateUI();
    }

    private void renderScene() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

        camera.setAspectRatio((float) (width / height));

        // Инициализация общего Z-буфера для всех моделей
        float[][] ZBuffer = new float[(int) width][(int) height];
        for (int x = 0; x < (int) width; x++) {
            for (int y = 0; y < (int) height; y++) {
                ZBuffer[x][y] = 9999.0F;  // Заполняем Z-буфер максимальными значениями
            }
        }

        // Рендерим все модели с учетом их индивидуальных настроек
        for (Model model : models) {
            Triangulator.triangulateModel(model);
            NormalsCalculation.calculateVertexNormals(model);

            // Получаем настройки для модели
            ModelSettings settings = new ModelSettings(
                    model.isDrawLines(),
                    model.isDrawTexture(),
                    model.isUseLight(),
                    model.getColor(),
                    model.getTexture()
            );

            try {
                // Рендерим каждую модель с использованием индивидуальных настроек
                RenderEngine.render(canvas.getGraphicsContext2D(),
                        camera,
                        model,
                        (int) width,
                        (int) height,
                        settings.getTexture(),
                        settings.isDrawLines(),
                        settings.isDrawTexture(),
                        settings.isUseLight(),
                        settings.getColor(),
                        ZBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при рендеринге модели: " + e.getMessage());
            }
        }
    }






    //Камера и всё что с ней связано
    @FXML
    private void handleAddCamera(ActionEvent event) {
        Camera newCamera = new Camera(
                new Vector3f(Float.parseFloat(px.getText()), Float.parseFloat(py.getText()), Float.parseFloat(pz.getText())),
                new Vector3f(Float.parseFloat(tarx.getText()), Float.parseFloat(tary.getText()), Float.parseFloat(tarz.getText())),
                1.0F, 1, 0.01F, 100
        );
        addCamera(newCamera);
    }

    @FXML
    private void applyCameraParams(ActionEvent event) {
        if (camera == null) {
            showAlert(AlertType.ERROR, "Ошибка", "Камера не выбрана.");
            return;
        }

        try {
            float posX = Float.parseFloat(px.getText());
            float posY = Float.parseFloat(py.getText());
            float posZ = Float.parseFloat(pz.getText());

            float targetX = Float.parseFloat(tarx.getText());
            float targetY = Float.parseFloat(tary.getText());
            float targetZ = Float.parseFloat(tarz.getText());

            camera.setPosition(new Vector3f(posX, posY, posZ));
            camera.setTarget(new Vector3f(targetX, targetY, targetZ));
            timeline.play();
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Ошибка", "Некорректные данные в текстовых полях.");
            e.printStackTrace();
        }
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);
        cameraListView.getItems().add(camera);
    }

    public void removeCamera(Camera camera) {
        if (cameras.size() > 1 && cameras.contains(camera)) {
            cameras.remove(camera);
            cameraListView.getItems().remove(camera);
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION * 10));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION * 10));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION * 5, 0, 0));
        camera.moveTarget(new Vector3f(TRANSLATION * 5, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION * 5, 0, 0));
        camera.moveTarget(new Vector3f(-TRANSLATION * 5, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION * 10, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION * 10, 0));
    }

    @FXML
    public void turnCameraRight(ActionEvent actionEvent) {
        camera.moveTarget(new Vector3f(-TRANSLATION * 5, 0, 0));
    }

    @FXML
    public void turnCameraLeft(ActionEvent actionEvent) {
        camera.moveTarget(new Vector3f(TRANSLATION * 5, 0, 0));
    }

    @FXML
    public void handleMouseDragged(MouseEvent event) {
        if (isLeftDragging) {
            // Перемещение камеры (левая кнопка мыши)
            double deltaX = event.getSceneX() - mousePosX;
            double deltaY = event.getSceneY() - mousePosY;

            float sensitivity = 0.0007f;

            Vector3f target = camera.getTarget();
            Vector3f position = camera.getPosition();
            Vector3f right = new Vector3f(1, 0, 0);

            Vector3f newTarget = GraphicConveyor.rotatePointAroundAxis(target, position, right, (float) -deltaY * sensitivity);
            Vector3f up = new Vector3f(0, 1, 0);
            newTarget = GraphicConveyor.rotatePointAroundAxis(newTarget, position, up, (float) -deltaX * sensitivity);

            camera.setTarget(newTarget);

            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
        } else if (isRightDragging) {
            // Вращение камеры вокруг target (правая кнопка мыши)
            double deltaX = event.getSceneX() - mousePosX;
            double deltaY = event.getSceneY() - mousePosY;

            float sensitivity = 0.0007f;

            Vector3f target = camera.getTarget();
            Vector3f position = camera.getPosition();

            Vector3f up = new Vector3f(0, 1, 0);

            // Вращение вокруг оси Y (горизонтальное движение мыши)
            Vector3f newPosition = GraphicConveyor.rotatePointAroundAxis(position, target, up, (float) -deltaX * sensitivity);

            // Вектор "вправо" (перпендикулярно направлению и вектору "вверх")
            Vector3f direction = new Vector3f(target.getX() - newPosition.getX(), target.getY() - newPosition.getY(),target.getZ() - newPosition.getZ());
            direction.normalize();

            Vector3f right = new Vector3f();
            right.cross(direction, up);
            right.normalize();

            // Вращение вокруг оси X (вертикальное движение мыши)
            newPosition = GraphicConveyor.rotatePointAroundAxis(newPosition, target, right, (float) -deltaY * sensitivity);

            camera.setPosition(newPosition);
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
        }
    }

    @FXML
    public void handleMouseScrolled(ScrollEvent event) {
        double delta = event.getDeltaY(); // Получаем значение прокрутки колеса мыши
        float zoomSpeed = 2.4f; // Чувствительность zoom`а

        Vector3f direction = new Vector3f(
                camera.getTarget().getX() - camera.getPosition().getX(),
                camera.getTarget().getY() - camera.getPosition().getY(),
                camera.getTarget().getZ() - camera.getPosition().getZ()
        );
        direction.normalize();

        if (delta > 0) {
            camera.movePosition(new Vector3f(
                    direction.getX() * zoomSpeed,
                    direction.getY() * zoomSpeed,
                    direction.getZ() * zoomSpeed
            ));
        } else {
            camera.movePosition(new Vector3f(
                    -direction.getX() * zoomSpeed,
                    -direction.getY() * zoomSpeed,
                    -direction.getZ() * zoomSpeed
            ));
        }
    }

    @FXML
    public void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) { // ЛКМ
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            isLeftDragging = true;
        } else if (event.getButton() == MouseButton.SECONDARY) { // ПКМ
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            isRightDragging = true;
        }
    }

    @FXML
    public void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            isLeftDragging = false;
        } else if (event.getButton() == MouseButton.SECONDARY) {
            isRightDragging = false;
        }
    }





    //Модель
    @FXML
    private void handleDeleteVertexes() {
        if (currentModel == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Сначала загрузите модель.");
            return;
        }

        String input = vertexIndexesField.getText().trim();
        if (input.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Введите индексы вершин.");
            return;
        }

        List<Integer> vertexIndexes;
        try {
            vertexIndexes = parseVertexIndexes(input);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректный формат индексов.");
            return;
        }

        List<Integer> missingVertices = new ArrayList<>();
        for (int index : vertexIndexes) {
            if (index < 0 || index >= currentModel.vertices.size()) {
                missingVertices.add(index);
            }
        }

        if (!missingVertices.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Следующие вершины отсутствуют в модели: " + missingVertices);
            return;
        }

        ModelVertexRemover.removeVertices(currentModel, vertexIndexes);
        updateUI();
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String fileContent = Files.readString(file.toPath());
            Model newModel = ObjReader.read(fileContent);
            models.add(newModel);
            originalModels.put(newModel, new Model(newModel));
            modelListView.getItems().add(newModel);
            modelListView.getSelectionModel().select(newModel);
        } catch (IOException exception) {
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке модели: " + exception.getMessage());
        }
    }

    public void removeModel(Model model) {
        models.remove(model); // Удаляем модель из списка
        modelListView.getItems().remove(model); // Удаляем её из ListView
        originalModels.remove(model);
        timeline.play(); // Перерисовываем сцену
    }

    @FXML
    private void applyTransformations() {
        try {
            if (currentModel != null) { // Проверяем текущую выбранную модель
                // Вращение
                float angleX = Float.parseFloat(rx.getText());
                float angleY = Float.parseFloat(ry.getText());
                float angleZ = Float.parseFloat(rz.getText());

                CompositeAffine composite = new CompositeAffine();
                if (angleX != 0) {
                    composite.add(new Rotate(angleX, Axis.X));
                }
                if (angleY != 0) {
                    composite.add(new Rotate(angleY, Axis.Y));
                }
                if (angleZ != 0) {
                    composite.add(new Rotate(angleZ, Axis.Z));
                }
                currentModel.vertices = composite.execute(currentModel.vertices);

                // Масштабирование
                float scaleX = Float.parseFloat(sx.getText());
                float scaleY = Float.parseFloat(sy.getText());
                float scaleZ = Float.parseFloat(sz.getText());

                composite = new CompositeAffine();
                composite.add(new Scale(scaleX, scaleY, scaleZ));
                currentModel.vertices = composite.execute(currentModel.vertices);

                // Перемещение
                float translateX = Float.parseFloat(tx.getText());
                float translateY = Float.parseFloat(ty.getText());
                float translateZ = Float.parseFloat(tz.getText());

                composite = new CompositeAffine();
                composite.add(new Transfer(translateX, translateY, translateZ));
                currentModel.vertices = composite.execute(currentModel.vertices);

                // Обновляем отображение
                timeline.play();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректные данные в одном из полей");
        }
    }





    //CheckBoxes
    @FXML
    private void onDrawLinesCheckBoxAction() {
        if (currentModel != null) {
            currentModel.setDrawLines(drawLinesCheckBox.isSelected());
        }
    }

    @FXML
    private void onDrawTextureCheckBoxAction() {
        if (currentModel != null && currentModel.hasTexture()) {
            currentModel.setDrawTexture(drawTextureCheckBox.isSelected());
        } else {
            showAlert(AlertType.ERROR, "Ошибка", "Пожалуйста загрузите текстуру для выбранной модели");
        }
    }

    @FXML
    private void onUseLightCheckBoxAction() {
        if (currentModel != null) {
            currentModel.setUseLight(useLightCheckBox.isSelected());
        }
    }

    @FXML
    private void onColorPickerAction() {
        if (currentModel != null) {
            currentModel.setColor(colorPicker.getValue());
        }
    }




    //Load
    private void loadTextureForModel(Model model, File file) {
        try {
            BufferedImage texture = ImageIO.read(file); // Загружаем текстуру
            model.setTexture(texture); // Привязываем текстуру к модели
            model.setTextureName(file.getName()); // Сохраняем имя файла текстуры
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Текстура успешно загружена и привязана к модели.");
            timeline.play(); // Обновляем отображение
        } catch (IOException exception) {
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке текстуры: " + exception.getMessage());
        }
    }

    private void loadModelFromFile(File file) {
        try {
            String fileContent = Files.readString(file.toPath());
            Model newModel = ObjReader.read(fileContent); // Загружаем модель
            models.add(newModel); // Добавляем в список моделей
            modelListView.getItems().add(newModel); // Добавляем в интерфейс
            modelListView.getSelectionModel().select(newModel); // Делаем её текущей
            timeline.play(); // Обновляем отображение
        } catch (IOException exception) {
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке файла: " + exception.getMessage());
        }
    }

    @FXML
    private void onLoadTextureMenuItemClick() {
        if (currentModel == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Сначала выберите модель для загрузки текстуры.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Texture");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            BufferedImage texture = ImageIO.read(file); // Загружаем текстуру
            currentModel.setTexture(texture); // Привязываем текстуру к текущей модели
            currentModel.setTextureName(file.getName()); // Сохраняем имя файла текстуры
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Текстура успешно загружена и привязана к модели.");
            timeline.play(); // Обновляем отображение
        } catch (IOException exception) {
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке текстуры: " + exception.getMessage());
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }




    //Save
    @FXML
    private void saveSceneAsSingleFile(ActionEvent event) {
        if (models.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Нет моделей для сохранения.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить сцену в один файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model Files (*.obj)", "*.obj"));

        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String sceneData = ObjWriter.writeModels(models);
            Files.write(file.toPath(), sceneData.getBytes());

            showAlert(Alert.AlertType.INFORMATION, "Успех", "Сцена успешно сохранена в файл: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при сохранении сцены: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveModelMenuItemClick(Model modelToSave) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path filePath = Path.of(file.getAbsolutePath());
        try {
            String modelData = ObjWriter.write(modelToSave);
            Files.write(filePath, modelData.getBytes());
        } catch (IOException exception) {
            exception.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при сохранении модели: " + exception.getMessage());
        }
    }

    @FXML
    private void onSaveOriginalModel(ActionEvent event) {
        if (currentModel == null) { // Проверяем текущую выбранную модель
            showAlert(AlertType.WARNING, "Предупреждение", "Пожалуйста, выберите модель перед сохранением.");
        } else {
            onSaveModelMenuItemClick(originalModels.get(currentModel)); // Сохраняем оригинальную копию текущей модели
        }
    }

    @FXML
    private void onSaveModifiedModel(ActionEvent event) {
        if (currentModel == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Изменённая модель не загружена. Пожалуйста, создайте или загрузите изменённую модель перед сохранением.");
        } else {
            onSaveModelMenuItemClick(currentModel);
        }
    }




    //Прочее
    @FXML
    public void handleCanvasClick(MouseEvent mouseEvent) {
        canvas.requestFocus();
    }

    private void setTheme(boolean isDarkTheme) {
        String theme = isDarkTheme ? "/com/cgvsu/fxml/dark.css" : "/com/cgvsu/fxml/light.css";
        anchorPane.getStylesheets().clear();
        anchorPane.getStylesheets().add(getClass().getResource(theme).toExternalForm());
        themeToggleButton.setText(isDarkTheme ? "Soy theme" : "Dark theme");
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        String theme = isDarkTheme ? "/com/cgvsu/fxml/dark.css" : "/com/cgvsu/fxml/light.css";
        alert.getDialogPane().getStylesheets().clear();
        alert.getDialogPane().getStylesheets().add(getClass().getResource(theme).toExternalForm());
        alert.showAndWait();
    }

    private void updateUI() {
        if (currentModel == null) {
            return;
        }
        drawLinesCheckBox.setSelected(currentModel.isDrawLines());
        drawTextureCheckBox.setSelected(currentModel.isDrawTexture());
        useLightCheckBox.setSelected(currentModel.isUseLight());
        colorPicker.setValue(currentModel.getColor());
    }
}