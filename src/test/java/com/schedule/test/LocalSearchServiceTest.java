package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;

/**
 * Tests for {@link LocalSearchService}
 */
public class LocalSearchServiceTest extends TestSetup {

    private final LocalSearchService localSearchService = new LocalSearchService();

    @Test
    public void executeLocalSearch() {

        setUp("ft10", 1);

        localSearchService.executeLocalSearchIteratively(optimal, 1000);

        Truth.assertThat(optimal.getMakespan()).isEqualTo(1233);
    }
}
