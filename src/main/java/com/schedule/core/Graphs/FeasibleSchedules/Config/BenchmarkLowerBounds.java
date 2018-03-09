package com.schedule.core.Graphs.FeasibleSchedules.Config;

import java.util.HashMap;
import java.util.Map;

public class BenchmarkLowerBounds {

    public static final Map<String, Integer> knownBest = new HashMap<String, Integer>() {
        {
            put("ft06", 55);
            put("ft10", 930);
            put("ft20", 1165);
            put("la01", 666);
            put("la17", 784);
            put("orb08", 899);
            put("abz6", 943);
            put("dmu05", 2749);
        }
    };

    public static final Map<String, Integer> achieved = new HashMap<String, Integer>() {
        {
            put("ft06", 55);
            put("ft10", 1026);
            put("ft20", 1450);
            put("la01", 666);
            put("la17", 837);
            put("orb08", 1069);
            put("abz6", 1022);
            put("dmu05", 3300);
        }
    };
}
