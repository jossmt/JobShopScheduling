package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SAFAService;
import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Callable for Shutdown method.
 */
public class ShutDownThreadsCallable implements Callable<Schedule> {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ShutDownThreadsCallable.class);

    /** {@link SimulatedAnnealingService}. */
    private SimulatedAnnealingService simulatedAnnealingService;

    /** {@link SAFAService}. */
    private SAFAService safaService;

    /**
     * Constructor for Shutdown Thread callable.s
     *
     * @param simulatedAnnealingService
     *         {@link SimulatedAnnealingService}
     * @param safaService
     *         {@link SAFAService}
     */
    public ShutDownThreadsCallable(final SimulatedAnnealingService simulatedAnnealingService,
                                   final SAFAService safaService) {
        this.simulatedAnnealingService = simulatedAnnealingService;
        this.safaService = safaService;
    }

    /**
     * Executor.
     */
    @Override
    public Schedule call() throws Exception {

        LOG.debug("Checking whether to shutdown");
        simulatedAnnealingService.shuttingDownService();

        //Iteratively attempts to shutdown services while threads are still running.
        boolean completedSAFA = safaService.removeCompletedThreads();
        boolean completedSA = simulatedAnnealingService.removeCompletedThreads();
        while (!(completedSA & completedSAFA)) {
            LOG.debug("Threads still running, not shutting down");
            Thread.sleep(2000);
            completedSAFA = safaService.removeCompletedThreads();
            completedSA = simulatedAnnealingService.removeCompletedThreads();
        }

        safaService.shutDownExecutorService();

        LOG.debug("Shutdown services.");

        return safaService.getOptimal();
    }
}
