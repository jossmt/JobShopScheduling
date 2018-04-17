package com.schedule.core.Graphs.FeasibleSchedules.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Benchmark execution parameters.
 */
public class AlgorithmParameters {

    /**
     * Starting firefly population.
     */
    public static final Map<String, Integer> startingPopulationParameter = new HashMap<String, Integer>() {
        {
            put("ft06", 80);
            put("ft10", 150);
            put("ft20", 150);

            put("la01", 150);
            put("la10", 150);
            put("la11", 150);
            put("la23", 150);
            put("la34", 120);
            put("la35", 120);
            put("la36", 120);
            put("la37", 120);

            put("swv11", 100);
            put("swv13", 100);
            put("swv17", 100);
            put("swv18", 100);

            put("ta69", 80);
            put("ta71", 80);
            put("ta76", 80);

            put("yn2", 120);
            put("yn3", 120);
        }
    };

    /**
     * Max local search iterations parameter.
     */
    public static final Map<String, Integer> localSearchIterationsParameter = new HashMap<String, Integer>() {
        {
            put("ft06", 30);
            put("ft10", 60);
            put("ft20", 60);

            put("la01", 30);
            put("la10", 50);
            put("la11", 60);
            put("la23", 80);

            put("la34", 100);
            put("la35", 100);
            put("la36", 100);
            put("la37", 100);

            put("swv11", 150);
            put("swv13", 150);
            put("swv17", 150);
            put("swv18", 150);

            put("ta69", 200);
            put("ta71", 200);
            put("ta76", 200);

            put("yn2", 100);
            put("yn3", 100);
        }
    };

    /**
     * Simulated Annealing temperature and cooling rate parameters.
     */
    public static final Map<String, Double[]> saParameters = new HashMap<String, Double[]>() {
        {
            //6x6 (around 200 iterations)
            put("ft06", new Double[]{1000.0, 0.03});
            //10x10 (around 400 iterations)
            put("ft10", new Double[]{1000.0, 0.015});
            //20x5 (Around 275 iterations)
            put("ft20", new Double[]{1000.0, 0.02});

            //10x5 (Around 250 iterations)
            put("la01", new Double[]{1000.0, 0.025});
            //15x5 (Around 275 iterations)
            put("la10", new Double[]{1000.0, 0.02});
            //20x5 (Around 275 iterations)
            put("la11", new Double[]{1000.0, 0.02});
            //15x10 (Around 500 iterations)
            put("la23", new Double[]{1000.0, 0.012});

            //30 x 10 (around 3k iterations)
            put("la34", new Double[]{1000.0, 0.002});
            put("la35", new Double[]{1000.0, 0.002});

            //15 x 15 (around 3k iterations)
            put("la36", new Double[]{1000.0, 0.002});
            put("la37", new Double[]{1000.0, 0.002});

            //50x10  (around 9k iterations)
            put("swv11", new Double[]{1000000.0, 0.001});
            put("swv13", new Double[]{10000.0, 0.001});
            put("swv17", new Double[]{10000.0, 0.001});
            put("swv18", new Double[]{10000.0, 0.001});

            //100 x 20
            put("ta69", new Double[]{1000000.0, 0.001});
            put("ta71", new Double[]{1000000.0, 0.001});
            put("ta76", new Double[]{1000000.0, 0.001});

            // 20x20 (around 2.5k iterations)
            put("yn2", new Double[]{1000.0, 0.0025});
            put("yn3", new Double[]{1000.0, 0.0025});

        }
    };
}
