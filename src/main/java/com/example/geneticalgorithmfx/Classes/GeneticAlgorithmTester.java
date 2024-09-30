package com.example.geneticalgorithmfx.Classes;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GeneticAlgorithmTester {
    public static int NUMBER_OF_FACILITIES = 2;
    private static int FACILITY_DIMENSION = 9;
    private static int NUMBER_OF_PEOPLE = 4;
    private static int NUMBER_OF_ITERATIONS = 15;
    //Global Variable that stores the best solutions
    public static HashMap<Integer, Station[][]> bestSolutionsPool = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_FACILITIES);
        Station[] listOfPeople = createPersonList(NUMBER_OF_PEOPLE);
        ArrayList<Facility> listOfFacilities = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_FACILITIES; i++) {
            listOfFacilities.add(new Facility(FACILITY_DIMENSION, listOfPeople, NUMBER_OF_ITERATIONS, latch));
        }
        for (Facility x : listOfFacilities) {
            x.start();
        }

        // wait for all the threads to make their solution pools
        latch.await();

        ArrayList<Station[][]> dividedSolutionPools = divideIntoHalves();
        recreateNewFacilities(dividedSolutionPools);

    }

    private static void recreateNewFacilities(ArrayList<Station[][]> dividedSolutionPools) {
        for (int i = 0; i < dividedSolutionPools.size(); i++) {
            Set<Integer> listOfIDS = flatten(dividedSolutionPools.get(i));
            for (int j = 0; j < dividedSolutionPools.size(); j++) {
                Set<Integer> candidate = flatten(dividedSolutionPools.get(j));
                boolean hasCommonElement = listOfIDS.stream().anyMatch(candidate::contains);
                if (!hasCommonElement) {
                    System.out.println(candidate + " AND " + listOfIDS);
                }
            }
        }
    }

    static Set<Integer> flatten(Station[][] arr) throws NullPointerException {
        return Arrays.stream(arr)
                .filter(Objects::nonNull) // Filter out null rows (Station[])
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull) // Filter out null Station objects
                .map(Station::getId)
                .collect(Collectors.toSet());
    }


    /**
     * Divides the solution hashmap into halves so that it can be reassembled to create a new facility
     *
     * @return ArrayList of Stations' Halves
     */
    private static ArrayList<Station[][]> divideIntoHalves() {
        ArrayList<Station[][]> dividedSolutionPools = new ArrayList<>();
        int midpoint = (FACILITY_DIMENSION / 2);
        int rightHalf = midpoint + 1;
        // Add Left Half into the Collection of Halves
        for (Station[][] stations : bestSolutionsPool.values()) {
            Station[][] stationLeftHalf = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
            Station[][] stationRightHalf = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
            for (int i = 0; i < midpoint; i++) {
                for (int j = 0; j < FACILITY_DIMENSION; j++) {
                    stationLeftHalf[i][j] = stations[i][j];
                    stationRightHalf[i + rightHalf][j] = stations[i + rightHalf][j];
                }
            }
            dividedSolutionPools.add(stationLeftHalf);
            dividedSolutionPools.add(stationRightHalf);
        }
        return dividedSolutionPools;
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
