package com.example.geneticalgorithmfx.Classes;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.geneticalgorithmfx.Classes.GlobalSolutionPool.bestSolutionsPool;
import static com.example.geneticalgorithmfx.Classes.GlobalSolutionPool.solutionsLock;

/**
 * Facility is a class that holds a 2d array, representing a floor plan. Each array index represents a location on the floor.
 * It is run in threads, randomly placing the stations, and calculating the affinity which is then added to a database of best solutions.
 */
public class Facility extends Thread {
    int FACILITY_DIMENSION;
    private final Station[][] floorPlan;
    private final Station[] listOfStation;
    private final ReentrantLock lock = new ReentrantLock();
    private final int NUMBER_OF_ITERATIONS;
    private final int AFFINITY_RADIUS = 2;
    //TEST CASES FOR GLOBAL
    CountDownLatch latch;


    public Facility(int FACILITY_DIMENSION, Station[] listOfStation, int NUMBER_OF_ITERATIONS, CountDownLatch latch) {
        this.floorPlan = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
        if (FACILITY_DIMENSION % 2 == 0) {
            this.FACILITY_DIMENSION = FACILITY_DIMENSION + 1;
        } else {
            this.FACILITY_DIMENSION = FACILITY_DIMENSION;
        }        this.listOfStation = listOfStation;
        this.NUMBER_OF_ITERATIONS = NUMBER_OF_ITERATIONS;
        this.latch = latch;
    }

    @Override
    public void run() {
        int i = 0;
        while (i < NUMBER_OF_ITERATIONS) {
            if (FACILITY_DIMENSION % 2 == 1) addArrayBorders();
            randomizeStationInFacility();
            clearAll();
            i++;
        }
        latch.countDown();
    }

    /**
     * Randomizes the station locations in the facility floor plan.
     */
    public void randomizeStationInFacility() {
        lock.lock();
        try {
            int index = 0;
            while (index < listOfStation.length) {
                addToFloorPlan(index);
                index++;
            }
            int affinity = calculateAffinity(floorPlan);
//            System.out.println(affinity);
            addToBestSolutionsPool(affinity, cloneFloorPlan(floorPlan));
        } finally {
            lock.unlock();
        }
    }

    private static void addToBestSolutionsPool(int affinity, Station[][] clonedFloorPlan) {
        solutionsLock.lock();
        try {
            bestSolutionsPool.put(affinity, clonedFloorPlan);
        } finally {
            solutionsLock.unlock();
        }
    }

    /**
     * Adds a station to the facility randomly. No overlaps.
     */
    private void addToFloorPlan(int index) {
        if (index >= listOfStation.length) return;
        int xValue = ThreadLocalRandom.current().nextInt(1, FACILITY_DIMENSION - 1);
        int yValue = ThreadLocalRandom.current().nextInt(1, FACILITY_DIMENSION - 1);
        Station station = listOfStation[index];
        int function = station.function;

        lock.lock();
        try {
            try {
                if (floorPlan[xValue][yValue] == null) {
                    boolean added = attemptToAddStation(xValue, yValue, function, station);
                    if (!added) {
                        addToFloorPlan(index);
                    }
                } else {
                    addToFloorPlan(index);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                addToFloorPlan(index);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param x        - x-axis for the floor plan
     *                 [ x ∈ ℤ : 0 < x < FACILITY_DIMENSION ]
     * @param y        - y-axis for the floor plan
     *                 [ y ∈ ℤ : 0 < x < FACILITY_DIMENSION ]
     * @param function - station value
     * @param station  - station being added to the floorPlan 2d array
     * @return boolean - true for station was added / false for station couldn't be added
     */
    private boolean attemptToAddStation(int x, int y, int function, Station station) {
        switch (function) {
            case 0:
                floorPlan[x][y] = station;
                return true;
            case 1:
                return addHorizontalStation(x, y, station);
            case 2:
                return addVerticalStation(x, y, station);
            case 3:
                return addSquareStation(x, y, station);
            default:
                return false;
        }
    }

    /* Helper for attemptToAddStation*/
    private boolean addHorizontalStation(int x, int y, Station station) {
        lock.lock();
        try {
            if (x > 0 && floorPlan[x - 1][y] == null) {
                floorPlan[x][y] = station;
                floorPlan[x - 1][y] = station;
                return true;
            } else if (x < FACILITY_DIMENSION - 1 && floorPlan[x + 1][y] == null) {
                floorPlan[x][y] = station;
                floorPlan[x + 1][y] = station;
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /* Helper for attemptToAddStation*/
    private boolean addVerticalStation(int x, int y, Station station) {
        lock.lock();
        try {
            if (y > 0 && floorPlan[x][y - 1] == null) {
                floorPlan[x][y] = station;
                floorPlan[x][y - 1] = station;
                return true;
            } else if (y < FACILITY_DIMENSION - 1 && floorPlan[x][y + 1] == null) {
                floorPlan[x][y] = station;
                floorPlan[x][y + 1] = station;
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /* Helper for attemptToAddStation*/
    private boolean addSquareStation(int x, int y, Station station) {
        lock.lock();
        try {
            if (floorPlan[x + 1][y] == null && floorPlan[x - 1][y] == null &&
                    floorPlan[x][y - 1] == null && floorPlan[x][y + 1] == null &&
                    floorPlan[x - 1][y + 1] == null && floorPlan[x - 1][y - 1] == null &&
                    floorPlan[x + 1][y + 1] == null && floorPlan[x + 1][y - 1] == null) {
                floorPlan[x][y] = station;
                floorPlan[x + 1][y] = station;
                floorPlan[x - 1][y] = station;
                floorPlan[x][y + 1] = station;
                floorPlan[x][y - 1] = station;
                floorPlan[x + 1][y + 1] = station;
                floorPlan[x + 1][y - 1] = station;
                floorPlan[x - 1][y + 1] = station;
                floorPlan[x - 1][y - 1] = station;
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Calculates total affinity for the entire floor plan
     * @param floorPlan - the 2d array holding the stations
     * @return affinity - the total affinity between the stations in the floor plan
     */
    private synchronized int calculateAffinity(Station[][] floorPlan) {
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
    private synchronized int[] calculateIndividualAffinity(Station[][] floorPlan, int x, int y) {
        int radius = AFFINITY_RADIUS;
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
    private synchronized boolean calculateTopLeftCoordinate(int x, int y, int radius) {
        return (x - radius >= 0 && y - radius >= 0);
    }


    private Station[][] cloneFloorPlan(Station[][] original) {
        Station[][] clone = new Station[original.length][];
        for (int i = 0; i < original.length; i++) {
            clone[i] = original[i].clone();
        }
        return clone;
    }

    public Station[][] getFloorPlan() {
        return floorPlan;
    }


    /**
     * Clear all arrays and references so the iteration can start anew.
     */
    private void clearAll() {
        for (int i = 0; i < FACILITY_DIMENSION; i++) {
            for (int j = 0; j < FACILITY_DIMENSION; j++) {
                floorPlan[i][j] = null;
            }
        }
    }

    /**
     * For odd sized facilities, add a center wall to make quadrant selection easier
     */
    void addArrayBorders() {
        int midpoint = FACILITY_DIMENSION / 2;
        for (int i = 0; i < FACILITY_DIMENSION; i++) {
            floorPlan[midpoint][i] = new Station(-1, -1);
        }
    }

}