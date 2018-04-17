package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Callable function for SAFA method.s
 */
public class SAFACallable implements Callable<Schedule> {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SAFACallable.class);

    /** {@link SAFAService}. */
    private SAFAService safaService;

    /** {@link Schedule}. */
    private Schedule schedule;

    /**
     * SAFACallable Constructor.
     *
     * @param safaService
     *         {@link SAFAService}
     * @param schedule
     *         {@link Schedule}
     */
    public SAFACallable(final SAFAService safaService, final Schedule schedule) {
        this.safaService = safaService;
        this.schedule = schedule;
    }

    /**
     * Executor.
     */
    @Override
    public Schedule call() throws Exception {

        LOG.debug("Starting new SAFA thread");
        safaService.iterateAndUpdateOptimalFirefly(schedule);
        LOG.debug("Finished SAFA thread.");

        return safaService.getOptimal();
    }
}
