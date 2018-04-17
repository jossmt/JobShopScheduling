package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.FireflyService;
import com.schedule.test.Config.TestDataPaths;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests of Firefly Service
 */
public class FireflyServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FireflyServiceTest.class);

    /** {@link FireflyService}. */
    private final FireflyService fireflyService = new FireflyService(optimalSchedule);

    /**
     * Tests movement of schedule toward beacon (black box)
     * Movement has been assessed manually prior.
     */
    @Test
    public void moveToOptimal() {

        setUp("ft06", 2);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        final String[] hashes = readFile(TestDataPaths.FIREFLY_MOVEMENT_PATH).split(",");
        final Schedule test = testSchedules.iterator().next();

        int count = 0;
        while (test.hashCode() != optimal.hashCode()) {

            Truth.assertThat(Integer.valueOf(hashes[count])).isEqualTo(test.hashCode());
            fireflyService.moveToOptimalNew(test);

            count++;
        }

        //Checks final instance equals optimal
        Truth.assertThat(test.hashCode()).isEqualTo(optimal.hashCode());

    }

    /**
     * Tests movement of schedule toward beacon (black box).
     * Movement has been assessed manually prior.
     */
    @Test
    public void moveToOptimalLarger() {

        setUp("swv11", 2);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        final String[] hashes = readFile(TestDataPaths.FIREFLY_MOVEMENT_LARGER_PATH).split(",");
        final Schedule test = testSchedules.iterator().next();

        int count = 0;
        while (test.hashCode() != optimal.hashCode()) {

            Truth.assertThat(Integer.valueOf(hashes[count])).isEqualTo(test.hashCode());
            fireflyService.moveToOptimalNew(test);

            count++;
        }

        //Checks final instance equals optimal
        Truth.assertThat(test.hashCode()).isEqualTo(optimal.hashCode());
    }
}
