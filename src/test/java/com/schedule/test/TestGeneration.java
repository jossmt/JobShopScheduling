package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Schedule builder
 */
public class TestGeneration extends TestSetup {

    /** logger. */
    private static final Logger LOG = LoggerFactory.getLogger(TestGeneration.class);

    @Test
    public void generateBenchmarkInstance() {

        schedulesBuilder.getBenchmarkInstance("la17");
    }

    /**
     * Generate graph test.
     */
    @Test
    public void generateTest() {

        setUp("4x4", 1);

        scheduleService.generateGraphCode(optimal, "4x4Test");
    }


    /**
     * Generates larger graph.
     */
    @Test
    public void generateTestLarge() {

        setUp("la23", 1);

        scheduleService.generateGraphCode(optimal, "la23Test");
    }
}
