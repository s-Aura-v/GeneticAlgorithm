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
    private static int AFFINITY_RADIUS = 5;
    //Global Variable that stores the best solutions
    public static HashMap<Integer, Station[][]> bestSolutionsPool = new HashMap<>();
    public static HashMap<Integer, Station[][]> rebuiltSolutionsPool = new HashMap<>();

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
        dividedSolutionPools = recreateNewFacilities(dividedSolutionPools);
        calculateNewAffinities(dividedSolutionPools);
    }

    public static HashMap<Integer, Station[][]> getRebuiltSolutionsPool() {
        return rebuiltSolutionsPool;
    }

    /**
     * Create new factories based on the best solutions which have been divided into halves.
     * Add left (even index) with right (odd index) factories;
     * @param dividedSolutionPools
     */
    public static ArrayList<Station[][]> recreateNewFacilities(ArrayList<Station[][]> dividedSolutionPools) {
        int midpoint = (FACILITY_DIMENSION / 2);
        int rightHalf = midpoint + 1;
        ArrayList<Station[][]> recreatedSolutionPools = new ArrayList<>();
        for (int i = 0; i < dividedSolutionPools.size(); i++) {
            Set<Integer> listOfIDS = flatten(dividedSolutionPools.get(i));
            for (int j = 0; j < dividedSolutionPools.size(); j++) {
                Set<Integer> candidate = flatten(dividedSolutionPools.get(j));
                boolean hasCommonElement = listOfIDS.stream().anyMatch(candidate::contains);
                if (!hasCommonElement) {
                    Station[][] mergedFloorPlan = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
                    if ((i + j)%2 != 0) {
                        for (int x = 0; x < midpoint; x++) {
                            for (int y = 0; y < FACILITY_DIMENSION; y++) {
                                if (i%2 == 0) {
                                    mergedFloorPlan[x][y] = dividedSolutionPools.get(i)[x][y];
                                    mergedFloorPlan[x+rightHalf][y] = dividedSolutionPools.get(j)[x+rightHalf][y];
                                } else {
                                    mergedFloorPlan[x][y] = dividedSolutionPools.get(j)[x][y];
                                    mergedFloorPlan[x+rightHalf][y] = dividedSolutionPools.get(i)[x+rightHalf][y];
                                }
                            }
                        }

                        recreatedSolutionPools.add(mergedFloorPlan);
                    }
                }
            }
        }
        return recreatedSolutionPools;
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
     * All left quadrants are going inside even index and vice versa.
     * @return ArrayList of Stations' Halves
     */
    public static ArrayList<Station[][]> divideIntoHalves() {
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

    /*

     */
    public static void calculateNewAffinities(ArrayList<Station[][]> stations) {
        for (int i = 0; i < stations.size(); i++) {
            rebuiltSolutionsPool.put(calculateAffinity(stations.get(i)), stations.get(i));
        }
        System.out.println(rebuiltSolutionsPool);
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

    /**
     * Calculates total affinity for the entire floor plan
     * @param floorPlan - the 2d array holding the stations
     * @return affinity - the total affinity between the stations in the floor plan
     */
    private static int calculateAffinity(Station[][] floorPlan) {
        int affinity = 0;

        for (int i = 1; i < FACILITY_DIMENSION - 1; i++) {
            for (int j = 1; j < FACILITY_DIMENSION - 1; j++) {
                if (floorPlan[i][j] == null) continue;
                int[] functions = calculateIndividualAffinity(floorPlan, i, j);
                for (int function : functions) {
                    switch (function) {
                        case 0: if (floorPlan[i][j].function == 3) { affinity++; break; }
                        case 1: if (floorPlan[i][j].function == 2) { affinity++; break; }
                        case 2: if (floorPlan[i][j].function == 1) { affinity++; break; }
                        case 3: if (floorPlan[i][j].function == 0) { affinity++; break; }
                    }
                }
            }
        }
        return (affinity);
    }

    /**
     * Function Counts - Stores the amount of times the function appears in the radius.
     * @param floorPlan
     * @param x
     * @param y
     * @return
     */
    private static int[] calculateIndividualAffinity(Station[][] floorPlan, int x, int y) {
        int radius = 2;
        int[] functionCounts = new int[4];
        HashSet<Integer> completedIDs = new HashSet<>();

        // Perfect Situation: If all space is available within the bounds
        if (calculateTopLeftCoordinate(x, y, radius)) {
            for (int i = x - radius; i < x + radius; i++) {
                for (int j = y - radius; j < y + radius; j++) {
                    // Skip if cell is null or function already counted
                    if (floorPlan[i][j] == null || completedIDs.contains(floorPlan[i][j].id)) continue;

                    // Count functions and add to completedIDs
                    switch (floorPlan[i][j].function) {
                        case 0: functionCounts[0]++; completedIDs.add(floorPlan[i][j].id); break;
                        case 1: functionCounts[1]++; completedIDs.add(floorPlan[i][j].id); break;
                        case 2: functionCounts[2]++; completedIDs.add(floorPlan[i][j].id); break;
                        case 3: functionCounts[3]++; completedIDs.add(floorPlan[i][j].id); break;
                    }
                }
            }
            completedIDs.clear();
            return functionCounts;
        }

        // Situation 2: Radius goes out of bounds
        int startX = Math.max(x - radius, 0);
        int endX = Math.min(x + radius, floorPlan.length - 1);
        int startY = Math.max(y - radius, 0);
        int endY = Math.min(y + radius, floorPlan[0].length - 1);

        // Iterate through the valid coordinates within bounds
        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if (floorPlan[i][j] == null || completedIDs.contains(floorPlan[i][j].id)) continue;
                switch (floorPlan[i][j].function) {
                    case 0: functionCounts[0]++; completedIDs.add(floorPlan[i][j].id); break;
                    case 1: functionCounts[1]++; completedIDs.add(floorPlan[i][j].id); break;
                    case 2: functionCounts[2]++; completedIDs.add(floorPlan[i][j].id); break;
                    case 3: functionCounts[3]++; completedIDs.add(floorPlan[i][j].id); break;
                }
            }
        }
        completedIDs.clear();
        return functionCounts;
    }

    /**
     * Calculate if the top-left coordinate is within bounds
     */
    private static boolean calculateTopLeftCoordinate(int x, int y, int radius) {
        return (x - radius >= 0 && y - radius >= 0);
    }

    }
