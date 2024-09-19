import java.util.ArrayList;
import java.util.Random;

public class GeneticAlgorithmTester {
    public static int NUMBER_OF_FACILITIES = 2;
    public static int FACILITY_DIMENSION = 12;
    public static int NUMBER_OF_PEOPLE = 4;


    public static void main(String[] args) {
        Person[] listOfPeople = createPersonList(NUMBER_OF_PEOPLE);

        // TODO: Create Facilities by using NUMBER_OF_FACILITIES and a loop
//        ArrayList<Facility> listOfFacilities = new ArrayList<>();
//        for (int i = 0; i < NUMBER_OF_FACILITIES; i++) {
//            listOfFacilities.add(new Facility(floorPlan));
//        }
//        for (Facility x : listOfFacilities) {
//            x.start();
//        }

        // TODO: Remove this implementation once completed
        Facility facility = new Facility(FACILITY_DIMENSION, listOfPeople);
        Facility facility1 = new Facility(FACILITY_DIMENSION, listOfPeople);
        ArrayList<Facility> listOfFacilities = new ArrayList<>();
        listOfFacilities.add(facility);
        listOfFacilities.add(facility1);

        for (Facility x : listOfFacilities) {
            x.start();
        }

    }

    /**
     * This function creates an array of people, with random functions, so that they may be placed in the facility.
     * @param numberOfPeople - insert the number of people that you want created
     * @return person[] - return an array filled with people of different functions
     */
    static Person[] createPersonList(int numberOfPeople) {
        Person[] people = new Person[numberOfPeople];
        for (int i = 0; i < numberOfPeople; i++) {
            Random r = new Random();
            int randomInt = r.nextInt(0, 5);
            people[i] = new Person(randomInt);
        }
        return people;
    }
}
