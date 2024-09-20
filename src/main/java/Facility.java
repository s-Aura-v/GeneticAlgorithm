import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Facility is a class that holds a 2d array, representing a floor plan. Each array index represents a location on the floor.
 */
public class Facility extends Thread {

    //TODO: Temp
    int FACILITY_DIMENSION;
    private Person[][] floorPlan;
    private Person[] listOfPeople;
    private ArrayList<Person[][]> solutionSet;
//    private final Exchanger< String > exchanger;

    //    private HashMap<Integer, Person[][]> solutionSet;
    int[] affinityKeys;


    public Facility(int FACILITY_DIMENSION, Person[] listOfPeople) {
        this.FACILITY_DIMENSION = FACILITY_DIMENSION;
        this.floorPlan = new Person[FACILITY_DIMENSION][FACILITY_DIMENSION];
        this.listOfPeople = listOfPeople;
        this.solutionSet = new ArrayList<>();
        this.affinityKeys = new int[10];
    }


    /**
     * Run - Starts the program in a multithreaded manner. conjoint with "extends Thread"
     */
    public void run() {
        randomizePeopleInFacility();
    }

    /**
     * Randomly place people in the floor plan.
     * Side Note: Enable thread debugging in Intelli by right-clicking the breakpoint and choosing "Suspend: Thread"
     */
    public void randomizePeopleInFacility() {
        // TODO: Figure out how to determine 12 without using 12 explicitly

        int index = 0;
        while (index < listOfPeople.length) {
            addToFloorPlan(index);
            index++;
        }
        this.solutionSet.add(floorPlan);

        //TODO: Alternative way to add to floor plan that looks at affinity
        calculateAffinity(floorPlan);
    }

    /**
     * Synchronized are locks used for atomicity. It locks the method then unlocks it at the end.
     *
     * @param index: the index for the Person stored in the Person array
     * @return void: adds a person to the floor plan
     * xValue: the x-axis in the floor plan
     * yValue: the y-axis in the floor plan
     */

    private synchronized void addToFloorPlan(int index) {
        int xValue = ThreadLocalRandom.current().nextInt(FACILITY_DIMENSION);
        int yValue = ThreadLocalRandom.current().nextInt(FACILITY_DIMENSION);
        Person person = listOfPeople[index];
        int personVolume = listOfPeople[index].function;
        if (floorPlan[xValue][yValue] == null) {
            boolean added = false;
            if (personVolume == 0) {
                floorPlan[xValue][yValue] = listOfPeople[index];
            } else if (personVolume == 1) {
                try {
                    if (floorPlan[xValue - 1][yValue] == null) {
                        floorPlan[xValue][yValue] = person;
                        floorPlan[xValue - 1][yValue] = person;
                        added = true;
                    }
                    if ((!added) && floorPlan[xValue + 1][yValue] == null) {
                        floorPlan[xValue][yValue] = person;
                        floorPlan[xValue + 1][yValue] = person;
                        added = true;
                    }
                    if (!added) addToFloorPlan(index);
                } catch (ArrayIndexOutOfBoundsException e) {
                    addToFloorPlan(index);
                }

            } else if (personVolume == 2) {
                try {
                    if (floorPlan[xValue][yValue - 1] == null) {
                        floorPlan[xValue][yValue] = person;
                        floorPlan[xValue][yValue - 1] = person;
                        added = true;
                    }
                    if ((!added) && floorPlan[xValue][yValue + 1] == null) {
                        floorPlan[xValue][yValue] = person;
                        floorPlan[xValue][yValue + 1] = person;
                        added = true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    addToFloorPlan(index);
                }
                if (!added) addToFloorPlan(index);
            } else if (personVolume == 3) {
                try {
                    if (floorPlan[xValue + 1][yValue] == null && floorPlan[xValue - 1][yValue] == null &&
                            floorPlan[xValue][yValue - 1] == null && floorPlan[xValue][yValue + 1] == null &&
                            floorPlan[xValue - 1][yValue + 1] == null && floorPlan[xValue - 1][yValue - 1] == null &&
                            floorPlan[xValue + 1][yValue + 1] == null && floorPlan[xValue + 1][yValue - 1] == null) {
                        floorPlan[xValue][yValue] = person;
                        floorPlan[xValue + 1][yValue] = person;
                        floorPlan[xValue - 1][yValue] = person;
                        floorPlan[xValue][yValue + 1] = person;
                        floorPlan[xValue][yValue - 1] = person;
                        floorPlan[xValue + 1][yValue + 1] = person;
                        floorPlan[xValue + 1][yValue - 1] = person;
                        floorPlan[xValue - 1][yValue + 1] = person;
                        floorPlan[xValue - 1][yValue - 1] = person;
                        added = true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    addToFloorPlan(index);
                }
                if (!added) addToFloorPlan(index);
            }
        } else {
            addToFloorPlan(index);
        }
    }

    private void addToSolutionSet() {

    }

    //TODO: Saved for later. Complete DEMO first to see if it works.

    /**
     * Functions are defined as 0,1,2,3.
     * 0 takes a space of 1x1. Likes 3.
     * 1 takes a space of 1x2. Likes 2.
     * 2 takes a space of 2x1; Likes 1.
     * 3 takes a space of 3x3; Likes 0.
     * Affinity starts at 0 and can only go up depending on proximity.
     * [x] -> [y] implies x likes y
     * [x] -/> [y] implies x does not like y
     * There is a max radius of [z]. The closer to z, the less value the likability has.
     * Affinity = distance * likeability
     * Find where every person is located and calculate the affinity for each one.
     */
    private void calculateAffinity(Person[][] floorPlan) {
        /*
        1. Figure out where everyone is located
        2. Calculate each individual's affinity
        3. Add it all together
         */
        int affinity = 0;
        ArrayList<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < FACILITY_DIMENSION; i++) {
            for (int j = 0; j < FACILITY_DIMENSION; j++) {
                if (floorPlan[i][j] == null) continue;
                boolean completed = false;
                try {
                    if (i == 0) {
                        // can't subtract X
                    }
                    if (j == 0) {
                        // can't subtract Y
                    }
                    if (i == 11) {
                        // can't add X
                    }
                    if (j == 11) {
                        // can't add Y
                    }
                    if ((!completed) && i != 0 && j != 0 && i != 11 && j != 11) {
                        // then you can add or subtract at all times
                        if (floorPlan[i + 1][j] != null) {
                            neighbors.add(floorPlan[i + 1][j].function);
                        }
                        if (floorPlan[i - 1][j] != null) {
                            neighbors.add(floorPlan[i - 1][j].function);
                        }
                        if (floorPlan[i][j - 1] != null) {
                            neighbors.add(floorPlan[i][j - 1].function);
                        }
                        if (floorPlan[i][j + 1] != null) {
                            neighbors.add(floorPlan[i][j + 1].function);
                        }
                        if (floorPlan[i - 1][j + 1] != null) {
                            neighbors.add(floorPlan[i - 1][j + 1].function);
                        }
                        if (floorPlan[i - 1][j - 1] != null) {
                            neighbors.add(floorPlan[i - 1][j - 1].function);
                        }
                        if (floorPlan[i + 1][j + 1] != null) {
                            neighbors.add(floorPlan[i + 1][j + 1].function);
                        }
                        if (floorPlan[i + 1][j - 1] != null) {
                            neighbors.add(floorPlan[i + 1][j - 1].function);
                        }
                    }
                    lookAround(floorPlan, i, j);
                } catch (ArrayIndexOutOfBoundsException e) {

                }
            }
        }
    }

    /**
     * Helper method for calculateAffinity
     * Looks around the object to see if there are any objects nearby and makes note of it.
     */
    private void lookAround(Person[][] floorPlan, int xValue, int yValue) {
        int affinity = 0;
        try {


        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }
}
