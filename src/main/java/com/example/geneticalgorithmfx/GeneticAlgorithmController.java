package com.example.geneticalgorithmfx;

import com.example.geneticalgorithmfx.Classes.Facility;
import com.example.geneticalgorithmfx.Classes.GeneticAlgorithm;
import com.example.geneticalgorithmfx.Classes.Station;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.geneticalgorithmfx.Classes.GeneticAlgorithm.*;


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

    @FXML
    private ListView<Integer> bestFacilityList;

    // Variables to run the code;

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

        });
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