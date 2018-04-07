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
 * Executes makespan optimisation process.
 */
public class Execution {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Execution.class);

    /** {@link LocalSearchService}. */
    private static final LocalSearchService localSearchService = new LocalSearchService();

    /** {@link ScheduleService}. */
    private static final ScheduleService scheduleService = new ScheduleService();

    /** {@link SchedulesBuilder}. */
    private static final SchedulesBuilder schedulesBuilder = new SchedulesBuilder();

    /** {@link OptimalSchedule}. */
    private static final OptimalSchedule optimalSchedule = new OptimalSchedule();

    /** {@link FireflyService}. */
    private static final FireflyService fireflyService = new FireflyService(optimalSchedule);

    /** {@link SimulatedAnnealingService}. */
    private static final SimulatedAnnealingService simulatedAnnealingService = new SimulatedAnnealingService
            (optimalSchedule);

    /** {@link SAFAService}. */
    private static final SAFAService safaService = new SAFAService(fireflyService, simulatedAnnealingService,
                                                                   optimalSchedule);

    /**
     * Executor
     */
    public static void main(String[] args) {

        optimalSchedule.addObserver(simulatedAnnealingService);
        optimalSchedule.addObserver(safaService);

        final String benchmarkInstance = "swv11";

        final StringBuilder resultBuilder = new StringBuilder();

        for (int i = 0; i < 1; i++) {

            LOG.debug("Generating starting schedules...");

            // Generate Schedules
            final Set<Schedule> scheduleSet = schedulesBuilder.generateStartingSchedules(benchmarkInstance, 200);

            LOG.debug("Finished generating schedules");

            // Execute Local Search
            final Set<Schedule> localOptimaSet = localSearchService.executeLocalSearch(scheduleSet, 100);

            // Executes SA on Optimal
            optimalSchedule.setOptimalSchedule(localSearchService.getOptimalSchedule());

            LOG.debug("Computed max local optimal: {}", optimalSchedule.getOptimalSchedule().getMakespan());

            //Executes SAFA
            safaService.iterativeApproachSAFA(localOptimaSet);

            //Result
            LOG.debug("Final: {}", optimalSchedule.getOptimalSchedule().getMakespan());
            resultBuilder.append(optimalSchedule.getOptimalSchedule().getMakespan()).append(",");

            if (BenchmarkLowerBounds.achieved.containsKey(benchmarkInstance)) {
                if (optimalSchedule.getOptimalSchedule().getMakespan() <=
                        BenchmarkLowerBounds.achieved.get(benchmarkInstance)) {
                    LOG.debug("NEW OPTIMUM FOUND: {}", optimalSchedule.getOptimalSchedule().getMakespan());
                    scheduleService.generateGraphCode(optimalSchedule.getOptimalSchedule(), benchmarkInstance +
                            "Optimal");

                }
            }

            //Restart thread executor
            simulatedAnnealingService.restartThreadExecutor();
            safaService.restartThreadExecutor();
        }

        LOG.debug("Results: benchmark: {}, values: {}", benchmarkInstance, resultBuilder.toString());
    }
}
