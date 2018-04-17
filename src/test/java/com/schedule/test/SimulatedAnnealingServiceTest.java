package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Config.AlgorithmParameters;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Services;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Tests for {@link SimulatedAnnealingService}
 */
public class SimulatedAnnealingServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SimulatedAnnealingServiceTest.class);

    /** {@link SimulatedAnnealingService}. */
    private SimulatedAnnealingService simulatedAnnealingService;

    /**
     * Black box SA test.
     */
    @Test
    public void simulatedAnnealingTest() {

        setUp("ft10", 1);
        instantiateServices("ft10");

        optimalSchedule.setOptimalSchedule(optimal, Services.LOCAL_SEARCH);
        final Integer randomStartingMakespan = optimalSchedule.getOptimalSchedule().getMakespan();

        simulatedAnnealingService.iterateAndUpdateOptimal(optimal);

        final Integer localMinimal = optimalSchedule.getOptimalSchedule().getMakespan();
        if (!Objects.equals(randomStartingMakespan, localMinimal)) {

            Truth.assertThat(localMinimal).isLessThan(randomStartingMakespan);
        }
    }

    /**
     * Instantiates services.
     */
    private void instantiateServices(final String benchmarkInstance) {

        final Double[] saParameters = AlgorithmParameters.saParameters.get(benchmarkInstance);

        simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule, saParameters[0], saParameters[1]);
    }
}
