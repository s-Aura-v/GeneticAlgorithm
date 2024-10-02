package com.example.geneticalgorithmfx;

import com.example.geneticalgorithmfx.Classes.Facility;
import com.example.geneticalgorithmfx.Classes.GeneticAlgorithm;
import com.example.geneticalgorithmfx.Classes.Station;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.geneticalgorithmfx.Classes.GlobalSolutionPool.bestSolutionsPool;
import static com.example.geneticalgorithmfx.Classes.GlobalSolutionPool.rebuiltSolutionsPool;


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
    private Tab facilitiesContainer;

    @FXML
    private TextField numOfFacilityText;

    @FXML
    private TextField facilityDimensionText;

    @FXML
    private TextField numOfStationsText;

    @FXML
    private Button submitConfigButton;

    @FXML
    ListView<Integer> listOfBestSolutions = new ListView<>();

    public void start() {
        submitConfigButton.setOnAction(event -> {
            int numOfFacilities = Integer.parseInt(numOfFacilityText.getText());
            int facilityDimension = Integer.parseInt(facilityDimensionText.getText());
            int numOfStations = Integer.parseInt(facilityDimensionText.getText());
            int NUMBER_OF_ITERATIONS = 15;
            int AFFINITY_RADIUS = 5;
            Station[] listOfStations = createStations(numOfStations);
            CountDownLatch latch = new CountDownLatch(numOfFacilities);
            ArrayList<Facility> listOfFacilities = new ArrayList<>();
            for (int i = 0; i < numOfFacilities; i++) {
                listOfFacilities.add(new Facility(facilityDimension, listOfStations, NUMBER_OF_ITERATIONS, latch));
            }
            // TODO: move it into the previous for loop [ listOfFacilities.get(i).start(); ]
            for (Facility x : listOfFacilities) {
                x.start();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(facilityDimension);
            ArrayList<Station[][]> dividedSolutionPools = geneticAlgorithm.divideIntoHalves();
            dividedSolutionPools = geneticAlgorithm.recreateNewFacilities(dividedSolutionPools);
            geneticAlgorithm.calculateNewAffinities(dividedSolutionPools);

            fillBestFacilityList();
            listOfBestSolutions.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    fillDetailedGridContainer(facilityDimension, listOfBestSolutions.getSelectionModel().getSelectedItem());
                }
            });
        });

    }

    private void fillBestFacilityList() {
        ObservableList<Integer> names = FXCollections.observableArrayList(bestSolutionsPool.keySet());
        listOfBestSolutions.setItems(names);
    }


    private void fillDetailedGridContainer(int facilityDimension, Integer selectedItem) {
        detailedGrid = new GridPane(facilityDimension, facilityDimension);
        detailedGrid.getChildren().removeAll();
        detailedGridContainer.getChildren().clear();
        detailedGrid.setAlignment(Pos.TOP_CENTER);
        detailedGridContainer.getChildren().add(detailedGrid);

        double width = detailedGridContainer.getWidth();
        double height = detailedGridContainer.getHeight();
        int rectangleHeight = (int) ((height / facilityDimension) - 1);
        int rectangleWidth = (int) ((width / facilityDimension) - 1);
        detailedGrid.setHgap(1);
        detailedGrid.setVgap(1);

        Station[][] floorPlan = rebuiltSolutionsPool.get(selectedItem);
        Rectangle blueRectangle = new Rectangle(rectangleWidth, rectangleHeight);
        blueRectangle.setFill(Color.BLUEVIOLET);
        Rectangle redRectangle= new Rectangle(rectangleWidth, rectangleHeight);
        redRectangle.setFill(Color.RED);
        Rectangle greenRectangle = new Rectangle(rectangleWidth, rectangleHeight);
        greenRectangle.setFill(Color.GREEN);
        Rectangle goldRectangle = new Rectangle(rectangleWidth, rectangleHeight);
        goldRectangle.setFill(Color.GOLD);
        for (int x = 0; x < facilityDimension; x++) {
            for (int y = 0; y < facilityDimension; y++) {
                if (floorPlan[x][y] == null) {
                    detailedGrid.add(new Rectangle(rectangleWidth, rectangleHeight), x, y);
                    continue;
                }
                switch (floorPlan[x][y].getFunction()) {
                    case 0: detailedGrid.add(createRectangle(rectangleWidth,rectangleHeight,Color.BLUEVIOLET), x, y); break;
                    case 1: detailedGrid.add(createRectangle(rectangleWidth,rectangleHeight,Color.RED), x, y); break;
                    case 2: detailedGrid.add(createRectangle(rectangleWidth,rectangleHeight,Color.GREEN), x, y); break;
                    case 3: detailedGrid.add(createRectangle(rectangleWidth,rectangleHeight,Color.GOLD), x, y); break;
                    default: detailedGrid.add(new Rectangle(rectangleWidth, rectangleHeight), x, y); break;
                }
            }
        }
    }

    private Rectangle createRectangle(int width, int height, Color color) {
        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setFill(color);
        return rectangle;
    }


    /**
     * This function creates an array of people, with random functions, so that they may be placed in the facility.
     * There are 4 function types, 0-3, and it is biased so that it produces more numbers of lower value.
     *
     * @param numOfStations - insert the number of people that you want created
     * @return Person[] - return an array filled with people of different functions
     */
    static Station[] createStations(int numOfStations) {
        Station[] people = new Station[numOfStations];
        for (int i = 0; i < numOfStations; i++) {
            double randomDouble = ThreadLocalRandom.current().nextDouble();
            double biasedRandomDouble = Math.pow(randomDouble, 2);
            int personFunction = (int) Math.round(biasedRandomDouble * 3);
            people[i] = new Station(personFunction, i);
        }
        return people;
    }
}