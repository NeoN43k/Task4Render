package com.cgvsu.dialogs;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ErrorDialog {

    public static void show(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Стилизация для темной темы
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getScene().getStylesheets().add(
                ErrorDialog.class.getResource("/com/cgvsu/theme/LightTheme.css").toExternalForm()
        );

        alert.showAndWait();
    }
}