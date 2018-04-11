package com.schedule.core.Graphs;

import com.schedule.core.Graphs.FeasibleSchedules.Config.AlgorithmParameters;
import com.schedule.core.Graphs.FeasibleSchedules.DataGenerator.SchedulesBuilder;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Services;
import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.ScheduleService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Executes solely Simulated Annealing.
 */
public class SAExecution {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SAExecution.class);

    /** {@link SchedulesBuilder}. */
    private static final SchedulesBuilder schedulesBuilder = new SchedulesBuilder();

    /** {@link OptimalSchedule}. */
    private static final OptimalSchedule optimalSchedule = new OptimalSchedule();

    private static final LocalSearchService localSearchService = new LocalSearchService();

    /** {@link SimulatedAnnealingService}. */
    private static SimulatedAnnealingService simulatedAnnealingService;

    public static void main(String[] args) {

        final String benchmarkInstance = args[0];

        //Generates parameters given the benchmark instance
        final Double[] saParameters = AlgorithmParameters.saParameters.get(benchmarkInstance);

        final Double startTempSA = saParameters[0];
        final Double coolingRateSA = saParameters[1];

        //Instantiates services with generated execution parameters
        simulatedAnnealingService = new SimulatedAnnealingService(optimalSchedule, startTempSA, coolingRateSA);
        optimalSchedule.addObserver(simulatedAnnealingService);

        final Set<Schedule> schedules = schedulesBuilder.generateStartingSchedules(benchmarkInstance, 1);

        localSearchService.executeLocalSearch(schedules, Integer.MAX_VALUE);

        final Schedule schedule = schedules.iterator().next();
        optimalSchedule.setOptimalSchedule(schedule, Services.LOCAL_SEARCH);

        LOG.debug("Makespan before SA: {}", optimalSchedule.getOptimalSchedule().getMakespan());

        simulatedAnnealingService.manualShutdownExecutorService();
    }
}
