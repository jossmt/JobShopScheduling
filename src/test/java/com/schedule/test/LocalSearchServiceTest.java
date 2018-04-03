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

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchServiceTest.class);

    /** {@link LocalSearchService}. */
    private final LocalSearchService localSearchService = new LocalSearchService();

    /**
     * Asserts local optimal reached.
     */
    @Test
    public void executeLocalSearch() {

        setUp("la01", 1);

        localSearchService.executeLocalSearchIteratively(optimal, 1000);

        Truth.assertThat(optimal.getMakespan()).isEqualTo(879);
    }

    @Test
    public void executeLocalSearchTest() {

        setUp("ft10", 10);

        for (final Schedule schedule : testSchedules) {

            try {
                localSearchService.executeLocalSearchIteratively(schedule, 100);
            }catch(StackOverflowError e){
                e.printStackTrace();
                scheduleService.generateGraphCode(schedule, "cycle");
                break;
            }
            LOG.debug("Localmin: {}", schedule.getMakespan());
        }
    }
}
