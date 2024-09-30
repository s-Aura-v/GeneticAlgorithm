package com.example.geneticalgorithmfx.Classes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;

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

        System.out.println(bestSolutionsPool);
        ArrayList<Station[][]> dividedSolutionPools = divideIntoHalves();
        recreateNewFacilities(dividedSolutionPools);

    }

    private static void recreateNewFacilities(ArrayList<Station[][]> dividedSolutionPools) {
    }

    /**
     * Divides the solution hashmap into halves so that it can be reassembled to create a new facility
     * @return ArrayList of Stations' Halves
     */
    private static ArrayList<Station[][]> divideIntoHalves() {
        ArrayList<Station[][]> dividedSolutionPools = new ArrayList<>();
        int midpoint = (FACILITY_DIMENSION/2);
        int rightHalf = midpoint + 1;
        // Add Left Half into the Collection of Halves
        for (Station[][] stations: bestSolutionsPool.values() ) {
            Station[][] stationLeftHalf = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
            Station[][] stationRightHalf = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
            for (int i = 0; i < midpoint; i++) {
                for (int j = 0; j < FACILITY_DIMENSION; j++) {
                    stationLeftHalf[i][j] = stations[i][j];
                    stationRightHalf[i+rightHalf][j] = stations[i+rightHalf][j];
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


    // FOR TESTING PURPOSES, WE'RE GONNA ASSUME THE FACILITY IS ALWAYS ODD.
    // FOR TESTING PURPOSES, WE'RE GONNA ASSUME THE FACILITY IS ALWAYS ODD.
    // FOR TESTING PURPOSES, WE'RE GONNA ASSUME THE FACILITY IS ALWAYS ODD.
//    synchronized void addToGlobalBestSolutions() {
//        // Get the best solution (full facility grid)
//        Station[][] solution = getBestSolution();
//        ArrayList<Station[][]> listOfQuadrants = new ArrayList<>();
//        solutionPoolQuadrants.add(solution); // add full solution to pool
//        Station[][] quadrant = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
//
//        // For odd-numbered quadrants, the facility is naturally separated by a barrier
//        int mid = FACILITY_DIMENSION / 2;
//
//        // Process the left quadrant
//        for (int i = 0; i <= mid; i++) {
//            for (int j = 0; j <= mid; j++) {
//                quadrant[i][j] = solution[i][j];
//            }
//        }
//        solutionPoolQuadrants.add(quadrant);
//
//        // Process the right quadrant
//        quadrant = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
//        for (int i = mid + 1; i < FACILITY_DIMENSION; i++) {
//            for (int j = mid + 1; i < FACILITY_DIMENSION; i++) {
//                quadrant[i][j] = solution[i][j];
//            }
//        }
//        solutionPoolQuadrants.add(quadrant);
//
//        createPooledFacility();
//    }
//
//
//    synchronized void createPooledFacility() {
//        HashMap<Integer, Station[][]> pooledSolutions = new HashMap<>();
//        for (int i = 0; i < solutionPoolQuadrants.size(); i++) {
//            for (int j = 0; j < solutionPoolQuadrants.size(); j++) {
//                if (contains(solutionPoolQuadrants.get(i), solutionPoolQuadrants.get(j))) {
//                    break;
//                } else {
//                    System.out.println("WORKS!");
//                    Station[][] merged = merge(solutionPoolQuadrants.get(i), solutionPoolQuadrants.get(j));
//                    System.out.println(calculateAffinity(merged));
//                }
//            }
//        }
//    }
//
//    /**
//     * Check if the two quadrants have overlapping values.
//     */
//    synchronized boolean contains(Station[][] station1, Station[][] station2) {
//        try {
//            for (Station[] row1 : station1) {
//                for (Station value1 : row1) {
//                    for (Station[] row2 : station2) {
//                        for (Station value2 : row2) {
//                            if (value1.getId() == value2.getId()) {
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
//            return false;
//        } catch (NullPointerException e) {
//        }
//        return false;
//    }
//
//    /**
//     * Combine two quadrants.
//     */
//    synchronized Station[][] merge(Station[][] quadrant1, Station[][] quadrant2) {
//        int leftQuadrant = FACILITY_DIMENSION / 2 - 1;
//        int rightQuadrant = leftQuadrant + 2;
//        Station[][] merged = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
//
//        // Copy quadrant1 into the left half of the merged array
//        for (int i = 0; i < leftQuadrant; i++) {
//            for (int j = 0; j < FACILITY_DIMENSION; j++) {
//                merged[i][j] = quadrant1[i][j];
//            }
//        }
//
//        // Copy quadrant2 into the right half of the merged array
//        for (int i = rightQuadrant; i < FACILITY_DIMENSION; i++) {
//            for (int j = rightQuadrant; j < FACILITY_DIMENSION; j++) {
//                merged[i][j] = quadrant2[i][j]; // Offset by the width of quadrant1
//            }
//        }
//        return merged;
//    }
//
//    /**
//     * Flatten the 2d array of stations in floor plan into a 1d array of ids so I can use them to determine if they are capable of fitting.
//     */
//    synchronized static int[] flatten(Station[][] arr) {
//        return Arrays.stream(arr) // Stream of MyClass[]
//                .flatMap(Arrays::stream) // Flatten MyClass[][] to MyClass[]
//                .mapToInt(Station::getId) // Map MyClass to int using getValue()
//                .toArray(); // Collect as an int[] array
//    }
//
//    synchronized Station[][] getBestSolution() {
//        if (bestSolutionsPool.isEmpty()) {
//            return null;
//        }
//        int highestAffinity = bestSolutionsPool.keySet().stream().mapToInt(v -> v).max().orElse(Integer.MIN_VALUE);
//        return bestSolutionsPool.get(highestAffinity);
//    }
}
