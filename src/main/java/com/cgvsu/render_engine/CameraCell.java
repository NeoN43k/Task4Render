package com.cgvsu.render_engine;

import com.cgvsu.managers.GuiController;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class CameraCell extends ListCell<Camera> {
    private final HBox hbox = new HBox();
    private final Text cameraName = new Text();
    private final Button deleteButton = new Button("Delete");

    public CameraCell(ListView<Camera> cameraListView, GuiController controller) {
        super();

        hbox.setSpacing(10);
        hbox.getChildren().add(cameraName);
        cameraName.getStyleClass().add("text");
        deleteButton.setOnAction(event -> {
            Camera camera = getItem();
            if (camera != null) {
                controller.removeCamera(camera);
                cameraListView.getItems().remove(camera);
            }
        });
    }

    @Override
    protected void updateItem(Camera camera, boolean empty) {
        super.updateItem(camera, empty);

        if (empty || camera == null) {
            setGraphic(null);
        } else {
            cameraName.setText("Camera " + (getIndex() + 1));
            if (getIndex() > 0) {
                if (!hbox.getChildren().contains(deleteButton)) {
                    hbox.getChildren().add(deleteButton);
                }
            } else {
                hbox.getChildren().remove(deleteButton);
            }

            setGraphic(hbox);
        }
    }
}