package com.schedule.core.Graphs.FeasibleSchedules.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Saved benchmark instances and best solutions found.
 */
public class BenchmarkLowerBounds {

    /**
     * Best-Known solution for given benchmark instance.
     */
    public static final Map<String, Integer> knownBest = new HashMap<String, Integer>() {
        {
            put("ft06", 55);
            put("ft10", 930);
            put("ft20", 1165);
            put("la01", 666);
            put("la10", 958);
            put("la11", 1222);
            put("la23", 1032);
            put("la34", 1721);
            put("la35", 1888);
            put("la36", 1268);
            put("la37", 1397);
            put("swv11", 2983);
            put("swv13", 3104);
            put("swv17", 2794);
            put("swv18", 2852);
            put("ta69", 3071);
            put("ta71", 5464);
            put("ta76", 5342);
            put("yn2", 904);
            put("yn3", 892);
        }
    };

    /**
     * Best Found solution by SAFA algorithm.
     */
    public static final Map<String, Integer> achieved = new HashMap<String, Integer>() {
        {
            put("ft06", 54);
            put("ft10", 1007);
            put("ft20", 2000);
            put("la01", 666);
            put("la10", 958);
            put("la11", 1222);
            put("la23", 1090);
            put("swv11", 4583);
            put("la34", 1926);
            put("la35", 2160);
            put("la36", 2500);
            put("la37", 2500);
            put("swv11", 4375);
            put("swv13", 4619);
            put("swv17", 3064);
            put("swv18", 3073);
            put("ta69", 4201);
            put("ta71", 7600);
            put("ta76", 7339);
            put("yn2", 1197);
            put("yn3", 2000);
        }
    };
}
