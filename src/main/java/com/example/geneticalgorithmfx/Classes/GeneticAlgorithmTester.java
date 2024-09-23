package com.example.geneticalgorithmfx.Classes;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticAlgorithmTester {
    private static int NUMBER_OF_FACILITIES = 2;
    private static int FACILITY_DIMENSION = 8;
    private static int NUMBER_OF_PEOPLE = 5;
    private static int NUMBER_OF_ITERATIONS = 5;


    public static void main(String[] args) {
        Station[] listOfPeople = createPersonList(NUMBER_OF_PEOPLE);
        ArrayList<Facility> listOfFacilities = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_FACILITIES; i++) {
            listOfFacilities.add(new Facility(FACILITY_DIMENSION, listOfPeople, NUMBER_OF_ITERATIONS));
        }
        for (Facility x : listOfFacilities) {
            x.start();
        }
    }

    /**
     * This function creates an array of people, with random functions, so that they may be placed in the facility.
     * There are 4 function types, 0-3, and it is biased so that it produces more numbers of lower value.
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
