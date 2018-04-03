package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.LocalSearchCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Local Search Service layer.
 */
public class LocalSearchService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(LocalSearchService.class);

    /** {@link ScheduleService}. */
    final ScheduleService scheduleService;

    /** {@link FeasibilityService}. */
    final FeasibilityService feasibilityService;

    /** Local optimas for SA. */
    private Set<Schedule> localOptimalSchedules;

    /**
     * Constructor.
     */
    public LocalSearchService() {

        scheduleService = new ScheduleService();
        feasibilityService = new FeasibilityService();
        localOptimalSchedules = new HashSet<>();

    }

    /**
     * Local Search Algorithm Executor
     * Creates a new thread for each of the schedule instances in the population and executes local search
     * for a number of iterations = maxIterations parameter.
     *
     * @param scheduleSet
     *         Set of {@link Schedule}
     * @return Set of local optimal {@link Schedule}
     */
    public Set<Schedule> executeLocalSearch(final Set<Schedule> scheduleSet, final Integer maxIterations) {

        final ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Build threads
        final List<Callable<Schedule>> callables = new ArrayList<>();
        for (final Schedule schedule : scheduleSet) {

            final LocalSearchCallable localSearchCallable = new LocalSearchCallable(this, schedule, maxIterations);
            callables.add(localSearchCallable);
        }
        try {

            final List<Future<Schedule>> results = executorService.invokeAll(callables);

            for (final Future<Schedule> result : results) {
                result.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Terminate executor
        executorService.shutdown();

        return localOptimalSchedules;
    }

    /**
     * LS Algorithm
     * <p>
     * Modifies the underlying structure of the given instance using the transition function (flipping an edge) and
     * checks if the neighbouring instance has a preferred cost function. If so, proceed with the new instance.
     * Otherwise, find a new neighbour. once all neighbours have been exhausted, a local optimum has been found.
     *
     * @param schedule
     *         {@link Schedule}
     * @param maxIterations
     *         Maximum number of iterations before returning.
     * @return {@link Schedule}
     */
    public Schedule executeLocalSearchIteratively(final Schedule schedule, final Integer maxIterations) {

        for (int i = 0; i < maxIterations; i++) {

            ArrayList<Edge> longestPathEdges = schedule.getMachineEdgesOnLP();

            final Integer makespan = schedule.getMakespan();
            LOG.trace("Current makespan: {}", schedule.getMakespan());

            Optional<Edge> edgeFlip = scheduleService.flipMostVisitedEdgeLongestPath(schedule,
                                                                                     longestPathEdges, false);

            while (edgeFlip.isPresent()) {

                LOG.trace("Edge flipped: {}", edgeFlip);

                scheduleService.calculateScheduleData(schedule);

                LOG.trace("Local MP: {}, Optimal: {}", schedule.getMakespan(), makespan);
                if (!(schedule.getMakespan() < makespan)) {

                    LOG.trace("Moving away from local minima, undoing move");

                    longestPathEdges.removeAll(Collections.singleton(edgeFlip.get()));

                    //flip back if not improved schedule
                    scheduleService.switchEdge(edgeFlip.get());
                    scheduleService.calculateScheduleData(schedule);
                    edgeFlip = scheduleService.flipMostVisitedEdgeLongestPath(schedule, longestPathEdges, false);
                } else {

                    LOG.trace("Accepted move");
                    break;
                }
            }

            //Reached local minima.
            if(!edgeFlip.isPresent()){
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

    /**
     * Adds a local optimal schedule to the set of local optimas.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public void addLocalOptimalSchedule(final Schedule schedule) {
        localOptimalSchedules.add(schedule);
    }

    /**
     * Returns optimal schedule instance.
     *
     * @return {@link Schedule}
     */
    public Schedule getOptimalSchedule() {

        return localOptimalSchedules.stream().min(Comparator.comparing(Schedule::getMakespan))
                .orElse(null);
    }
}
