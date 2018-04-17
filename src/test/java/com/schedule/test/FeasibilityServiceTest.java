package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.FeasibilityService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Tests for {@link FeasibilityService}
 */
public class FeasibilityServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FeasibilityServiceTest.class);

    /**
     * Evaluates the ratio of feasible/infeasible schedules following edge switches not on longest path
     */
    @Test
    public void successfulSwitchRatios() {

        final String[] benchmarks = {"ft06", "ft10", "la23"};

        for (final String benchmark : benchmarks) {

            setUp(benchmark, 10);

            final String[] averageRatios = new String[9];

            int count = 0;
            for (final Schedule currentSchedule : testSchedules) {
                Integer successSwitchCount = 0;
                Integer failureSwitchCount = 0;

                final ArrayList<Edge> machineEdges = currentSchedule.getAllMachineEdgesManually();

                for (final Edge edge : machineEdges) {

                    LOG.trace("edge: {}", edge);
                    scheduleService.switchEdge(edge);

                    final Operation from = edge.getOperationFrom();
                    final Operation to = edge.getOperationTo();

                    if (feasibilityService.scheduleIsFeasibleProof(from, to)) {

                        successSwitchCount++;
                    } else {

                        failureSwitchCount++;
                    }

                    //undo flip edge
                    scheduleService.switchEdge(edge);
                }

                averageRatios[count] = successSwitchCount + "/" + (successSwitchCount + failureSwitchCount);
                count++;
            }

            LOG.debug("For Benchmark: {} ratios were: {}", benchmark, averageRatios);
        }
    }

    /**
     * Checks functionality of feasibility proof test against a BFS cycle test.
     */
    @Test
    public void feasibilityTest() {

        setUp("ft10", 1);

        final ArrayList<Edge> machineEdges = optimal.getAllMachineEdgesManually();

        for (final Edge edge : machineEdges) {

            LOG.debug("edge: {}", edge);
            scheduleService.switchEdge(edge);

            final Operation from = edge.getOperationFrom();
            final Operation to = edge.getOperationTo();

            if (feasibilityService.scheduleIsFeasibleProof(from, to)) {
                Truth.assertThat(feasibilityService.hasCycle(optimal)).isFalse();
            } else {
                Truth.assertThat(feasibilityService.hasCycle(optimal)).isTrue();
            }

            scheduleService.switchEdge(edge);
        }
    }
}
