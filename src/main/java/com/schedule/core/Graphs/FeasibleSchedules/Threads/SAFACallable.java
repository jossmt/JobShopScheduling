package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class SAFACallable implements Callable<Schedule> {

    private static final Logger LOG = LoggerFactory.getLogger(SAFACallable.class);

    private SAFAService safaService;
    private Schedule schedule;

    public SAFACallable(final SAFAService safaService, final Schedule schedule) {
        this.safaService = safaService;
        this.schedule = schedule;
    }

    @Override
    public Schedule call() throws Exception {

        LOG.debug("Starting new SAFA thread");

        safaService.iterateAndUpdateOptimalFirefly(schedule);

        LOG.debug("Finished SAFA thread.");

        return safaService.getOptimal();
    }
}
