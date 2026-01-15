package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Simple3DViewer extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cgvsu/fxml/gui.fxml"));
        Parent root = loader.load();

        GuiController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle("3D Viewer - Minimal Version");
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}