package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

/**
 * Tests for {@link LocalSearchService}
 * <p>
 * Note: LS takes advantage of randomisation therefore blackbox testing required.
 */
public class LocalSearchServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchServiceTest.class);

    /** {@link LocalSearchService}. */
    private final LocalSearchService localSearchService = new LocalSearchService();

    /**
     * Iteratively applies LS on each benchmark size and asserts new makespan is less.
     */
    @Test
    public void localSearchBlackBox() {

        final String[] benchmarks = {"ft06", "ft10", "swv11", "ta69"};

        for (final String benchmark : benchmarks) {
            setUp(benchmark, 1);

            final Integer makespan = optimal.getMakespan();
            localSearchService.executeLocalSearchIteratively(optimal, 30);

            final Integer localMinimaMakespan = optimal.getMakespan();

            Truth.assertThat(localMinimaMakespan).isLessThan(makespan);
        }
    }

    /**
     * Applies LS in a threaded fashion.
     */
    @Test
    public void localSearchThreaded() {
        setUp("ft10", 10);

        final Schedule schedule = Collections.min(testSchedules, Comparator.comparing(Schedule::getMakespan));

        final Integer maxRandomMakespan = schedule.getMakespan();

        //Executes LS on results
        localSearchService.executeLocalSearch(testSchedules, 30);

        final Schedule scheduleMinima = Collections.min(testSchedules, Comparator.comparing(Schedule::getMakespan));

        final Integer localMinima = scheduleMinima.getMakespan();
        if (!Objects.equals(maxRandomMakespan, localMinima)) {

            Truth.assertThat(localMinima).isLessThan(maxRandomMakespan);

            LOG.debug("Random Min: {}, Local Min: {}", maxRandomMakespan, localMinima);
        }
    }
}
