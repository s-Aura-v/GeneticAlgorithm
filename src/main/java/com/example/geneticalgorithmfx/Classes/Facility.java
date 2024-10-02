package com.example.geneticalgorithmfx.Classes;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.geneticalgorithmfx.Classes.GeneticAlgorithmTester.bestSolutionsPool;

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
    private final int AFFINITY_RADIUS = 5;
    //TEST CASES FOR GLOBAL
    CountDownLatch latch;


    public Facility(int FACILITY_DIMENSION, Station[] listOfStation, int NUMBER_OF_ITERATIONS, CountDownLatch latch) {
        this.floorPlan = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
        this.FACILITY_DIMENSION = FACILITY_DIMENSION;
        this.listOfStation = listOfStation;
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
            System.out.println(affinity);
            bestSolutionsPool.put(affinity, cloneFloorPlan(floorPlan));
        } finally {
            lock.unlock();
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

    public synchronized int calculateAffinity(Station[][] floorPlan) {
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
    private int calculateIndividualAffinity(Station[][] floorPlan, int x, int y, HashSet<Integer> completedIDs) {
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

    private int processStation(Station[][] floorPlan, int x, int y, Station currentStation, HashSet<Integer> completedIDsForNeighbors) {
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

    private boolean isWithinBounds(Station[][] floorPlan, int x, int y) {
        return x >= 0 && x < floorPlan.length && y >= 0 && y < floorPlan[0].length;
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