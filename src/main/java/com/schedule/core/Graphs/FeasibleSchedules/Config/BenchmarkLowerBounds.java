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
            put("la34", 1721);
            put("la35", 1888);
            put("swv11",2983);
            put("swv13",3104);
            put("swv17",2794);
            put("swv18",2852);
            put("ta69", 3071);
            put("ta71", 5464);
            put("ta76", 5342);
            put("yn2",  870);
        }
    };

    public static final Map<String, Integer> achieved = new HashMap<String, Integer>() {
        {
            put("ft06", 54);
            put("ft10", 1026);
            put("la01", 666);
            put("la10", 958);
            put("la11", 1222);
            put("la23", 1155);
            put("swv11", 4583);
            put("la34", 10000);
            put("la35", 10000);
            put("swv11",10000);
            put("swv13",10000);
            put("swv17",10000);
            put("swv18",10000);
            put("ta69", 10000);
            put("ta71", 10000);
            put("ta76", 10000);
            put("yn2",  1197);
        }
    };
}
