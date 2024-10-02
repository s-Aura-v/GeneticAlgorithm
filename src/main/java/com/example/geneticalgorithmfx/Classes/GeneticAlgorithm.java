package com.example.geneticalgorithmfx.Classes;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.example.geneticalgorithmfx.Classes.GlobalSolutionPool.bestSolutionsPool;

public class GeneticAlgorithm {
    //Global Variable that stores the best solutions
    public HashMap<Integer, Station[][]> rebuiltSolutionsPool = new HashMap<>();
    private int FACILITY_DIMENSION;

    public GeneticAlgorithm(int numOfFacilities) {
        FACILITY_DIMENSION = numOfFacilities;
    }

    public HashMap<Integer, Station[][]> getRebuiltSolutionsPool() {
        return rebuiltSolutionsPool;
    }

    /**
     * Create new factories based on the best solutions which have been divided into halves.
     * Add left (even index) with right (odd index) factories;
     * @param dividedSolutionPools
     */
    public ArrayList<Station[][]> recreateNewFacilities(ArrayList<Station[][]> dividedSolutionPools) {
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
    public ArrayList<Station[][]> divideIntoHalves() {
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
    public void calculateNewAffinities(ArrayList<Station[][]> stations) {
        for (int i = 0; i < stations.size(); i++) {
            rebuiltSolutionsPool.put(calculateAffinity(stations.get(i)), stations.get(i));
        }
        System.out.println(rebuiltSolutionsPool);
    }

    /**
     * Calculates total affinity for the entire floor plan
     * @param floorPlan - the 2d array holding the stations
     * @return affinity - the total affinity between the stations in the floor plan
     */
    private int calculateAffinity(Station[][] floorPlan) {
        int affinity = 0;

        for (int i = 1; i < FACILITY_DIMENSION - 1; i++) {
            for (int j = 1; j < FACILITY_DIMENSION - 1; j++) {
                if (floorPlan[i][j] == null) continue;
                int[] functions = calculateIndividualAffinity(floorPlan, i, j);
                switch (floorPlan[i][j].function) {
                    case 0: affinity += functions[3]; break;
                    case 1: affinity += functions[2]; break;
                    case 2: affinity += functions[1]; break;
                    case 3: affinity += functions[0]; break;
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
    private int[] calculateIndividualAffinity(Station[][] floorPlan, int x, int y) {
        int radius = 2;
        int[] functionCounts = new int[4];
        HashSet<Integer> completedIDs = new HashSet<>();

        // Perfect Situation: If all space is available within the bounds
        if (calculateTopLeftCoordinate(x, y, radius)) {
            for (int i = x - radius; i < x + radius; i++) {
                for (int j = y - radius; j < y + radius; j++) {
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
    private boolean calculateTopLeftCoordinate(int x, int y, int radius) {
        return (x - radius >= 0 && y - radius >= 0);
    }

    }
