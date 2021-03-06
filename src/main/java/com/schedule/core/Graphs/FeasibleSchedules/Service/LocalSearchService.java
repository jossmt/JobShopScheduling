package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.LocalSearchCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class LocalSearchService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchService.class);

    /** {@link ScheduleService}. */
    final ScheduleService scheduleService;

    /** Local optimas for SA. */
    private Set<Schedule> localOptimalSchedules;

    public LocalSearchService() {

        scheduleService = new ScheduleService();
        localOptimalSchedules = new HashSet<>();

    }

    /**
     * Executes SA using random population of schedules.
     *
     * @param scheduleSet
     *         Set of {@link Schedule}
     * @return Set of local optimal {@link Schedule}
     */
    public Set<Schedule> executeLocalSearch(final Set<Schedule> scheduleSet) {

        final ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Build threads
        final List<Callable<Schedule>> callables = new ArrayList<>();
        for (final Schedule schedule : scheduleSet) {

            final LocalSearchCallable localSearchCallable = new LocalSearchCallable(this, schedule);
            callables.add(localSearchCallable);
        }
        try {

            final List<Future<Schedule>> results = executorService.invokeAll(callables);

            for (final Future<Schedule> result : results) {
                result.get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Terminate executor
        executorService.shutdown();

        return localOptimalSchedules;
    }

    public Schedule executeLocalSearchIteratively(final Schedule schedule, final Integer maxIterations) {

        ArrayList<Edge> longestPathEdges = schedule.getLongestPathArray();

        for (int i = 0; i < maxIterations; i++) {

            final Integer makespan = schedule.getMakespan();
            LOG.trace("Current makespan: {}", schedule.getMakespan());

            final Optional<Edge> edgeFlip = scheduleService.flipMostVisitedEdgeLongestPath(schedule,
                                                                                           longestPathEdges, false);

            if (edgeFlip.isPresent()) {

                LOG.trace("Edge flipped: {}", edgeFlip);

                scheduleService.calculateScheduleData(schedule);
                scheduleService.calculateMakeSpan(schedule);

                if (!(schedule.getMakespan() < makespan)) {

                    LOG.trace("Moving away from local minima, undoing move");

                    longestPathEdges.removeAll(Collections.singleton(edgeFlip.get()));

                    //flip back if not improved schedule
                    scheduleService.switchEdge(edgeFlip.get());
                    scheduleService.calculateScheduleData(schedule);
                } else {

                    LOG.trace("Accepted move");

                    longestPathEdges = schedule.getLongestPathArray();
                }
            } else {
                break;
            }
        }

        return schedule;
    }

    /**
     * Gets Local optimas for SA..
     *
     * @return Value of Local optimas for SA..
     */
    public Set<Schedule> getLocalOptimalSchedules() {
        return localOptimalSchedules;
    }

    public void addLocalOptimalSchedule(final Schedule schedule) {
        localOptimalSchedules.add(schedule);
    }

    public Schedule getOptimalSchedule() {

        return localOptimalSchedules.stream().min(Comparator.comparing(Schedule::getMakespan))
                .orElse(null);
    }
}
