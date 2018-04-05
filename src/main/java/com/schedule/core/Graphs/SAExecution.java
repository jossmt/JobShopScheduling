package com.schedule.core.Graphs;

import com.schedule.core.Graphs.FeasibleSchedules.DataGenerator.SchedulesBuilder;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
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

    /** {@link LocalSearchService}. */
    private static final LocalSearchService localSearchService = new LocalSearchService();

    /** {@link ScheduleService}. */
    private static final ScheduleService scheduleService = new ScheduleService();

    /** {@link SchedulesBuilder}. */
    private static final SchedulesBuilder schedulesBuilder = new SchedulesBuilder();

    /** {@link OptimalSchedule}. */
    private static final OptimalSchedule optimalSchedule = new OptimalSchedule();

    /** {@link SimulatedAnnealingService}. */
    private static final SimulatedAnnealingService simulatedAnnealingService = new SimulatedAnnealingService
            (optimalSchedule);

    public static void main(String[] args){

        optimalSchedule.addObserver(simulatedAnnealingService);

        final Set<Schedule> schedules = schedulesBuilder.generateStartingSchedules("la23", 1);

        final Schedule schedule = schedules.iterator().next();
        optimalSchedule.setOptimalSchedule(schedule);

        LOG.debug("Makespan before SA: {}", optimalSchedule.getOptimalSchedule().getMakespan());
        simulatedAnnealingService.manualShutdownExecutorService();

        LOG.debug("Makespan after SA: {}", optimalSchedule.getOptimalSchedule().getMakespan());
    }
}
