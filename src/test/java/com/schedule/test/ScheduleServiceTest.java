package com.schedule.test;


import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.test.Config.TestDataPaths;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Optional;
import java.util.Set;

public class ScheduleServiceTest extends TestSetup {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleServiceTest.class);

    @Test
    public void calculatePathsTest() {

        setUp("ft10", 1);

        scheduleService.calculateScheduleData(optimal);

        Truth.assertThat(optimal.getMachineEdgesNotOnLP().toString()).isEqualTo(readFile(TestDataPaths
                                                                                                 .CALCULATE_PATHS_PATH));
    }

    @Test
    public void calculatePathsInfeasibleScheduleTest() {

        setUp("ft10", 1);

        scheduleService.calculateScheduleData(optimal);

        Truth.assertThat(feasibilityService.hasCycle(optimal)).isFalse();

        final Set<Edge> edges = optimal.getMachineEdgesOnLPSet();

        Edge toModify = null;
        Edge child = null;
        for (final Edge edge : edges) {

            if (edge.getOperationTo().hasDisjunctiveEge()) {
                if (edge.getOperationTo().getDisjunctiveEdge().isMachinePath()) {

                    toModify = edge;
                    child = edge.getOperationTo().getDisjunctiveEdge();
                    break;
                }
            }
        }

        //Manually added loop
        child.getOperationTo().setDisjunctiveEdge(new Edge(child.getOperationTo(), toModify.getOperationFrom(), 100));

        Truth.assertThat(feasibilityService.hasCycle(optimal)).isTrue();

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

    @Test
    public void flipPopularEdgeTest() {

        setUp("ft10", 1);

        final String[] scheduleHashes = readFile(TestDataPaths.FLIP_EDGE_HASHES_PATH).split(",");

        for (final String hash : scheduleHashes) {

            scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal.getMachineEdgesOnLP(), false);
            scheduleService.calculateScheduleData(optimal);

            Truth.assertThat(Integer.valueOf(hash)).isEqualTo(optimal.hashCode());
            Truth.assertThat(feasibilityService.hasCycle(optimal)).isFalse();
        }
    }

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

    @Test
    public void testCalculateMachineEdgesOnLP(){

        setUp("ft06", 1);

        scheduleService.calculateScheduleData(optimal);

        LOG.debug("Machine edges: {}", optimal.getMachineEdgesOnLP());
        LOG.debug("Machine edges set: {}", optimal.getMachineEdgesOnLPSet());
    }
}
