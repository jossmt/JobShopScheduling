package com.schedule.test;

import com.schedule.core.Graphs.FeasibleSchedules.Service.FireflyService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import com.schedule.test.Config.TestSetup;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for Simulated Annealing-Firefly Algorithm service methods.
 */
public class SAFAServiceTest extends TestSetup {

    private final FireflyService fireflyService = new FireflyService(optimalSchedule);

    private final SimulatedAnnealingService simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule);

    private final SAFAService safaService = new SAFAService(fireflyService, simulatedAnnealingService, optimalSchedule);

    @Before
    public void setUp() {
        optimalSchedule.addObserver(safaService);
        optimalSchedule.addObserver(simulatedAnnealingService);
    }


    @Test
    public void SAFATest() {

        setUp("ft20", 10);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        safaService.executeSimulatedAnnealingFirefly(testSchedules);
    }

    @Test
    public void SAFATestIterative() {

        setUp("ft10", 500);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        safaService.iterativeApproachSAFA(testSchedules);

        System.out.println(optimalSchedule.getOptimalSchedule().getMakespan());
    }

    public void SAFATestIterative2() {

        setUp("ft10", 10);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        safaService.iterativeApproachSAFA(testSchedules);
    }
}
