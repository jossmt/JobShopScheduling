//package com.schedule.test;
//
//import com.google.common.truth.Truth;
//import com.schedule.core.Graphs.FeasibleSchedules.Service.FireflyService;
//import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
//import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
//import com.schedule.test.Config.TestSetup;
//import org.junit.Before;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Test for Simulated Annealing-Firefly Algorithm service methods.
// */
//public class SAFAServiceTest extends TestSetup {
//
//    /** Logger. */
//    private static final Logger LOG = LoggerFactory.getLogger(SAFAServiceTest.class);
//
//    /** {@link FireflyService}. */
//    private final FireflyService fireflyService = new FireflyService(optimalSchedule);
//
//    /** {@link SimulatedAnnealingService}. */
//    private final SimulatedAnnealingService simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule);
//
//    /** {@link SAFAService}. */
//    private final SAFAService safaService = new SAFAService(fireflyService, simulatedAnnealingService, optimalSchedule);
//
//    /**
//     * Setup.
//     */
//    @Before
//    public void setUp() {
//        optimalSchedule.addObserver(safaService);
//        optimalSchedule.addObserver(simulatedAnnealingService);
//    }
//
//    /**
//     * Simple SAFA black box test. (Threaded approach)
//     */
//    @Test
//    public void SAFATest() {
//
//        setUp("ft10", 10);
//
//        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);
//        final Integer prevMakespan = optimal.getMakespan();
//
//        safaService.executeSimulatedAnnealingFirefly(testSchedules);
//
//        LOG.debug("Optimal: {}", optimalSchedule.getOptimalSchedule().getMakespan());
//        Truth.assertThat(optimalSchedule.getOptimalSchedule().getMakespan()).isLessThan(prevMakespan);
//    }
//
//    /**
//     * Simple SAFA blackbox test. (iterative approach)
//     */
//    @Test
//    public void SAFATestIterative() {
//
//        setUp("ft10", 10);
//
//        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);
//        final Integer prevMakespan = optimal.getMakespan();
//        LOG.debug("Previous Makespan: {}", prevMakespan);
//
//        safaService.iterativeApproachSAFA(testSchedules);
//
//        LOG.debug("Optimal: {}", optimalSchedule.getOptimalSchedule().getMakespan());
//        Truth.assertThat(optimalSchedule.getOptimalSchedule().getMakespan()).isLessThan(prevMakespan);
//    }
//}
