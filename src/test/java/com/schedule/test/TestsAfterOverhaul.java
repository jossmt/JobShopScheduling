package com.schedule.test;

import com.rits.cloning.Cloner;
import com.schedule.core.Graphs.FeasibleSchedules.Config.AlgorithmParameters;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Services;
import com.schedule.core.Graphs.FeasibleSchedules.Service.FireflyService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

public class TestsAfterOverhaul extends TestSetup {

    private static final Logger LOG = LoggerFactory.getLogger(TestsAfterOverhaul.class);

    private LocalSearchService localSearchService = new LocalSearchService();

    private FireflyService fireflyService = new FireflyService(optimalSchedule);

    private SimulatedAnnealingService simulatedAnnealingService;

    private SAFAService safaService;

    @Test
    public void SATest() {
        instantiateServices("ft06");
        setUp("ft06", 2);

        optimalSchedule.setOptimalSchedule(optimal, Services.LOCAL_SEARCH);

        simulatedAnnealingService.iterateAndUpdateOptimal(testSchedules.iterator().next());
    }

    /**
     * Tests movement of schedule toward beacon (black box)
     */
    @Test
    public void moveToOptimal() {
        instantiateServices("ft10");
        setUp("ft10", 2);

        LOG.debug("Optimal Hash: {}", optimal.hashCode());

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        final Schedule test = testSchedules.iterator().next();

        LOG.debug("Test Hash: {}", test.hashCode());

        int count = 0;
        while (test.hashCode() != optimal.hashCode()) {

            LOG.debug("Iteration: {}", count);

            fireflyService.moveToOptimalNew(test);

            count++;
        }

        LOG.debug("Hash optimal: {}, hash local: {}", optimal.hashCode(), test.hashCode());
    }

    /**
     * Tests movement of schedule toward beacon (black box)
     */
    @Test
    public void moveToOptimal2() {
        instantiateServices("la23");
        setUp("la23", 50);


        LOG.debug("Optimal Hash: {}", optimal.hashCode());

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        for (final Schedule test : testSchedules) {

            LOG.trace("Test Hash: {}", test.hashCode());

            int count = 0;
            while (test.hashCode() != optimal.hashCode()) {

                LOG.trace("Iteration: {}", count);

                fireflyService.moveToOptimalNew(test);

                count++;
            }

            LOG.debug("Hash optimal: {}, hash local: {}", optimal.hashCode(), test.hashCode());
        }
    }

    /**
     * Tests movement of schedule toward beacon (black box)
     */
    @Test
    public void moveToOptimal3() {
        instantiateServices("swv11");
        setUp("swv11", 2);
        LOG.debug("Optimal Hash: {}", optimal.hashCode());

        localSearchService.executeLocalSearchIteratively(optimal, 1000);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        final Schedule test = testSchedules.iterator().next();

        int count = 0;
        while (test.hashCode() != optimal.hashCode()) {

            LOG.debug("Iteration: {}", count);

            fireflyService.moveToOptimalNew(test);

            count++;
        }
    }

    @Test
    public void SAFAserviceTest() {
        instantiateServices("swv11");
        setUp("swv11", 100);

        localSearchService.executeLocalSearch(testSchedules, 500);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(localSearchService.getOptimalSchedule());

        safaService.iterativeApproachSAFA(testSchedules);
    }

    @Test
    public void checkNumberOfRequiredIterations() {
        instantiateServices("swv11");
        setUp("swv11", 2);

        optimalSchedule.setOptimalSchedule(optimal, Services.LOCAL_SEARCH);

        safaService.iterativeApproachSAFA(testSchedules);
    }

    @Test
    public void calculateSAParams() {

        Double temp = 10000.0;
        Double coolingRate = 0.001;

        int count = 0;
        while (temp > 1) {
            count++;
            temp *= 1 - coolingRate;
        }

        LOG.debug("Count: {}", count);
    }

    @Test
    public void localSearchIterations() {
        instantiateServices("la23");
        setUp("la23", 30);

        int priortotal = 0;
        int total = 0;
        int count = 0;
        final long start = System.currentTimeMillis();
        for (final Schedule schedule : testSchedules) {

            priortotal += schedule.getMakespan();
            localSearchService.executeLocalSearchIteratively(schedule, Integer.MAX_VALUE);
            total += schedule.getMakespan();
            count++;
        }
        final long end = System.currentTimeMillis();
        final long timeElapsed = end-start;

        LOG.debug("Average makespan before: {}, after: {}", priortotal / count, total / count);
        LOG.debug("Time elapsed: {}", timeElapsed);
    }

    @Test
    public void cloningLargeObjects() {
        instantiateServices("ta76");
        setUp("ta76", 1);

        final Cloner cloner = new Cloner();

        final Schedule schedule = optimal;

        final Schedule schedule1 = cloner.deepClone(schedule);

    }

    /**
     * Instantiates services.
     */
    public void instantiateServices(final String benchmarkInstance) {

        final Double[] saParameters = AlgorithmParameters.saParameters.get(benchmarkInstance);

        simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule, saParameters[0], saParameters[1]);
        safaService = new SAFAService(fireflyService, simulatedAnnealingService, optimalSchedule, saParameters[0],
                                      saParameters[1]);
    }
}
