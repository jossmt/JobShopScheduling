//package com.schedule.test;
//
//import com.google.common.truth.Truth;
//import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
//import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
//import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
//import com.schedule.core.Graphs.FeasibleSchedules.Service.FeasibilityService;
//import com.schedule.test.Config.TestSetup;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.Set;
//
///**
// * Tests for {@link FeasibilityService}
// */
//public class FeasibilityServiceTest extends TestSetup {
//
//    /** Logger. */
//    private static final Logger LOG = LoggerFactory.getLogger(FeasibilityServiceTest.class);
//
//    /**
//     * Asserts feasibility check functions following single edge flips
//     */
//    @Test
//    public void feasibilityTestExtended() {
//
//        setUp("ft10", 1);
//
//        final ArrayList<Edge> machineEdges = optimal.getAllMachineEdgesManually();
//    }
//
//
//    /**
//     * Evaluates the ratio of feasible/infeasible schedules following edge switches not on longest path
//     */
//    @Test
//    public void successfulSwitchRatios() {
//
//        final String[] benchmarks = {"ft06", "ft10", "la23"};
//
//        for (final String benchmark : benchmarks) {
//
//            setUp(benchmark, 10);
//
//            final String[] averageRatios = new String[9];
//
//            int count = 0;
//            for (final Schedule currentSchedule : testSchedules) {
//                Integer successSwitchCount = 0;
//                Integer failureSwitchCount = 0;
//
//                final Set<Edge> machineEdgesNotOnLongestPath = currentSchedule.getMachineEdgesNotOnLP();
//                LOG.trace("Edge options: {}", machineEdgesNotOnLongestPath);
//
//                int countcount = 0;
//                for (final Edge edge : machineEdgesNotOnLongestPath) {
//
//                    LOG.trace("edge: {}", edge);
//                    scheduleService.switchEdge(edge);
//
//                    final Operation from = edge.getOperationFrom();
//                    final Operation to = edge.getOperationTo();
//
//                    if (feasibilityService.scheduleIsFeasibleProof(from, to)) {
//
//                        successSwitchCount++;
//                    } else {
//
//                        failureSwitchCount++;
//                    }
//
//                    //undo flip edge
//                    scheduleService.switchEdge(edge);
//                    scheduleService.calculateScheduleData(currentSchedule);
//
//                    countcount++;
//                }
//
//                averageRatios[count] = successSwitchCount + "/" + (successSwitchCount + failureSwitchCount);
//                count++;
//            }
//
//            LOG.debug("For Benchmark: {} ratios were: {}", benchmark, averageRatios);
//        }
//    }
//
//    /**
//     * Forced cycle test.
//     */
//    @Test
//    public void feasibilityTest() {
//
//        setUp("ft06", 2);
//
//        final Schedule testSchedule = testSchedules.iterator().next();
//
//        LOG.debug("Edge options: {}", testSchedule.getMachineEdgesNotOnLP());
//
//        int count = 0;
//        for (final Edge edge : testSchedule.getMachineEdgesNotOnLP()) {
//
//            if (count == 0) {
//                final Operation from = edge.getOperationFrom();
//                final Operation to = edge.getOperationTo();
//
//                LOG.trace("Edge: {}", edge);
//                scheduleService.switchEdge(edge);
//                scheduleService.calculateScheduleData(testSchedule);
//
//                Truth.assertThat(feasibilityService.scheduleIsFeasibleProof(from, to)).isTrue();
//            }
//
//            if (count == 1) {
//
//                final Operation from = edge.getOperationFrom();
//                final Operation to = edge.getOperationTo();
//
//                LOG.trace("Edge: {}", edge);
//                scheduleService.switchEdge(edge);
//                scheduleService.calculateScheduleData(testSchedule);
//
//                LOG.trace("Has cycle First: {}", feasibilityService.hasCycle(testSchedule));
//                Truth.assertThat(feasibilityService.scheduleIsFeasibleProof(from, to)).isTrue();
//
//                final Operation cycleParent = testSchedule.locateOperation(2, 0);
//                scheduleService.switchEdge(cycleParent.getDisjunctiveParent());
//                scheduleService.calculateScheduleData(testSchedule);
//
//                LOG.trace("Has Cycle Final: {}", feasibilityService.hasCycle(testSchedule));
//            }
//            count++;
//        }
//    }
//
//    /**
//     * Manually creates cycle and tests if cycle is detected.
//     */
//    @Test
//    public void calculatePathsInfeasibleScheduleTest() {
//
//        setUp("ft10", 1);
//
//        scheduleService.calculateScheduleData(optimal);
//
//        Truth.assertThat(feasibilityService.hasCycle(optimal)).isFalse();
//
//        final Set<Edge> edges = optimal.getMachineEdgesOnLPSet();
//
//        Edge toModify = null;
//        Edge child = null;
//        for (final Edge edge : edges) {
//
//            if (edge.getOperationTo().hasDisjunctiveEge()) {
//                if (edge.getOperationTo().getDisjunctiveEdge().isMachinePath()) {
//
//                    toModify = edge;
//                    child = edge.getOperationTo().getDisjunctiveEdge();
//                    break;
//                }
//            }
//        }
//
//        //Manually added loop
//        child.getOperationTo().setDisjunctiveEdge(new Edge(child.getOperationTo(), toModify.getOperationFrom(), 100));
//
//        Truth.assertThat(feasibilityService.hasCycle(optimal)).isTrue();
//    }
//}
