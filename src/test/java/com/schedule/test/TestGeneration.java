package com.schedule.test;

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
    public void generateBenchmarkInstance(){

        schedulesBuilder.getBenchmarkInstance("la17");
    }

    @Test
    public void generateTest(){

        setUp("4x4", 5);

        int count = 0;
        for(final Schedule schedule : testSchedules){

            scheduleService.generateGraphCode(schedule, "4x4test" + count);
            count++;
        }
    }
}
