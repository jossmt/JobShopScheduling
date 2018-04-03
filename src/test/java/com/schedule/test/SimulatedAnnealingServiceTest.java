package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import com.schedule.test.Config.TestDataPaths;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Tests for {@link SimulatedAnnealingService}
 */
public class SimulatedAnnealingServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SimulatedAnnealingServiceTest.class);

    /** {@link SimulatedAnnealingService}. */
    private final SimulatedAnnealingService simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule);

    /**
     * Test tabu list functionality (test the caching of previously flipped edges).
     */
    @Test
    public void testTabuList() {

        setUp("ft06", 1);

        final ArrayList<Edge> edgesOnLongest = optimal.getMachineEdgesOnLP();
        optimal.initialiseCache();

        Optional<Edge> edgeOptional = scheduleService.getMostVisitedEdgeLongestPath(optimal, edgesOnLongest, true);

        for (int i = 0; i < 20; i++) {
            if (!edgeOptional.isPresent()) {
                break;
            }
            LOG.debug("Edge: {}", edgeOptional.get().toString());
            edgesOnLongest.remove(edgeOptional.get());
            edgeOptional = scheduleService.getMostVisitedEdgeLongestPath(optimal, edgesOnLongest, true);
        }

        LOG.debug("Optimal cache: {}", optimal.getLruEdgeCache().toString());

        scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal.getMachineEdgesOnLP(), true);

        LOG.debug("Optimal Cache After edge flip: {}", optimal.getLruEdgeCache().toString());

        scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal.getMachineEdgesOnLP(), true);

        Truth.assertThat(optimal.getLruEdgeCache().size()).isAtMost(6);
    }

    /**
     * Test tabu list cache functionality.
     */
    @Test
    public void testTabuListCache() {

        setUp("ft10", 1);
        optimal.initialiseCache();

        Optional<Edge> edgeOptional = scheduleService.getMostVisitedEdgeLongestPath(optimal, optimal
                .getMachineEdgesOnLP(), true);

        scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal.getMachineEdgesOnLP(), true);
        LOG.debug("Optimal cache: {}", optimal.getLruEdgeCache().toString());

        final String cacheTestData = readFile(TestDataPaths.LRU_CACHE_PATH);
        Truth.assertThat(cacheTestData).isEqualTo(optimal.getLruEdgeCache().toString());
    }
}
