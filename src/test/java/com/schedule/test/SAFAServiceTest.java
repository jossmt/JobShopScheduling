package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Config.AlgorithmParameters;
import com.schedule.core.Graphs.FeasibleSchedules.Service.FireflyService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for Simulated Annealing-Firefly Algorithm service methods.
 */
public class SAFAServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SAFAServiceTest.class);

    /** {@link FireflyService}. */
    private final FireflyService fireflyService = new FireflyService(optimalSchedule);

    /** {@link SimulatedAnnealingService}. */
    private SimulatedAnnealingService simulatedAnnealingService;

    /** {@link SAFAService}. */
    private SAFAService safaService;

    /**
     * Setup.
     */
    public void setUp() {
        optimalSchedule.addObserver(safaService);
        optimalSchedule.addObserver(simulatedAnnealingService);
    }

    /**
     * SAFA black box test. (Threaded approach)
     */
    @Test
    public void SAFATest() {

        setUp("ft06", 10);
        instantiateServices("ft06");
        setUp();

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);
        final Integer prevMakespan = optimal.getMakespan();

        safaService.executeSimulatedAnnealingFirefly(testSchedules);

        LOG.debug("Optimal: {}", optimalSchedule.getOptimalSchedule().getMakespan());
        if (optimalSchedule.getOptimalSchedule().getMakespan() != prevMakespan) {
            Truth.assertThat(optimalSchedule.getOptimalSchedule().getMakespan()).isLessThan(prevMakespan);
        }
    }

    /**
     * SAFA blackbox test. (iterative approach)
     */
    @Test
    public void SAFATestIterative() {

        setUp("ft10", 10);
        instantiateServices("ft10");
        setUp();

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);
        final Integer prevMakespan = optimal.getMakespan();

        safaService.iterativeApproachSAFA(testSchedules);

        LOG.debug("Optimal: {}", optimalSchedule.getOptimalSchedule().getMakespan());
        if (optimalSchedule.getOptimalSchedule().getMakespan() != prevMakespan) {
            Truth.assertThat(optimalSchedule.getOptimalSchedule().getMakespan()).isLessThan(prevMakespan);
        }
    }

    /**
     * Instantiates services.
     */
    private void instantiateServices(final String benchmarkInstance) {

        final Double[] saParameters = AlgorithmParameters.saParameters.get(benchmarkInstance);

        simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule, saParameters[0], saParameters[1]);
        safaService = new SAFAService(fireflyService, simulatedAnnealingService, optimalSchedule, saParameters[0],
                                      saParameters[1]);
    }
}
