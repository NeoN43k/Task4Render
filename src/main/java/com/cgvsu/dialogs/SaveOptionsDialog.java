package com.cgvsu.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SaveOptionsDialog {

    public static class SaveOptions {
        private final boolean applyTransformations;

        public SaveOptions(boolean applyTransformations) {
            this.applyTransformations = applyTransformations;
        }

        public boolean applyTransformations() {
            return applyTransformations;
        }
    }

    public static SaveOptions show(Stage owner) {
        Dialog<SaveOptions> dialog = new Dialog<>();
        dialog.setTitle("Опции сохранения");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        CheckBox applyTransformsCheck = new CheckBox("Применить трансформации");
        applyTransformsCheck.setSelected(true);

        grid.add(new Label("Параметры сохранения:"), 0, 0);
        grid.add(applyTransformsCheck, 0, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new SaveOptions(applyTransformsCheck.isSelected());
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}