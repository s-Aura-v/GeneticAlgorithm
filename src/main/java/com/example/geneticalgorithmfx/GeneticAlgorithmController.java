package com.example.geneticalgorithmfx;

import com.example.geneticalgorithmfx.Classes.Facility;
import com.example.geneticalgorithmfx.Classes.GeneticAlgorithmTester;
import com.example.geneticalgorithmfx.Classes.Station;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;


public class GeneticAlgorithmController {
    @FXML
    private AnchorPane background;

    @FXML
    private GridPane detailedGrid;

    @FXML
    private GridPane facilitiesOverviewGrid;

    @FXML
    private AnchorPane detailedGridContainer;

    @FXML
    private AnchorPane facilitiesOverviewContainer;

    @FXML
    private Tab detailedTab;

    @FXML
    private TableView<?> detailedTable;

    @FXML
    private AnchorPane detailedTableContainer;

    @FXML
    private Tab facilitiesContainer;

    @FXML
    private TextField numOfFacilityText;

    @FXML
    private TextField facilityDimensionText;

    @FXML
    private TextField numOfStationsText;

    @FXML
    private Button submitConfigButton;

    public void setDetailedGrid(int dimensions, Station[][] floorPlan) {
        detailedGrid = new GridPane(10, 10);
        detailedGrid.getChildren().removeAll();
        detailedGridContainer.getChildren().clear();
        detailedGrid.setAlignment(Pos.TOP_CENTER);
        detailedGridContainer.getChildren().add(detailedGrid);

        double totalHeight = detailedGridContainer.getHeight();
        double totalWidth = detailedGridContainer.getWidth();
        double rectangleWidth = (totalWidth / dimensions) - 1;
        double rectangleHeight = (totalHeight / dimensions) - 1;


        for (int i = 0; i < dimensions; i++) {
            for (int j = 0; j < dimensions; j++) {
                detailedGrid.add(new Rectangle(rectangleWidth, rectangleHeight), i, j);
                detailedGrid.setHgap(1);
                detailedGrid.setVgap(1);
            }
        }
    }

    public void setSubmitConfigButton() {
        int NUMBER_OF_ITERATIONS = 10;
        submitConfigButton.setOnAction(event -> {
            int NUMBER_OF_FACILITIES = Integer.parseInt(numOfFacilityText.getText());
            int FACILITY_DIMENSION = Integer.parseInt(facilityDimensionText.getText());
            int NUMBER_OF_STATIONS = Integer.parseInt(numOfStationsText.getText());
            Station[] listOfPeople = createPersonList(NUMBER_OF_STATIONS);
            ArrayList<Facility> listOfFacilities = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(NUMBER_OF_FACILITIES);

            for (int i = 0; i < NUMBER_OF_FACILITIES; i++) {
                listOfFacilities.add(new Facility(FACILITY_DIMENSION, listOfPeople, NUMBER_OF_ITERATIONS, latch));
            }
            for (Facility x : listOfFacilities) {
                x.start();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            setDetailedGrid(FACILITY_DIMENSION, listOfFacilities.getFirst().getFloorPlan());
            createFacilitiesOverviewGrid();
        });
    }

    private void createFacilitiesOverviewGrid() {
        facilitiesOverviewGrid = new GridPane();
        facilitiesOverviewGrid.setAlignment(Pos.TOP_CENTER);
        facilitiesOverviewGrid.setHgap(1);
        facilitiesOverviewGrid.setVgap(1);
        facilitiesOverviewGrid.setGridLinesVisible(true);

        final int numCols = 9;
        final int numRows = 9;

        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / numCols);
            facilitiesOverviewGrid.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < numRows; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / numRows);
            facilitiesOverviewGrid.getRowConstraints().add(rowConst);
        }

        facilitiesOverviewContainer.getChildren().clear();
        facilitiesOverviewContainer.getChildren().add(facilitiesOverviewGrid);

        for (Station[][] floorPlan : GeneticAlgorithmTester.bestSolutionsPool.values()) {
            int dimensions = 9;
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    facilitiesOverviewGrid.add(createNewCell(dimensions, floorPlan), i, j);
                }
            }
        }
    }

    public AnchorPane createNewCell(int dimensions, Station[][] floorPlan) {
        AnchorPane cell = new AnchorPane();

        // Set preferred size for the AnchorPane
        // TODO: LOOK INTO THIS
        cell.setPrefSize(50, 50);

        GridPane cellGrid = new GridPane();
        cellGrid.setAlignment(Pos.TOP_CENTER);
        cellGrid.setHgap(0);
        cellGrid.setVgap(0);

        // EDIT THIS LATER...
        cellGrid.setPrefSize(cell.getPrefWidth(), cell.getPrefHeight());

        double totalHeight = cellGrid.getPrefHeight();
        double totalWidth = cellGrid.getPrefWidth();
        double rectangleWidth = (totalWidth / dimensions) - 1;
        double rectangleHeight = (totalHeight / dimensions) - 1;

        for (int i = 0; i < dimensions; i++) {
            for (int j = 0; j < dimensions; j++) {
                Rectangle rect = new Rectangle(rectangleWidth, rectangleHeight);
                cellGrid.add(rect, i, j);  // Add rectangles to the grid
            }
        }

        cell.getChildren().add(cellGrid);
        return cell;
    }


    /**
     * This function creates an array of people, with random functions, so that they may be placed in the facility.
     * There are 4 function types, 0-3, and it is biased so that it produces more numbers of lower value.
     *
     * @param numberOfPeople - insert the number of people that you want created
     * @return Person[] - return an array filled with people of different functions
     */
    static Station[] createPersonList(int numberOfPeople) {
        Station[] people = new Station[numberOfPeople];
        for (int i = 0; i < numberOfPeople; i++) {
            double randomDouble = ThreadLocalRandom.current().nextDouble();
            double biasedRandomDouble = Math.pow(randomDouble, 2);
            int personFunction = (int) Math.round(biasedRandomDouble * 3);
            people[i] = new Station(personFunction, i);
        }
        return people;
    }
}