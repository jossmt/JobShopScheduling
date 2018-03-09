package com.schedule.test;

import com.google.common.truth.Truth;
import com.rits.cloning.Cloner;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link LocalSearchService}
 */
public class LocalSearchServiceTest extends TestSetup {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchServiceTest.class);

    private final LocalSearchService localSearchService = new LocalSearchService();

    @Test
    public void executeLocalSearch() {

        setUp("la01", 1);

        localSearchService.executeLocalSearchIteratively(optimal, 1000);

        Truth.assertThat(optimal.getMakespan()).isEqualTo(966);
    }

    @Test
    public void executeLocalSearchTest() {

        setUp("4x4", 500);

        final Cloner clone = new Cloner();

        int count = 0;
        for (final Schedule schedule : testSchedules) {

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            final Schedule before = clone.deepClone(schedule);

            LOG.debug("Sched count: {}", count);

            localSearchService.executeLocalSearchIteratively(schedule, 1000);

            count++;
        }
    }
}
