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

import java.util.ArrayList;
import java.util.Deque;

/**
 * Tests for {@link ScheduleService}
 */
public class ScheduleServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleServiceTest.class);

    /**
     * Asserts makespan values using pre-assessed makespan calculations.
     */
    @Test
    public void calculateMakespan() {

        final String[] benchmarks = {"ft06", "ft10", "ft20", "la01", "la10", "la11", "la23", "la34",
                "la35", "la36", "la37", "swv11", "swv13", "swv17", "swv18", "yn2", "yn3"};
        final String[] makespans = readFile(TestDataPaths.MAKESPAN_CALCULATIONS_PATH).split(",");

        Integer count = 0;
        for (final String benchmark : benchmarks) {
            setUp(benchmark, 1);

            LOG.debug("Makespan: {}", optimal.getMakespan());
            Truth.assertThat(Integer.valueOf(makespans[count])).isEqualTo(optimal.getMakespan());
            count++;
        }
    }

    /**
     * Asserts topological sort values using pre-assessed array
     */
    @Test
    public void topologicalSort() {

        setUp("ft10", 1);

        final Deque<Operation> operations = scheduleService.topologicalSort(optimal);

        Truth.assertThat(operations.toString()).isEqualTo(readFile(TestDataPaths.TOPOLOGICAL_SORT_PATH));
    }

    /**
     * Flips edge and then re-flips and asserts hashes equal.
     */
    @Test
    public void flipBackEdges() {

        setUp("ft10", 1);

        final Integer hashOriginal = optimal.hashCode();

        final Edge edgeChoice = optimal.getAllMachineEdgesManually().iterator().next();
        scheduleService.switchEdge(edgeChoice);
        scheduleService.switchEdge(edgeChoice);
        final Integer hash = optimal.hashCode();

        Truth.assertThat(hashOriginal).isEqualTo(hash);
    }

    /**
     * Asserts random edge is removed on selection.
     */
    @Test
    public void findRandomEdge() {

        setUp("la23", 1);

        final ArrayList<Edge> edges = optimal.getAllMachineEdgesManually();

        Integer size = edges.size();
        while (!edges.isEmpty()) {

            size--;
            scheduleService.findRandomEdge(edges);
            Truth.assertThat(edges.size()).isEqualTo(size);
        }

        Truth.assertThat(edges.isEmpty()).isTrue();
    }

    /**
     * Generates graph code and exports to png.
     */
    @Test
    public void generateGraphCode() {
        setUp("ft06", 1);
        scheduleService.generateGraphCode(optimal, "graphCodeTest");
    }

    /**
     * Operation order test.
     */
    @Test
    public void isInOrderTest() {
        final String[] benchmarks = {"ft06", "ft10", "ft20", "la01", "la10", "la11", "la23", "la34",
                "la35", "la36", "la37", "swv11", "swv13", "swv17", "swv18", "yn2", "yn3"};

        for (final String benchmark : benchmarks) {
            setUp(benchmark, 1);

            final Edge edge = optimal.getAllMachineEdgesManually().iterator().next();
            final boolean orderA = scheduleService.isInOrder(edge.getOperationFrom(), edge.getOperationTo());
            final boolean orderB = scheduleService.isInOrder(edge.getOperationTo(), edge.getOperationFrom());

            Truth.assertThat(orderA).isTrue();
            Truth.assertThat(orderB).isFalse();
        }
    }
}
