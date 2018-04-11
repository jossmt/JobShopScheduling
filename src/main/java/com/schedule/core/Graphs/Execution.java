package com.schedule.core.Graphs;

import com.schedule.core.Graphs.FeasibleSchedules.Config.AlgorithmParameters;
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
    private static SimulatedAnnealingService simulatedAnnealingService;

    /** {@link SAFAService}. */
    private static SAFAService safaService;

    /**
     * Executor main method.
     */
    public static void main(String[] args) {

        //Benchmark instance to use
        final String benchmarkInstance = args[0];
        final Integer iterations = Integer.valueOf(args[1]);

        //Generates parameters given the benchmark instance
        final Integer startingPopulation = AlgorithmParameters.startingPopulationParameter.get(benchmarkInstance);
        final Integer localSearchMaxIterations = AlgorithmParameters.localSearchIterationsParameter.get
                (benchmarkInstance);
        final Double[] saParameters = AlgorithmParameters.saParameters.get(benchmarkInstance);

        final Double startTempSA = saParameters[0];
        final Double coolingRateSA = saParameters[1];

        //Instantiates services with generated execution parameters
        simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule, startTempSA, coolingRateSA);
        safaService = new SAFAService(fireflyService, simulatedAnnealingService, optimalSchedule, startTempSA,
                                      coolingRateSA);

        // Adds observer services to {@link OptimalSchedule}.
        optimalSchedule.addObserver(simulatedAnnealingService);
        optimalSchedule.addObserver(safaService);

        // Accumulates results for multiple executions.
        final StringBuilder resultBuilder = new StringBuilder();

        // Loop dictating number of times entire algorithm is to be run on a random instance.
        for (int i = 0; i < iterations; i++) {

            LOG.debug("Generating starting schedules...");

            // Generate Schedules
            final Set<Schedule> scheduleSet = schedulesBuilder.generateStartingSchedules(benchmarkInstance,
                                                                                         startingPopulation);

            LOG.debug("Finished generating schedules");

            // Execute Local Search
            final Set<Schedule> localOptimaSet = localSearchService.executeLocalSearch(scheduleSet,
                                                                                       localSearchMaxIterations);

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
