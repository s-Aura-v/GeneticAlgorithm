import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Facility is a class that holds a 2d array, representing a floor plan. Each array index represents a location on the floor.
 *
 */
public class Facility extends Thread {

    //TODO: Temp
    int FACILITY_DIMENSION;
    private Person[][] floorPlan;
    private Person[] listOfPeople;
    private ArrayList<Person[][]> solutionSet;


    public Facility(int FACILITY_DIMENSION, Person[] listOfPeople) {
        this.FACILITY_DIMENSION = FACILITY_DIMENSION;
        this.floorPlan = new Person[FACILITY_DIMENSION][FACILITY_DIMENSION];
        this.listOfPeople = listOfPeople;
        this.solutionSet = new ArrayList<>();
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
        int floorMaxCapacity = 12;

        int index = 0;
        while (index < listOfPeople.length) {
            int xValue = ThreadLocalRandom.current().nextInt(floorMaxCapacity);
            int yValue = ThreadLocalRandom.current().nextInt(floorMaxCapacity);
            addToFloorPlan(xValue, yValue, index);
            index++;
        }
        this.solutionSet.add(floorPlan);
    }

    /**
     * Synchronized are locks used for atomicity. It locks the method then unlocks it at the end.
     * @param xValue: the x-axis in the floor plan
     * @param yValue: the y-axis in the floor plan
     * @param index: the index for the Person stored in the Person array
     * @return void: adds a person to the floor plan
     */
    private synchronized void addToFloorPlan(int xValue, int yValue, int index) {
        floorPlan[xValue][yValue] = listOfPeople[index];
    }

    private void addToSolutionSet() {

    }
    private void calculateAffinity() {

    }







}
