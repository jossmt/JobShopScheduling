package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;

public class SimulatedAnnealingServiceTest extends TestSetup {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedAnnealingServiceTest.class);

    private final SimulatedAnnealingService simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule);

    @Test
    public void acceptanceProb() {

        int prevMakespan = 43;
        int newMakespan = 45;

        final Double startTemp = 1000.0;
        for (double temp = 1000; temp > 1; temp -= 100) {

            System.out.println(simulatedAnnealingService.acceptanceProbability(prevMakespan, newMakespan, temp,
                                                                               startTemp));
        }

    }

    @Test
    public void SAManip() {

        setUp("4x4", 1);

        optimalSchedule.setOptimalScheduleWithoutNotifyingObservers(optimal);

        simulatedAnnealingService.executeSimulatedAnnealing(optimal);

    }

    @Test
    public void testTabuList() {

        setUp("4x4", 1);

        final ArrayList<Edge> edgesOnLongest = optimal.getMachineEdgesOnLP();
        optimal.initialiseCache();

        Optional<Edge> edgeOptional = scheduleService.getMostVisitedEdgeLongestPath(optimal, edgesOnLongest, true);

        for (int i = 0; i < 20; i++) {

            edgesOnLongest.remove(edgeOptional.get());
            edgeOptional = scheduleService.getMostVisitedEdgeLongestPath(optimal, edgesOnLongest, true);
        }

        LOG.debug("Optimal cache: {}", optimal.getLruEdgeCache().toString());

        scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal.getMachineEdgesOnLP(), true);

        LOG.debug("Optimal Cache After edge flip: {}", optimal.getLruEdgeCache().toString());

        scheduleService.flipMostVisitedEdgeLongestPath(optimal, optimal.getMachineEdgesOnLP(), true);

        Truth.assertThat(optimal.getLruEdgeCache().size()).isAtMost(4);
    }
}
