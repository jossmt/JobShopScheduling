package com.schedule.core.Graphs.FeasibleSchedules.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Saved benchmark instances and best solutions found.
 */
public class BenchmarkLowerBounds {

    public static final Map<String, Integer> knownBest = new HashMap<String, Integer>() {
        {
            put("ft06", 55);
            put("ft10", 930);
            put("la01", 666);
            put("la10", 958);
            put("la11", 1222);
            put("la23", 1032);
        }
    };

    public static final Map<String, Integer> achieved = new HashMap<String, Integer>() {
        {
            put("ft06", 55);
            put("ft10", 1026);
            put("la01", 666);
            put("la10", 958);
            put("la11", 1222);
            put("la23", 1155);
        }
    };
}
