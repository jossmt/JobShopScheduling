package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.FireflyService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ShutDownThreadsCallable implements Callable<Schedule> {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedAnnealingCallable.class);

    private SAFAService safaService;

    public ShutDownThreadsCallable(final SAFAService safaService) {
        this.safaService = safaService;
    }

    @Override
    public Schedule call() throws Exception {

        LOG.debug("Checking whether to shutdown");

        boolean completed = safaService.removeCompletedThreads();
        while (!completed) {
            LOG.debug("Threads still running, not shutting down");
            Thread.sleep(2000);
            completed = safaService.removeCompletedThreads();
        }

        safaService.shutDownExecutors();

        LOG.debug("Shutdown services.");

        return safaService.getOptimal();
    }
}
