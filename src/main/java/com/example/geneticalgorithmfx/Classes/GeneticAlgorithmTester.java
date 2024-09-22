package com.example.geneticalgorithmfx.Classes;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticAlgorithmTester {
    public static int NUMBER_OF_FACILITIES = 2;
    public static int FACILITY_DIMENSION = 12;
    public static int NUMBER_OF_PEOPLE = 4;


    public static void main(String[] args) {
        Person[] listOfPeople = createPersonList(NUMBER_OF_PEOPLE);
        ArrayList<Facility> listOfFacilities = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_FACILITIES; i++) {
            listOfFacilities.add(new Facility(FACILITY_DIMENSION, listOfPeople));
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
    static Person[] createPersonList(int numberOfPeople) {
        Person[] people = new Person[numberOfPeople];
        for (int i = 0; i < numberOfPeople; i++) {
            double randomDouble = ThreadLocalRandom.current().nextDouble();
            double biasedRandomDouble = Math.pow(randomDouble, 2);
            int personFunction = (int) Math.round(biasedRandomDouble * 3);
            people[i] = new Person(personFunction, i);
        }
        return people;
    }
}
