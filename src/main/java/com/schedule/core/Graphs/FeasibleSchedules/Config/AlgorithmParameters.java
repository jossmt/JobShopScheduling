package com.schedule.core.Graphs.FeasibleSchedules.Config;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmParameters {

    public static final Map<String, Integer> startingPopulationParameter = new HashMap<String, Integer>() {
        {
            put("ft06", 80);
            put("ft10", 300);
            put("la01", 300);
            put("la10", 300);
            put("la11", 300);
            put("la23", 300);
            put("swv11", 200);
        }
    };

    public static final Map<String, Integer> localSearchIterationsParameter = new HashMap<String, Integer>() {
        {
            put("ft06", 300);
            put("ft10", 300);
            put("la01", 300);
            put("la10", 300);
            put("la11", 300);
            put("la23", 500);
            put("swv11", 500);
        }
    };

    public static final Map<String, Double[]> saParameters = new HashMap<String, Double[]>() {
        {
            put("ft06", new Double[]{1000.0, 0.02});
            put("ft10", new Double[]{10000.0, 0.01});
            put("la01", new Double[]{1000.0, 0.01});
            put("la10", new Double[]{10000.0, 0.01});
            put("la11", new Double[]{100000.0, 0.01});
            put("la23", new Double[]{100000.0, 0.01});
            put("swv11", new Double[]{1000000.0, 0.001});
        }
    };
}
