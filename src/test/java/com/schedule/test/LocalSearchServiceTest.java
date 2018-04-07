//package com.schedule.test;
//
//import com.google.common.truth.Truth;
//import com.rits.cloning.Cloner;
//import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
//import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
//import com.schedule.test.Config.TestDataPaths;
//import com.schedule.test.Config.TestSetup;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Set;
//
///**
// * Tests for {@link LocalSearchService}
// */
//public class LocalSearchServiceTest extends TestSetup {
//
//    /** Logger. */
//    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchServiceTest.class);
//
//    /** {@link LocalSearchService}. */
//    private final LocalSearchService localSearchService = new LocalSearchService();
//
//
//    @Test
//    public void executeLocalSearchNewMethod() {
//        setUp("ft06", 50);
//
//        Integer total = 0;
//        Integer sum = 0;
//        for(final Schedule schedule : testSchedules) {
//            localSearchService.executeLocalSearchIteratively(schedule, 1000);
//            total += schedule.getMakespan();
//            sum += 1;
//        }
//
//        LOG.debug("Average makespan LS result: {}", total/sum);
//    }
//
//    /**
//     * Asserts local optimal reached.
//     */
//    @Test
//    public void executeLocalSearch() {
//
//        setUp("la01", 5);
//
//        final String[] expectedMakespans = readFile(TestDataPaths.LOCAL_SEARCH_DATA_PATH_2).split(",");
//
//        testLSMakespans(expectedMakespans, testSchedules);
//    }
//
//    /**
//     * Asserts local optimal reached.
//     */
//    @Test
//    public void executeLocalSearch1() {
//
//        setUp("la23", 5);
//
//        final String[] expectedMakespans = readFile(TestDataPaths.LOCAL_SEARCH_DATA_PATH_3).split(",");
//
//        testLSMakespans(expectedMakespans, testSchedules);
//    }
//
//    /**
//     * Asserts local optimal reached.
//     */
//    @Test
//    public void executeLocalSearch3() {
//
//        setUp("ft10", 5);
//
//        final String[] expectedMakespans = readFile(TestDataPaths.LOCAL_SEARCH_DATA_PATH).split(",");
//
//        testLSMakespans(expectedMakespans, testSchedules);
//    }
//
//    private void testLSMakespans(final String[] expectedMakespans, final Set<Schedule> schedules) {
//
//        int count = 0;
//        for (final Schedule schedule : testSchedules) {
//            localSearchService.executeLocalSearchIteratively(schedule, 1000);
//            LOG.debug("Local search val: {}", schedule.getMakespan());
//            Truth.assertThat(Integer.valueOf(expectedMakespans[count])).isEqualTo(schedule.getMakespan());
//
//            count++;
//        }
//
//        Truth.assertThat(optimal.getMakespan()).isEqualTo(Integer.valueOf(expectedMakespans[expectedMakespans.length
//                - 1]));
//    }
//}
