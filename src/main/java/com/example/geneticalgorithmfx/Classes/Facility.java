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
    int AFFINITY_LIMIT = 10;
    int[] affinityKeys = new int[AFFINITY_LIMIT];
    //TEST CASES FOR GLOBAL
    HashMap<Integer, Station[][]> solutionPoolTest = new HashMap<>();
    ArrayList<Station[][]> solutionPoolQuadrants = new ArrayList<>();
    HashMap<Integer, Station[][]> bestSolutionsPool = new HashMap<>();

    void fillSolutionPoolTest() {
        for (int i = 0; i < AFFINITY_LIMIT; i++) {
            int randomNumberGenerator = ThreadLocalRandom.current().nextInt(100, 1000);
            solutionPoolTest.put(randomNumberGenerator, cloneFloorPlan(floorPlan));
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

    synchronized void addToGlobalBestSolutions() {
        // GRAB 2 QUADRANTS FROM THREADS
        // SEE IF THEY FIT ONE ANOTHER
        // IF THEY DO, COMBINE THEM AND TO GLOBAL
        Station[][] solution = getBestSolution();
        ArrayList<Station[][]> listOfQuadrants = new ArrayList<>();
        solutionPoolQuadrants.add(getBestSolution());

        Station[][] quadrant = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
        if (FACILITY_DIMENSION % 2 == 0) {
            ArrayList<Integer> skip = new ArrayList<>();
            int rightQuad = FACILITY_DIMENSION / 2;
            int leftQuad = rightQuad - 1;
            for (int i = 1; i <= leftQuad; i++) {
                for (int j = 1; i <= leftQuad; i++) {
                    if (solution[i][j].function == 0) {
                        quadrant[i][j] = solution[i][j];
                    }
                    //Check for horizontal and see if it goes to the other side
                    if (solution[i][j].function == 1) {
                        if (solution[i + 1][j].function == 1) {
                            quadrant[i + 1][j] = solution[i + 1][j];
                            //RETHINK SKIP
                            skip.add(i + 1);
                        }
                    }
                    if (solution[i][j].function == 3) {
                        if (solution[i + 1][j].function == 3) {
                            quadrant[i + 1][j + 1] = solution[i][j + 1];
                            quadrant[i + 1][j] = solution[i][j];
                            quadrant[i + 1][j - 1] = solution[i][j - 1];
                            //RETHINK SKIP
                            skip.add(j + 1);
                            skip.add(j - 1);
                            skip.add(j);
                        }
                    }
                }
            }
            solutionPoolQuadrants.add(quadrant);
            quadrant = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
            for (int i = rightQuad; i < FACILITY_DIMENSION; i++) {
                for (int j = rightQuad; i < FACILITY_DIMENSION; i++) {
                    quadrant[i][j] = solution[i][j];
                }
            }
            solutionPoolQuadrants.add(quadrant);
        } else {
            int leftQuad = (FACILITY_DIMENSION / 2) - 1;
            int rightQuad = leftQuad + 1;
            for (int i = 1; i <= leftQuad; i++) {
                for (int j = 1; j <= leftQuad; j++) {
                    quadrant[i][j] = solution[i][j];
                }
            }
            solutionPoolQuadrants.add(quadrant);
            quadrant = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
            for (int i = rightQuad; i < FACILITY_DIMENSION; i++) {
                for (int j = rightQuad; i < FACILITY_DIMENSION; i++) {
                    quadrant[i][j] = solution[i][j];
                }
            }
        }
        createPooledFacility();
    }

    synchronized void createPooledFacility() {
        for (int i = 0; i < solutionPoolQuadrants.size(); i++) {
            for (int j = 0; j < solutionPoolQuadrants.size(); j++) {
                if (contains(solutionPoolQuadrants.get(i), solutionPoolQuadrants.get(j), solutionPoolQuadrants.size())) {
                    break;
                } else {
                    System.out.println("WORKS!");
                }
            }
        }
    }

    /**
     * If a creature goes through multiple quadrants, try to relocate it.
     */
    synchronized void relocate(int x, int y, int function, boolean leftQuadrant, Station[][] selectedFloorPlan) {
        if (leftQuadrant) {
            switch (function) {
                case 1: // Horizontal station (shift left or right)
                    // Try to relocate to the left
                    if (x > 0 && selectedFloorPlan[x - 1][y] == null) {
                        selectedFloorPlan[x - 1][y] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    } else if (x < FACILITY_DIMENSION - 1 && selectedFloorPlan[x + 1][y] == null) {
                        // If shifting left is not possible, shift right
                        selectedFloorPlan[x + 1][y] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    }
                    break;

                case 2: // Vertical station (shift up or down)
                    if (y > 0 && selectedFloorPlan[x][y - 1] == null) {
                        selectedFloorPlan[x][y - 1] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    } else if (y < FACILITY_DIMENSION - 1 && selectedFloorPlan[x][y + 1] == null) {
                        selectedFloorPlan[x][y + 1] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    }
                    break;

                case 3: // Square-shaped station
                    relocateSquareStation(x, y, selectedFloorPlan);
                    break;
            }
        } else {
            // Try shifting to the right for stations in the right quadrant
            switch (function) {
                case 1: // Horizontal station (shift right or left)
                    if (x < FACILITY_DIMENSION - 1 && selectedFloorPlan[x + 1][y] == null) {
                        selectedFloorPlan[x + 1][y] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    } else if (x > 0 && selectedFloorPlan[x - 1][y] == null) {
                        // If shifting right is not possible, shift left
                        selectedFloorPlan[x - 1][y] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    }
                    break;

                case 2: // Vertical station (shift down or up)
                    if (y < FACILITY_DIMENSION - 1 && selectedFloorPlan[x][y + 1] == null) {
                        selectedFloorPlan[x][y + 1] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    } else if (y > 0 && selectedFloorPlan[x][y - 1] == null) {
                        selectedFloorPlan[x][y - 1] = selectedFloorPlan[x][y];
                        selectedFloorPlan[x][y] = null;
                    }
                    break;

                case 3: // Square-shaped station
                    relocateSquareStation(x, y, selectedFloorPlan);
                    break;
            }
        }
    }

    /**
     * Relocates a square-shaped station by checking all surrounding cells and shifting left or right.
     */
    private void relocateSquareStation(int x, int y, Station[][] selectedFloorPlan) {
        lock.lock();
        try {
            // Check if all 3x3 surrounding cells can be shifted to the left
            if (x > 0 && selectedFloorPlan[x - 1][y] == null && selectedFloorPlan[x - 1][y - 1] == null &&
                    selectedFloorPlan[x - 1][y + 1] == null) {
                selectedFloorPlan[x - 1][y] = selectedFloorPlan[x][y];
                selectedFloorPlan[x][y] = null;
            }
            // Else check if all 3x3 surrounding cells can be shifted to the right
            else if (x < FACILITY_DIMENSION - 1 && selectedFloorPlan[x + 1][y] == null &&
                    selectedFloorPlan[x + 1][y - 1] == null && selectedFloorPlan[x + 1][y + 1] == null) {
                selectedFloorPlan[x + 1][y] = selectedFloorPlan[x][y];
                selectedFloorPlan[x][y] = null;
            }
        } finally {
            lock.unlock();
        }
    }


    synchronized boolean contains(Station[][] station1, Station[][] station2, int size) {
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

    /**
     * TODO: I could recurse this using function.
     * TODO: Add weight so the affinity actually matters.
     *
     * @param floorPlan    - the 2d array of stations
     * @param i            - the xValue of the station
     * @param j            - the yValue of the station
     * @param completedIDs - set to make sure the affinity doesn't count itself multiple times (since they can take multiple spaces).
     */
    private int calculateIndividualAffinity(Station[][] floorPlan, int i, int j, HashSet<Integer> completedIDs) {
        //TODO: Should I convert this to AtomicInteger? or maybe add a lock again? or just leave it be.
        int zeroCount = 0, oneCount = 0, twoCount = 0, threeCount = 0;
        completedIDs.add(floorPlan[i][j].id);
        HashSet<Integer> completedIDsForNeighbors = new HashSet<>();

        try {
            if (floorPlan[i][j].id == floorPlan[i + 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i + 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i + 1][j].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j].id);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i - 1][j].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j].id);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i][j + 1].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i][j + 1].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j + 1].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j + 1].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j + 1].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j + 1].id);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i][j - 1].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i][j - 1].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j - 1].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j - 1].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j - 1].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i][j - 1].id);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j + 1].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i - 1][j + 1].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j + 1].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j + 1].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j + 1].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j + 1].id);
                    break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j - 1].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i - 1][j - 1].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j - 1].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j - 1].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j - 1].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i - 1][j - 1].id);
                    break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i + 1][j + 1].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i + 1][j + 1].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j + 1].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j + 1].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j + 1].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j + 1].id);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i + 1][j - 1].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i + 1][j - 1].function) {
                case 0:
                    zeroCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j - 1].id);
                    break;
                case 1:
                    oneCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j - 1].id);
                    break;
                case 2:
                    twoCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j - 1].id);
                    break;
                case 3:
                    threeCount++;
                    completedIDsForNeighbors.add(floorPlan[i + 1][j - 1].id);
                    break;
            }
        } catch (NullPointerException ignored) {
        }
        completedIDsForNeighbors.clear();
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