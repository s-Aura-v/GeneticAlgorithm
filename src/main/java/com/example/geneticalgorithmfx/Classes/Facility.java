package com.example.geneticalgorithmfx.Classes;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

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
    // Affinity keys store the keys so we can access the map in bestSolutionPool.
    private final int AFFINITY_LIMIT = 10;
    private final int AFFINITY_RADIUS = 5;
    private int[] affinityKeys = new int[AFFINITY_LIMIT];
    //TEST CASES FOR GLOBAL
    HashMap<Integer, Station[][]> solutionPoolTest = new HashMap<>();
    ArrayList<Station[][]> solutionPoolQuadrants = new ArrayList<>();
    HashMap<Integer, Station[][]> bestSolutionsPool = new HashMap<>();

    void fillSolutionPoolTest() {
        for (int i = 0; i < AFFINITY_LIMIT; i++) {
            int randomNumberGenerator = ThreadLocalRandom.current().nextInt(100, 1000);
            solutionPoolTest.put(randomNumberGenerator, cloneFloorPlan(floorPlan));
        }
        addToGlobalBestSolutions();
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

    // FOR TESTING PURPOSES, WE'RE GONNA ASSUME THE FACILITY IS ALWAYS ODD.
    // FOR TESTING PURPOSES, WE'RE GONNA ASSUME THE FACILITY IS ALWAYS ODD.
    // FOR TESTING PURPOSES, WE'RE GONNA ASSUME THE FACILITY IS ALWAYS ODD.
    synchronized void addToGlobalBestSolutions() {
        // Get the best solution (full facility grid)
        Station[][] solution = getBestSolution();
        ArrayList<Station[][]> listOfQuadrants = new ArrayList<>();
        solutionPoolQuadrants.add(solution); // add full solution to pool
        Station[][] quadrant = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];

        // For odd-numbered quadrants, the facility is naturally separated by a barrier
        int mid = FACILITY_DIMENSION / 2;

        // Process the left quadrant
        for (int i = 0; i <= mid; i++) {
            for (int j = 0; j <= mid; j++) {
                quadrant[i][j] = solution[i][j];
            }
        }
        solutionPoolQuadrants.add(quadrant);

        // Process the right quadrant
        quadrant = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
        for (int i = mid + 1; i < FACILITY_DIMENSION; i++) {
            for (int j = mid + 1; i < FACILITY_DIMENSION; i++) {
                quadrant[i][j] = solution[i][j];
            }
        }
        solutionPoolQuadrants.add(quadrant);

        createPooledFacility();
    }


    synchronized void createPooledFacility() {
        HashMap<Integer, Station[][]> pooledSolutions = new HashMap<>();
        for (int i = 0; i < solutionPoolQuadrants.size(); i++) {
            for (int j = 0; j < solutionPoolQuadrants.size(); j++) {
                if (contains(solutionPoolQuadrants.get(i), solutionPoolQuadrants.get(j))) {
                    break;
                } else {
                    System.out.println("WORKS!");
                    Station[][] merged = merge(solutionPoolQuadrants.get(i), solutionPoolQuadrants.get(j));
                    System.out.println(calculateAffinity(merged));
                }
            }
        }
    }

    /**
     * Check if the two quadrants have overlapping values.
     */
    synchronized boolean contains(Station[][] station1, Station[][] station2) {
        try {
            for (Station[] row1 : station1) {
                for (Station value1 : row1) {
                    for (Station[] row2 : station2) {
                        for (Station value2 : row2) {
                            if (value1.getId() == value2.getId()) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } catch (NullPointerException e) {
        }
        return false;
    }

    /**
     * Combine two quadrants.
     */
    synchronized Station[][] merge(Station[][] quadrant1, Station[][] quadrant2) {
        int leftQuadrant = FACILITY_DIMENSION / 2 - 1;
        int rightQuadrant = leftQuadrant + 2;
        Station[][] merged = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];

        // Copy quadrant1 into the left half of the merged array
        for (int i = 0; i < leftQuadrant; i++) {
            for (int j = 0; j < FACILITY_DIMENSION; j++) {
                merged[i][j] = quadrant1[i][j];
            }
        }

        // Copy quadrant2 into the right half of the merged array
        for (int i = rightQuadrant; i < FACILITY_DIMENSION; i++) {
            for (int j = rightQuadrant; j < FACILITY_DIMENSION; j++) {
                merged[i][j] = quadrant2[i][j]; // Offset by the width of quadrant1
            }
        }
        return merged;
    }


    /**
     * Flatten the 2d array of stations in floor plan into a 1d array of ids so I can use them to determine if they are capable of fitting.
     */
    synchronized static int[] flatten(Station[][] arr) {
        return Arrays.stream(arr) // Stream of MyClass[]
                .flatMap(Arrays::stream) // Flatten MyClass[][] to MyClass[]
                .mapToInt(Station::getId) // Map MyClass to int using getValue()
                .toArray(); // Collect as an int[] array
    }

    synchronized Station[][] getBestSolution() {
        if (bestSolutionsPool.isEmpty()) {
            return null;
        }
        int highestAffinity = bestSolutionsPool.keySet().stream().mapToInt(v -> v).max().orElse(Integer.MIN_VALUE);
        return bestSolutionsPool.get(highestAffinity);
    }


    public Facility(int FACILITY_DIMENSION, Station[] listOfStation, int NUMBER_OF_ITERATIONS) {
        this.floorPlan = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
        this.FACILITY_DIMENSION = FACILITY_DIMENSION;
        this.listOfStation = listOfStation;
        this.NUMBER_OF_ITERATIONS = NUMBER_OF_ITERATIONS;
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
        fillSolutionPoolTest();
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

            // Adds data into a solution pool
            if (bestSolutionsPool.size() < AFFINITY_LIMIT) {
                bestSolutionsPool.put(affinity, cloneFloorPlan(floorPlan));
            } else if (bestSolutionsPool.size() == AFFINITY_LIMIT) {
                affinityKeys = bestSolutionsPool.keySet().stream().mapToInt(Integer::intValue).toArray();
                insertionSort();
            } else {
                addToBestSolutionsPool(affinity);
            }
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

    private synchronized int calculateAffinity(Station[][] floorPlan) {
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

    /***
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
     * Sorts the affinity array (keys for hashMap) once 10 elements are added.
     * Insertion sort is used due to the array's small size.
     */
    private void insertionSort() {
        int i, j, item;
        for (i = 1; i < affinityKeys.length; i++) {
            item = affinityKeys[i];
            j = i;
            while (j > 0 && affinityKeys[j - 1] < item) {
                affinityKeys[j] = affinityKeys[j - 1];
                j--;
            }
            affinityKeys[j] = item;
        }
    }

    /**
     * Checks if affinity is higher than the values at the top 10; if true, then adds it to the arrays.
     */
    private void addToBestSolutionsPool(int affinity) {
        int indexToReplace = -1;
        if (affinity <= affinityKeys[AFFINITY_LIMIT - 1]) {
            return;
        }
        for (int i = 0; i < AFFINITY_LIMIT; i++) {
            if (affinity >= affinityKeys[i]) {
                indexToReplace = i;
                break;
            }
        }
        for (int i = AFFINITY_LIMIT - 1; i > indexToReplace; i--) {
            affinityKeys[i] = affinityKeys[i - 1];
        }
        affinityKeys[indexToReplace] = affinity;
        bestSolutionsPool.put(affinity, floorPlan);
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
}