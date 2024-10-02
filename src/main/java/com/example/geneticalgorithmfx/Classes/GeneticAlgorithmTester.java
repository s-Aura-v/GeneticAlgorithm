package com.example.geneticalgorithmfx.Classes;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GeneticAlgorithmTester {
    public static int NUMBER_OF_FACILITIES = 2;
    private static int FACILITY_DIMENSION = 21;
    private static int NUMBER_OF_PEOPLE = 62;
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

    private static int calculateAffinity(Station[][] floorPlan) {
        int affinity = 0;
        HashSet<Integer> completedIDs = new HashSet<>();

        for (int i = 1; i < FACILITY_DIMENSION - 1; i++) {
            for (int j = 1; j < FACILITY_DIMENSION - 1; j++) {
                if (floorPlan[i][j] == null) continue;
                if (completedIDs.contains(floorPlan[i][j].id)) continue;

                affinity = calculateIndividualAffinity(floorPlan, i, j, completedIDs);
            }
        }
        return (affinity);
    }

    /**
     * Helper for calculate affinity
     * @param floorPlan    - the 2d array of stations
     * @param x            - the xValue of the station
     * @param y            - the yValue of the station
     * @param completedIDs - set to make sure the affinity doesn't count itself multiple times (since they can take multiple spaces).
     */
    private static int calculateIndividualAffinity(Station[][] floorPlan, int x, int y, HashSet<Integer> completedIDs) {
        int affinity = 0;
        completedIDs.add(floorPlan[x][y].id);
        HashSet<Integer> completedIDsForNeighbors = new HashSet<>();

        for (int radius = 1; radius < AFFINITY_RADIUS; radius++) {
            // Check the boundaries and avoid out-of-bounds access
            if (x + radius < floorPlan.length) {
                affinity += processStation(floorPlan, x + radius, y, floorPlan[x][y], completedIDsForNeighbors);
            }
            if (x - radius >= 0) {
                affinity += processStation(floorPlan, x - radius, y, floorPlan[x][y], completedIDsForNeighbors);
            }
            if (y + radius < floorPlan[0].length) {
                affinity += processStation(floorPlan, x, y + radius, floorPlan[x][y], completedIDsForNeighbors);
            }
            if (y - radius >= 0) {
                affinity += processStation(floorPlan, x, y - radius, floorPlan[x][y], completedIDsForNeighbors);
            }
            if (x + radius < floorPlan.length && y + radius < floorPlan[0].length) {
                affinity += processStation(floorPlan, x + radius, y + radius, floorPlan[x][y], completedIDsForNeighbors);
            }
            if (x - radius >= 0 && y + radius < floorPlan[0].length) {
                affinity += processStation(floorPlan, x - radius, y + radius, floorPlan[x][y], completedIDsForNeighbors);
            }
            if (x + radius < floorPlan.length && y - radius >= 0) {
                affinity += processStation(floorPlan, x + radius, y - radius, floorPlan[x][y], completedIDsForNeighbors);
            }
            if (x - radius >= 0 && y - radius >= 0) {
                affinity += processStation(floorPlan, x - radius, y - radius, floorPlan[x][y], completedIDsForNeighbors);
            }
        }
        completedIDsForNeighbors.clear();
        return affinity;
    }

    private static int processStation(Station[][] floorPlan, int x, int y, Station currentStation, HashSet<Integer> completedIDsForNeighbors) {
        int zeroCount = 0, oneCount = 0, twoCount = 0, threeCount = 0;
        try {
            if (currentStation.id == floorPlan[x][y].id || completedIDsForNeighbors.contains(floorPlan[x][y].id)) {
                return 0;
            }
            switch (floorPlan[x][y].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[x][y].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[x][y].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[x][y].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[x][y].id);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
        return zeroCount + oneCount + twoCount + threeCount;
    }

}
