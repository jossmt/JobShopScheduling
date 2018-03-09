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

import java.util.Set;

/**
 * Tests for {@link FeasibilityService}
 */
public class FeasibilityServiceTest extends TestSetup {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FeasibilityServiceTest.class);

    /**
     * Asserts feasibility check functions following single edge flips
     */
    @Test
    public void feasibilityTestExtended() {

        setUp("ft10", 1);

        Integer successSwitchCount = 0;
        Integer failureSwitchCount = 0;

        final Set<Edge> machineEdgesLongestPath = optimal.getAllMachineEdgesNotOnLongestPath();
        LOG.trace("Edge options: {}", machineEdgesLongestPath);

        for (final Edge edge : machineEdgesLongestPath) {

            final Operation from = edge.getOperationFrom();
            final Operation to = edge.getOperationTo();

            LOG.debug("Edge: {}", edge);
            scheduleService.switchEdge(edge);
            scheduleService.calculateScheduleData(optimal);

            if (feasibilityService.hasCycle(optimal)) {

                LOG.debug("Cycle is infeasible");
                Truth.assertThat(feasibilityService.scheduleIsFeasibleProof(from, to)).isFalse();

                failureSwitchCount++;

                //undo flip edge
                scheduleService.switchEdge(edge);
                scheduleService.calculateScheduleData(optimal);
            } else {

                successSwitchCount++;
                Truth.assertThat(feasibilityService.scheduleIsFeasibleProof(from, to)).isTrue();
            }
        }

        LOG.debug("Ratio of success:failure edge flips: {}:{}", successSwitchCount, failureSwitchCount);
    }


    /**
     * Evaluates the ratio of feasible/infeasible schedules following edge switches not on longest path
     */
    @Test
    public void successfulSwitchRatios() {

        final String[] benchmarks = {"4x4", "ft06", "ft10", "ft20"};

        for (final String machineJob : benchmarks) {


            setUp(machineJob, 10);

            final String[] averageRatios = new String[10];

            int count = 0;
            for (final Schedule currentSchedule : testSchedules) {
                Integer successSwitchCount = 0;
                Integer failureSwitchCount = 0;

                final Set<Edge> machineEdgesLongestPath = currentSchedule.getAllMachineEdgesNotOnLongestPath();
                LOG.trace("Edge options: {}", machineEdgesLongestPath);

                for (final Edge edge : machineEdgesLongestPath) {

                    final Operation from = edge.getOperationFrom();
                    final Operation to = edge.getOperationTo();

                    LOG.trace("Edge: {}", edge);
                    scheduleService.switchEdge(edge);
                    scheduleService.calculateScheduleData(currentSchedule);

                    if (!(feasibilityService.scheduleIsFeasibleProof(from, to))) {

                        failureSwitchCount++;

                        //undo flip edge
                        scheduleService.switchEdge(edge);
                        scheduleService.calculateScheduleData(currentSchedule);
                    } else {

                        successSwitchCount++;
                    }
                }

                averageRatios[count] = successSwitchCount + "/" + (successSwitchCount + failureSwitchCount);
                count++;
            }

            LOG.debug("For Benchmark: {} ratios were: {}", machineJob, averageRatios);
        }
    }

    @Test
    public void feasibilityTest() {

        setUp("ft06", 2);

        final Schedule testSchedule = testSchedules.iterator().next();

        LOG.debug("Edge options: {}", testSchedule.getAllMachineEdgesNotOnLongestPath());

        int count = 0;
        for (final Edge edge : testSchedule.getAllMachineEdgesNotOnLongestPath()) {

            if (count == 0) {
                final Operation from = edge.getOperationFrom();
                final Operation to = edge.getOperationTo();

                LOG.debug("Edge: {}", edge);
                scheduleService.switchEdge(edge);
                scheduleService.calculateScheduleData(testSchedule);

                Truth.assertThat(feasibilityService.scheduleIsFeasibleProof(from, to)).isTrue();
            }

            if (count == 1) {

                final Operation from = edge.getOperationFrom();
                final Operation to = edge.getOperationTo();

                LOG.debug("Edge: {}", edge);
                scheduleService.switchEdge(edge);
                scheduleService.calculateScheduleData(testSchedule);

                LOG.debug("Has cycle First: {}", feasibilityService.hasCycle(testSchedule));
                Truth.assertThat(feasibilityService.scheduleIsFeasibleProof(from, to)).isFalse();

                final Operation cycleParent = testSchedule.locateOperation(2, 0);
                scheduleService.switchEdge(cycleParent.getDisjunctiveParent());
                scheduleService.calculateScheduleData(testSchedule);

                LOG.debug("Has Cycle Final: {}", feasibilityService.hasCycle(testSchedule));
            }
            count++;
        }
    }

    @Test
    public void feasibilityTest4x4() {

        setUp("4x4", 1);

//        scheduleService.generateGraphCode(optimal);

        LOG.debug("Edge options: {}", optimal.getAllMachineEdgesNotOnLongestPath());

        int count = 0;
        for (final Edge edge : optimal.getAllMachineEdgesNotOnLongestPath()) {

            if (count == 1) {

                final Operation from = edge.getOperationFrom();
                final Operation to = edge.getOperationTo();

                LOG.debug("Edge: {}", edge);
                scheduleService.switchEdge(edge);
                scheduleService.calculateScheduleData(optimal);

                LOG.debug("Has cycle: {}", feasibilityService.hasCycle(optimal));
                Truth.assertThat(feasibilityService.scheduleIsFeasibleProof(from, to)).isFalse();

                final Operation cycleOperation = optimal.locateOperation(0, 1);
                final Operation cycleOperation2 = cycleOperation.getDisjunctiveParent().getOperationTo();

                scheduleService.switchEdge(cycleOperation.getDisjunctiveParent());
                scheduleService.calculateScheduleData(optimal);

                LOG.debug("Has cycle: {}", feasibilityService.hasCycle(optimal));
                LOG.debug("Feasible: {}", feasibilityService.scheduleIsFeasibleProof(cycleOperation, cycleOperation2));
            }
            count++;
        }
    }
}
