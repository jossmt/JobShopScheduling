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

    private static final Logger LOG = LoggerFactory.getLogger(FireflyServiceTest.class);

    private final FireflyService fireflyService = new FireflyService(optimalSchedule);

    /**
     * Tests movement of schedule toward beacon
     */
    @Test
    public void moveToOptimal() {

        setUp("3x3", 2);

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
     * Tests movement of schedule toward beacon
     */
    @Test
    public void moveToOptimalLarge() {

        setUp("4x4", 3);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        final String[] hashes = readFile(TestDataPaths.FIREFLY_MOVEMENT_PATH).split(",");

        final Schedule test = testSchedules.iterator().next();

        while (test.hashCode() != optimal.hashCode()) {

            fireflyService.moveToOptimalNew(test);
        }
    }

    @Test
    public void moveToOptimalNewTest() {

        setUp("la01", 10);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        for(final Schedule otherSchedule : testSchedules) {
            for (int i = 0; i < 500; i++) {

                final boolean val = fireflyService.moveToOptimalNew(otherSchedule);

                if (!val) {
                    LOG.debug("Equal");

                    LOG.debug("Hash code optimal: {}, hash code suboptimal: {}", optimal.hashCode(), otherSchedule.hashCode());

                    break;
                }
            }
        }
    }
}
