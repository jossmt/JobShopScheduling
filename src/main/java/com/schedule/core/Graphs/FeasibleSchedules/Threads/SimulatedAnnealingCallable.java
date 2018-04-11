package com.schedule.core.Graphs.FeasibleSchedules.Threads;

import com.schedule.core.Graphs.FeasibleSchedules.Service.SimulatedAnnealingService;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class SimulatedAnnealingCallable implements Callable<Schedule> {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedAnnealingCallable.class);

    private SimulatedAnnealingService simulatedAnnealingService;
    private Schedule schedule;

    public SimulatedAnnealingCallable(final SimulatedAnnealingService simulatedAnnealingService,
                                      final Schedule schedule) {
        this.simulatedAnnealingService = simulatedAnnealingService;
        this.schedule = schedule;
    }

    @Override
    public Schedule call() throws Exception {

        LOG.debug("Starting new SA thread");

        simulatedAnnealingService.iterateAndUpdateOptimal(schedule);

        LOG.debug("Finished SA thread.");

        // Keeps SA running
        if(simulatedAnnealingService.inactive()){
            simulatedAnnealingService.update(null);
        }

        return simulatedAnnealingService.getOptimal();
    }
}
