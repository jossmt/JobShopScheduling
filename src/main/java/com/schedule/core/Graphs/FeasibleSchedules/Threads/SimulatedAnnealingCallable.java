package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Callable for SA method.
 */
public class SimulatedAnnealingCallable implements Callable<Schedule> {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SimulatedAnnealingCallable.class);

    /** {@link SimulatedAnnealingService}. */
    private SimulatedAnnealingService simulatedAnnealingService;

    /** {@link Schedule} on which to execute SA. */
    private Schedule schedule;

    /**
     * SACallable Constructor.
     *
     * @param simulatedAnnealingService
     *         {@link SimulatedAnnealingService}
     * @param schedule
     *         {@link Schedule}
     */
    public SimulatedAnnealingCallable(final SimulatedAnnealingService simulatedAnnealingService,
                                      final Schedule schedule) {
        this.simulatedAnnealingService = simulatedAnnealingService;
        this.schedule = schedule;
    }

    /**
     * Executor.
     */
    @Override
    public Schedule call() throws Exception {

        LOG.debug("Starting new SA thread");

        simulatedAnnealingService.iterateAndUpdateOptimal(schedule);

        LOG.debug("Finished SA thread.");

        return simulatedAnnealingService.getOptimal();
    }
}
