package com.schedule.core.Graphs;

import com.schedule.core.Graphs.FeasibleSchedules.Config.BenchmarkLowerBounds;
import com.schedule.core.Graphs.FeasibleSchedules.DataGenerator.SchedulesBuilder;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.*;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Executes makespan optimisation process for given machine size and starting tree set size.
 */
public class Execution {

    private static final Logger LOG = LoggerFactory.getLogger(Execution.class);

    private static final LocalSearchService localSearchService = new LocalSearchService();

    private static final ScheduleService scheduleService = new ScheduleService();

    /** {@link SchedulesBuilder}. */
    private static final SchedulesBuilder schedulesBuilder = new SchedulesBuilder();

    private static final OptimalSchedule optimalSchedule = new OptimalSchedule();

    private static final FireflyService fireflyService = new FireflyService(optimalSchedule);

    private static final SimulatedAnnealingService simulatedAnnealingService = new SimulatedAnnealingService
            (optimalSchedule);

    private static final SAFAService safaService = new SAFAService(fireflyService, simulatedAnnealingService,
                                                                   optimalSchedule);

    public static void main(String[] args) {

        optimalSchedule.addObserver(simulatedAnnealingService);
        optimalSchedule.addObserver(safaService);

        final String benchmarkInstance = "ft06";

        // Generate Schedules
        final Set<Schedule> scheduleSet = schedulesBuilder.generateStartingSchedules(benchmarkInstance, 200);

        // Execute Local Search
        final Set<Schedule> localOptimaSet = localSearchService.executeLocalSearch(scheduleSet);

        // Executes SA on Optimal
        fireflyService.computeOptimal(localOptimaSet, false, localSearchService.getOptimalSchedule());

        LOG.debug("Computed optimal: {}", optimalSchedule.getOptimalSchedule().getMakespan());

        //Executes SAFA
        safaService.iterativeApproachSAFA(localOptimaSet);

        //Result
        LOG.debug("Final: {}", optimalSchedule.getOptimalSchedule().getMakespan());

        if (optimalSchedule.getOptimalSchedule().getMakespan() <=
                BenchmarkLowerBounds.achieved.get(benchmarkInstance)) {
            LOG.debug("NEW OPTIMUM FOUND: {}", optimalSchedule.getOptimalSchedule().getMakespan());
            scheduleService.generateGraphCode(optimalSchedule.getOptimalSchedule(), benchmarkInstance + "Optimal");
        }
    }
}
