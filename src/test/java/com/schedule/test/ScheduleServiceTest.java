package com.schedule.test;


import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Service.ScheduleService;
import com.schedule.test.Config.TestDataPaths;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for {@link ScheduleService}
 */
public class ScheduleServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleServiceTest.class);

    /**
     * Calculates paths and asserts the same as tested sample.
     */
    @Test
    public void calculatePathsTest() {

        setUp("ft10", 1);

        scheduleService.calculateScheduleData(optimal);

        Truth.assertThat(optimal.getMachineEdgesNotOnLP().toString()).isEqualTo(readFile(TestDataPaths
                                                                                                 .CALCULATE_PATHS_PATH));
    }

    @Test
    public void calculateMakespan() {

        setUp("ft10", 1);

        final Integer makespan = scheduleService.calculateMakeSpan(optimal);

        Truth.assertThat(makespan).isEqualTo(1799);
    }

    @Test
    public void topologicalSort() {

        setUp("ft10", 1);

        final Deque<Operation> operations = scheduleService.topologicalSort(optimal);

        Truth.assertThat(operations.toString()).isEqualTo(readFile(TestDataPaths.TOPOLOGICAL_SORT_PATH));
    }

    /**
     * Flips popular edges and asserts hash equal to previously assessed hashes.
     */
    @Test
    public void flipPopularEdgeTest() {

        setUp("ft10", 1);
        optimal.initialiseCache();

        final String[] scheduleHashes = readFile(TestDataPaths.FLIP_EDGE_HASHES_PATH).split(",");

        for (final String hash : scheduleHashes) {

            scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal.getMachineEdgesOnLP(), true);
            scheduleService.calculateScheduleData(optimal);

            Truth.assertThat(Integer.valueOf(hash)).isEqualTo(optimal.hashCode());
        }
    }

    /**
     * Flips edge and then re-flips and asserts hashes equal.
     */
    @Test
    public void flipBackEdges() {

        setUp("ft10", 1);

        final Integer hashCode = optimal.hashCode();
        final Optional<Edge> edgeFlipped = scheduleService.flipMostVisitedEdgeLongestPath(optimal,
                                                                                          optimal.getMachineEdgesOnLP()
                , false);
        scheduleService.calculateScheduleData(optimal);


        final Integer flippedHashCode = optimal.hashCode();
        Truth.assertThat(hashCode).isNotEqualTo(flippedHashCode);

        edgeFlipped.ifPresent(edge -> scheduleService.switchEdge(edge));
        scheduleService.calculateScheduleData(optimal);

        final Integer unFlippedHashCode = optimal.hashCode();
        Truth.assertThat(hashCode).isEqualTo(unFlippedHashCode);
    }

    @Test
    public void scheduleLRUCacheTest() {

        setUp("4x4", 1);

        optimal.initialiseCache();

        final Optional<Edge> edge = scheduleService.findMostVisitedEdge(optimal.getMachineEdgesOnLP());

        if (edge.isPresent()) {
            optimal.updateLruEdgeCache(edge.get());

            Truth.assertThat(optimal.getCachedEdgeAcceptanceProb(edge.get()).get()).isEqualTo(0.9);

            optimal.updateLruEdgeCache(edge.get());

            Truth.assertThat(optimal.getCachedEdgeAcceptanceProb(edge.get()).get()).isEqualTo(0.81);

            optimal.updateLruEdgeCache(edge.get());

            Truth.assertThat(optimal.getCachedEdgeAcceptanceProb(edge.get()).get()).isEqualTo(0.7290000000000001);
        } else {
            throw new IllegalStateException("Failed test");
        }

        final Edge edge1 = new Edge(null, null, 1);
        final Edge edge2 = new Edge(null, null, 2);
        final Edge edge3 = new Edge(null, null, 3);
        final Edge edge4 = new Edge(null, null, 4);
        final Edge edge5 = new Edge(null, null, 5);


        optimal.updateLruEdgeCache(edge1);
        LOG.debug("Cache size 1: {}", optimal.getLruEdgeCache().size());
        Truth.assertThat(optimal.getLruEdgeCache().size()).isEqualTo(2);
        optimal.updateLruEdgeCache(edge2);
        LOG.debug("Cache size 2: {}", optimal.getLruEdgeCache().size());
        Truth.assertThat(optimal.getLruEdgeCache().size()).isEqualTo(3);
        optimal.updateLruEdgeCache(edge3);
        LOG.debug("Cache size 3: {}", optimal.getLruEdgeCache().size());
        Truth.assertThat(optimal.getLruEdgeCache().size()).isEqualTo(4);
        optimal.updateLruEdgeCache(edge4);
        LOG.debug("Cache size 4: {}", optimal.getLruEdgeCache().size());
        Truth.assertThat(optimal.getLruEdgeCache().size()).isEqualTo(4);
        optimal.updateLruEdgeCache(edge5);
        LOG.debug("Cache size 5: {}", optimal.getLruEdgeCache().size());
        Truth.assertThat(optimal.getLruEdgeCache().size()).isEqualTo(4);

    }

    /**
     * Checks that the calculate maximal machine edge on LP is calculated correctly
     */
    @Test
    public void testCalculateMachineEdgesOnLP() {

        setUp("ft10", 1);
        optimal.initialiseCache();

        final String[] edges = readFile(TestDataPaths.MOST_VISITED_EDGE_PATH).split(",");

        for(final String edge : edges) {
            final Optional<Edge> result = scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal
                    .getMachineEdgesOnLP(), true);
            LOG.debug("Result: {}", result.toString());
            LOG.debug("Res: {}", edge);
            Truth.assertThat(result.toString().contains(edge)).isTrue();
            scheduleService.calculateScheduleData(optimal);
        }
    }
}
