package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ShutDownThreadsSAOnlyCallable implements Callable<Schedule> {

    private static final Logger LOG = LoggerFactory.getLogger(ShutDownThreadsSAOnlyCallable.class);

    private SimulatedAnnealingService simulatedAnnealingService;

    public ShutDownThreadsSAOnlyCallable(final SimulatedAnnealingService simulatedAnnealingService) {
        this.simulatedAnnealingService = simulatedAnnealingService;
    }

    @Override
    public Schedule call() throws Exception {

        LOG.debug("Checking whether to shutdown");
        simulatedAnnealingService.shuttingDownService();

        boolean completedSA = simulatedAnnealingService.removeCompletedThreads();
        while (!(completedSA)) {
            LOG.debug("Threads still running, not shutting down");
            Thread.sleep(2000);
            completedSA = simulatedAnnealingService.removeCompletedThreads();
        }

        simulatedAnnealingService.shutdownExecutorService();

        LOG.debug("Shutdown services.");

        LOG.debug("Optimal: {}", simulatedAnnealingService.getOptimal().getMakespan());

        return simulatedAnnealingService.getOptimal();
    }
}
