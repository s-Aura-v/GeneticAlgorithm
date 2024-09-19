import java.util.ArrayList;
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
     * xValue: the x-axis in the floor plan
     * yValue: the y-axis in the floor plan
     * @param index: the index for the Person stored in the Person array
     * @return void: adds a person to the floor plan
     */
    private synchronized void addToFloorPlan(int index) {
        int xValue = ThreadLocalRandom.current().nextInt(FACILITY_DIMENSION);
        int yValue = ThreadLocalRandom.current().nextInt(FACILITY_DIMENSION);
        if (floorPlan[xValue][yValue] != null) {
            floorPlan[xValue][yValue] = listOfPeople[index];
        } else {
            addToFloorPlan(index);
        }
    }

    /**
     //TODO: Saved for later. Complete program first to see if it works, then add complicated ideas.
     */
//    private synchronized void addToFloorPlan(int index) {
//        int xValue = ThreadLocalRandom.current().nextInt(FACILITY_DIMENSION);
//        int yValue = ThreadLocalRandom.current().nextInt(FACILITY_DIMENSION);
//        Person person = listOfPeople[index];
//        int personVolume = listOfPeople[index].function;
//        if (floorPlan[xValue][yValue] == null) {
//            if (personVolume == 0) {
//                floorPlan[xValue][yValue] = listOfPeople[index];
//            } else if (personVolume == 1) {
//                boolean added = false;
//                if (floorPlan[xValue - 1][yValue] != null) {
//                    floorPlan[xValue][yValue] = person;
//                    floorPlan[xValue - 1][yValue] = person;
//                    added = true;
//                }
//                if (floorPlan[xValue + 1][yValue] != null && (!added)) {
//                    floorPlan[xValue][yValue] = person;
//                    floorPlan[xValue + 1][yValue] = person;
//                }
//            } else if (personVolume == 2) {
//                boolean added = false;
//                if (floorPlan[xValue][yValue - 1] != null) {
//                    floorPlan[xValue][yValue] = person;
//                    floorPlan[xValue][yValue - 1] = person;
//                    added = true;
//                }
//                if (floorPlan[xValue][yValue + 1] != null && (!added)) {
//                    floorPlan[xValue][yValue] = person;
//                    floorPlan[xValue][yValue + 1] = person;
//                }
//            } else if (personVolume == 3) {
//                if (floorPlan[xValue + 1][yValue] != null && floorPlan[xValue - 1][yValue] != null &&
//                        floorPlan[xValue][yValue - 1] != null && floorPlan[xValue][yValue + 1] != null &&
//                        floorPlan[xValue - 1][yValue + 1] != null && floorPlan[xValue - 1][yValue - 1] != null &&
//                        floorPlan[xValue + 1][yValue + 1] != null && floorPlan[xValue + 1][yValue - 1] != null) {
//                    floorPlan[xValue][yValue] = person;
//                    floorPlan[xValue + 1][yValue] = person;
//                    floorPlan[xValue - 1][yValue] = person;
//                    floorPlan[xValue][yValue + 1] = person;
//                    floorPlan[xValue][yValue - 1] = person;
//                    floorPlan[xValue + 1][yValue + 1] = person;
//                    floorPlan[xValue + 1][yValue - 1] = person;
//                    floorPlan[xValue - 1][yValue + 1] = person;
//                    floorPlan[xValue - 1][yValue - 1] = person;
//                }
//            }
//        } else {
//            addToFloorPlan(index);
//        }
//    }

    private void addToSolutionSet() {

    }

    //TODO: Saved for later. Complete DEMO first to see if it works.
    /**
     * Functions are defined as 0,1,2,3,4 representing "Doctor", "Assistant", "Student", "Patient", and "Dean Of Medicine."
     * [x] -> [y] implies x likes y
     * [x] -/> [y] implies x does not like y
     * There is a max radius of [z]. The closer to z, the less value the likability has.
     * Affinity = distance * likeability
     * Find where every person is located and calculate the affinity for each one.
     */
//    private void calculateAffinity(Person[][] floorPlan) {
//        /*
//        1. Figure out where everyone is located
//        2. Calculate each individual's affinity
//        3. Add it all together
//         */
//        for (Person[] rowsOfPeople : floorPlan) {
//            for (Person person : rowsOfPeople) {
//                if (person == null) continue;
//
//            }
//        }
//    }


    /*
    Demo where we only look around us.
     */
    private void calculateAffinity(Person[][] floorPlan) {
        /*
        1. Figure out where everyone is located
        2. Calculate each individual's affinity
        3. Add it all together
         */
        for (int i = 0; i < FACILITY_DIMENSION; i++) {
            for (int j = 0; j < FACILITY_DIMENSION; j++ ) {
                if (floorPlan[i][j] == null) continue;
                int affinity = 0;
                try {

                } catch (ArrayIndexOutOfBoundsException e){

                }
            }
        }
    }
}
