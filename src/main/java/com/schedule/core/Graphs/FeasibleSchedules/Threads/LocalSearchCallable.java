package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Service.LocalSearchService;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class LocalSearchCallable implements Callable<Schedule> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchCallable.class);

    private LocalSearchService localSearchService;
    private Schedule schedule;

    public LocalSearchCallable(final LocalSearchService localSearchService,
                                      final Schedule schedule) {
        this.localSearchService = localSearchService;
        this.schedule = schedule;
    }

    @Override
    public Schedule call() throws Exception {

        LOG.debug("Starting new LS thread");

        final Schedule localOptimal = localSearchService.executeLocalSearchIteratively(schedule, 100);
        LOG.debug("Finished LS thread.");

        localSearchService.addLocalOptimalSchedule(schedule);

        return localOptimal;
    }
}
