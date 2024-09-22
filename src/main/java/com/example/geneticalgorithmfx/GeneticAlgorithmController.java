package com.example.geneticalgorithmfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;


public class GeneticAlgorithmController {
    @FXML
    private AnchorPane background;

    @FXML
    private GridPane detailedGrid;

    @FXML
    private AnchorPane detailedGridContainer;

    @FXML
    private Tab detailedTab;

    @FXML
    private TableView<?> detailedTable;

    @FXML
    private AnchorPane detailedTableContainer;

    @FXML
    private Tab facilitiesContainer;

    public void setDetailedGrid() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                detailedGrid.add(new Button(), i, j);

            }
        }
    }
}