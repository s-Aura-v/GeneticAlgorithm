/**
 * Facility is a class that holds a 2d array, representing a floor plan. Each array index represents a location on the floor.
 *
 */
public class Facility extends Thread {
    private String[][] floorPlan;
    private Person[] listOfPeople;
    private String[] solutionSet;


    public Facility(String[][] floorPlan, Person[] listOfPeople) {
        this.floorPlan = floorPlan;
        this.listOfPeople = listOfPeople;
    }


    /**
     * Run - Starts the program in a multithreaded manner. conjoint with "extends Thread"
     */
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(listOfPeople.length);
        }
    }




}
