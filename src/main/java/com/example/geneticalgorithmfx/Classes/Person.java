package com.example.geneticalgorithmfx.Classes;

/**
 * Classes.Person, representing station, has 4 functions.
 * Functions are defined as 0,1,2,3.
 * Each function takes a different amount of space; They take the same amount of space as their integer representation.
 */
public class Person {
    int function;
    int affinity;
    final int id;
    public Person(int function, int id) {
        this.function = function;
        this.id = id;
    }

    public int getFunction() {
        return function;
    }
}
