package com.example.geneticalgorithmfx.Classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Facility is a class that holds a 2d array, representing a floor plan. Each array index represents a location on the floor.
 */
public class Facility extends Thread {
    int FACILITY_DIMENSION;
    private final Station[][] floorPlan;
    private final Station[] listOfStation;
    private final ArrayList<Station[][]> solutionSet;
    int[] affinityKeys;
    private final ReentrantLock lock = new ReentrantLock();

    public Facility(int FACILITY_DIMENSION, Station[] listOfStation) {
        this.floorPlan = new Station[FACILITY_DIMENSION][FACILITY_DIMENSION];
        this.FACILITY_DIMENSION = FACILITY_DIMENSION;
        this.listOfStation = listOfStation;
        this.solutionSet = new ArrayList<>();
        this.affinityKeys = new int[10];
    }

    @Override
    public void run() {
        randomizeStationInFacility();
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
            this.solutionSet.add(cloneFloorPlan(floorPlan));
            calculateAffinity(floorPlan);
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

    private boolean addSquareStation(int x, int y, Station station) {
        lock.lock();
        try {
            if (floorPlan[x + 1][y] == null && floorPlan[x - 1][y] == null &&
                    floorPlan[x][y - 1] == null && floorPlan[x][y + 1] == null &&
                    floorPlan[x - 1][y + 1] == null && floorPlan[x - 1][y - 1] == null &&
                    floorPlan[x + 1][y + 1] == null && floorPlan[x + 1][y - 1] == null){
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

    private synchronized void calculateAffinity(Station[][] floorPlan) {
        int affinity = 0;
        HashSet<Integer> completedIDs = new HashSet<>();

        for (int i = 1; i < FACILITY_DIMENSION - 1; i++) {
            for (int j = 1; j < FACILITY_DIMENSION - 1; j++) {
                if (floorPlan[i][j] == null) continue;
                if (completedIDs.contains(floorPlan[i][j].id)) continue;

                affinity = calculateIndividualAffinity(floorPlan, i, j, completedIDs);
            }
        }
        System.out.println(affinity);

    }

    /**
     * TODO: I could recurse this using function.
     * TODO: Add weight so the affinity actually matters.
     * @param floorPlan - the 2d array of stations
     * @param i - the xValue of the station
     * @param j - the yValue of the station
     * @param completedIDs - set to make sure the affinity doesn't count itself multiple times (since they can take multiple spaces).
     */
    private int calculateIndividualAffinity(Station[][] floorPlan, int i, int j, HashSet<Integer> completedIDs) {
        //TODO: Should I convert this to AtomicInteger? or maybe add a lock again? or just leave it be.
        int zeroCount = 0, oneCount = 0, twoCount = 0, threeCount = 0;
        completedIDs.add(floorPlan[i][j].id);
        HashSet<Integer> completedIDsForNeighbors = new HashSet<>();

        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i + 1][j].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i - 1][j].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i][j + 1].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i][j - 1].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i - 1][j + 1].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i - 1][j - 1].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i + 1][j + 1].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (floorPlan[i][j].id == floorPlan[i - 1][j].id
                    || completedIDsForNeighbors.contains(floorPlan[i - 1][j].id)) {
                throw new NullPointerException();
            }
            switch (floorPlan[i + 1][j - 1].function) {
                case 0: zeroCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 1: oneCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 2: twoCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
                case 3: threeCount++; completedIDsForNeighbors.add(floorPlan[i][j].id); break;
            }
        } catch (NullPointerException e) {
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
}