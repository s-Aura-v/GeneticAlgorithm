import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
        int xValue = ThreadLocalRandom.current().nextInt(1, FACILITY_DIMENSION - 1);
        int yValue = ThreadLocalRandom.current().nextInt(1, FACILITY_DIMENSION - 1);
        Person person = listOfPeople[index];
        int function = listOfPeople[index].function;
        if (floorPlan[xValue][yValue] == null) {
            boolean added = false;
            if (function == 0) {
                floorPlan[xValue][yValue] = listOfPeople[index];
            } else if (function == 1) {
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
            } else if (function == 2) {
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
            } else if (function == 3) {
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
     * There is a max radius of [z]. The closer to z, the less value the likability has.
     * Affinity = distance * likeability
     * Find where every person is located and calculate the affinity for each one.
     */
    //@formatter:off
    private synchronized void calculateAffinity(Person[][] floorPlan) {
        /*
        1. Figure out where everyone is located
        2. Calculate each individual's affinity
        3. Add it all together

        The arrays have a border so it's safe to visit every cell.
        Now, I need to get the proper affininty
        Figure out how many people are around it, then determine how good it is
         */
        int affinity = 0;
        int zeroCount = 0;
        int oneCount = 0;
        int twoCount = 0;
        int threeCount = 0;
        HashSet<Integer> completedIDs = new HashSet<Integer>();

        for (int i = 1; i < FACILITY_DIMENSION - 1; i++) {
            for (int j = 1; j < FACILITY_DIMENSION - 1; j++) {
                if (floorPlan[i][j] == null) continue;
                try {
                    switch (floorPlan[i + 1][j].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }
                try {
                    switch (floorPlan[i - 1][j].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }
                try {
                    switch (floorPlan[i][j + 1].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }
                try {
                    switch (floorPlan[i][j - 1].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }
                try {
                    switch (floorPlan[i - 1][j + 1].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }
                try {
                    switch (floorPlan[i - 1][j - 1].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }
                try {
                    switch (floorPlan[i + 1][j + 1].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }
                try {
                    switch (floorPlan[i + 1][j - 1].function) {
                        case 0: zeroCount++; break;
                        case 1: oneCount++; break;
                        case 2: twoCount++; break;
                        case 3: threeCount++; break;
                    }
                } catch (NullPointerException e) {
                }

                Person person = floorPlan[i][j];
                completedIDs.add(person.id);

                if ((!completedIDs.contains(person.id)) && person.function == 0) {
                    affinity += threeCount;
                }
                if ((!completedIDs.contains(person.id)) && person.function == 1) {
                    affinity += twoCount;
                }
                if ((!completedIDs.contains(person.id)) && person.function == 2) {
                    affinity += oneCount;
                }
                if ((!completedIDs.contains(person.id)) && person.function == 3) {
                    affinity += zeroCount;
                }
                System.out.println(affinity);
            }
        }
    }
    //@formatter:on

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
