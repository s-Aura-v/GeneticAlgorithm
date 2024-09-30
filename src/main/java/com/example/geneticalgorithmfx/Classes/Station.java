package com.example.geneticalgorithmfx.Classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Classes.Person, representing station, has 4 functions.
 * Functions are defined as 0,1,2,3, representing a station, horizontal station, vertical station, and square station.
 * Each function takes a different amount of space; They take the same amount of space as their integer representation.
 */
public class Station {
    int function;
    final int id;
    public Station(int function, int id) {
        this.function = function;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean contains(Station[][] stations){
        return false;
    }

    @Override
    public String toString() {
        return "hi";
    }
}
