package com.example.geneticalgorithmfx.Classes;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalSolutionPool {
    public static HashMap<Integer, Station[][]> bestSolutionsPool = new HashMap<>();
    public static HashMap<Integer, Station[][]> rebuiltSolutionsPool = new HashMap<>();
    public static ReentrantLock solutionsLock = new ReentrantLock();

}
