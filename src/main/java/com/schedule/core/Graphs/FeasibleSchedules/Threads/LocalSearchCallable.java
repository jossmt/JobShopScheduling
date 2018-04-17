package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Callable for LocalSearch method.
 */
public class LocalSearchCallable implements Callable<Schedule> {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchCallable.class);

    /** {@link LocalSearchService}. */
    private LocalSearchService localSearchService;

    /** Schedule to apply LS. */
    private Schedule schedule;

    /** Max number of LS iterations. */
    private Integer maxIterations;

    /**
     * LS Callable constructor.
     *
     * @param localSearchService
     *         {@link LocalSearchService}
     * @param schedule
     *         {@link Schedule}
     * @param maxIterations
     *         Max nuber of LS Iterations
     */
    public LocalSearchCallable(final LocalSearchService localSearchService,
                               final Schedule schedule, final Integer maxIterations) {
        this.localSearchService = localSearchService;
        this.schedule = schedule;
        this.maxIterations = maxIterations;
    }

    /**
     * Callable executor.
     */
    @Override
    public Schedule call() throws Exception {

        LOG.debug("Starting new LS thread");
        final Schedule localOptimal = localSearchService.executeLocalSearchIteratively(schedule, maxIterations);
        LOG.debug("Finished LS thread.");

        localSearchService.addLocalOptimalSchedule(schedule);
        return localOptimal;
    }
}
