package com.cgvsu.render_engine;

import com.cgvsu.managers.GuiController;
import com.cgvsu.model.Model;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class ModelCell extends ListCell<Model> {
    private final HBox hbox = new HBox();
    private final Text modelName = new Text();
    private final Button deleteButton = new Button("Delete");

    public ModelCell(ListView<Model> modelListView, GuiController controller) {
        super();

        hbox.setSpacing(10);
        hbox.getChildren().add(modelName);
        modelName.getStyleClass().add("text");

        deleteButton.setOnAction(event -> {
            Model model = getItem();
            if (model != null) {
                controller.removeModel(model);
                modelListView.getItems().remove(model);
            }
        });
    }

    @Override
    protected void updateItem(Model model, boolean empty) {
        super.updateItem(model, empty);

        if (empty || model == null) {
            setGraphic(null);
        } else {
            modelName.setText("Model " + (getIndex() + 1));
            if (!hbox.getChildren().contains(deleteButton)) {
                hbox.getChildren().add(deleteButton);
            }

            setGraphic(hbox);
        }
    }
}
