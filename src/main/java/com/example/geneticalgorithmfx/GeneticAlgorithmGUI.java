package com.example.geneticalgorithmfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GeneticAlgorithmGUI extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GeneticAlgorithmGUI.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Genetic Algorithm");
        stage.setScene(scene);
        stage.show();


        GeneticAlgorithmController controller = fxmlLoader.getController();
        controller.start();
    }
     
    public static void main(String[] args) {
        launch();
    }
}