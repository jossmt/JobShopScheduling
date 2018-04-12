package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Services;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.SAFACallable;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Observer;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.ShutDownThreadsCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Simulated Annealing-Firefly Algorithm service layer.
 */
public class SAFAHybridService {

    /** logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SAFAService.class);

    private FeasibilityService feasibilityService = new FeasibilityService();

    /** {@link ScheduleService}. */
    private ScheduleService scheduleService = new ScheduleService();

    /** {@link FireflyService}. */
    private FireflyService fireflyService;

    /** {@link SimulatedAnnealingService}. */
    private SimulatedAnnealingService simulatedAnnealingService;

    /** {@link Schedule}. */
    private OptimalSchedule optimalSchedule;

    /** {@link ExecutorService}. */
    private ExecutorService executorService;

    /** List of {@link Future} of type {@link Schedule}. */
    private List<Future<Schedule>> allRunningThreads;

    /** Start temperature. */
    private Double startTemp;

    /** Cooling rate. */
    private Double coolingRate;

    /**
     * Constructor.
     *
     * @param fireflyService
     *         {@link FireflyService}
     */
    public SAFAHybridService(final FireflyService fireflyService, final SimulatedAnnealingService
            simulatedAnnealingService,
                             final OptimalSchedule optimalSchedule, final Double startTemp, final Double coolingRate) {
        this.fireflyService = fireflyService;
        this.simulatedAnnealingService = simulatedAnnealingService;
        this.optimalSchedule = optimalSchedule;

        executorService = Executors.newFixedThreadPool(5);
        allRunningThreads = Collections.synchronizedList(new ArrayList<>());

        this.startTemp = startTemp;
        this.coolingRate = coolingRate;
    }

    /**
     * New SAFA approach, single SA executor and firefly population.
     *
     * @param scheduleSet
     *         Set of {@link Schedule} starting population
     */
    public void iterativeApproachSAFA(final Set<Schedule> scheduleSet) {

        scheduleService.calculateMakeSpan(optimalSchedule.getTargetSchedule());

        Double temp = 10000.0;
        coolingRate = 0.01;

        ArrayList<Edge> allMachineEdges = optimalSchedule.getTargetSchedule().getAllMachineEdgesManually();

        int count = 0;
        while (temp > 1) {

            LOG.debug("Iteration: {}, Makespan: {}", count, optimalSchedule.getTargetSchedule().getMakespan());

            // Makespan before flipping edge.
            final Integer prevMakespan = optimalSchedule.getTargetSchedule().getMakespan();

            // Flipping most visited edge on longest path
            final Optional<Edge> edgeOptional = scheduleService.findRandomEdge(allMachineEdges);

            if (!edgeOptional.isPresent()) {
                LOG.trace("Local minima reached");
                allMachineEdges = optimalSchedule.getTargetSchedule().getAllMachineEdgesManually();
                continue;
            }

            final Edge edge = edgeOptional.get();

            LOG.trace("Switching edge: {}", edge);
            scheduleService.switchEdge(edge);

            final Operation opFrom = edge.getOperationFrom();
            final Operation opTo = edge.getOperationTo();

            if (feasibilityService.scheduleIsFeasibleProof(opFrom, opTo)) {

                scheduleService.calculateMakeSpan(optimalSchedule.getTargetSchedule());

                // New makespan calculated
                final Integer currentMakespan = optimalSchedule.getTargetSchedule().getMakespan();

                // Calculates probability of accepting new schedule
                final Double acceptanceProb = acceptanceProbability(prevMakespan, currentMakespan, temp, startTemp);
                final Double random = scheduleService.randomDouble();

                LOG.trace("Acceptance prob: {}, Random generated: {}, temp: {}", acceptanceProb, random, temp);

                // Update external reference to optimal if makespan preferred
                if (currentMakespan < optimalSchedule.getOptimalSchedule().getMakespan()) {

                    LOG.debug("Setting new optimal: {} old: {}", currentMakespan, optimalSchedule.getOptimalSchedule
                            ().getMakespan());
                    optimalSchedule.setOptimalSchedule(optimalSchedule.getTargetSchedule(), Services
                            .SIMULATED_ANNEALING);
                }

                // If acceptance prob exceeds threshold, flip edge back
                if (acceptanceProb > random) {

                    LOG.trace("Accepted flip");

                } else {
                    LOG.trace("Not accepting edge flip");

                    // Switching same edge back
                    scheduleService.switchEdge(edgeOptional.get());
                    scheduleService.calculateMakeSpan(optimalSchedule.getTargetSchedule());
                }
            } else {
                LOG.trace("Infeasible edge flip");

                // Switching same edge back - continue so as not to count iteration in temp.
                scheduleService.switchEdge(edgeOptional.get());
                scheduleService.calculateMakeSpan(optimalSchedule.getTargetSchedule());
            }

            executeSingleSAFAIteration(scheduleSet);

            count++;
            temp *= 1 - coolingRate;
        }

        LOG.debug("Finished SAFA execution after {} iterations", count);
    }


    public void executeSingleSAFAIteration(final Set<Schedule> schedules) {

        LOG.debug("Executing single safa, schedule size: {}", schedules.size());
        final Iterator<Schedule> scheduleIterator = schedules.iterator();
        while (scheduleIterator.hasNext()) {

            final Schedule schedule = scheduleIterator.next();

            if (schedule.hashCode() == optimalSchedule.getTargetSchedule().hashCode()) {
                continue;
            }

            LOG.trace("\n_________________________\n");

            LOG.debug("Firefly move toward target");

            final boolean successMove = fireflyService.moveToOptimalNew(schedule);

            if (!successMove) {

                LOG.trace("No more move options, check if equal to optimal: {}",
                          schedule.hashCode() == optimalSchedule.getOptimalSchedule().hashCode());
                if (schedule.hashCode() != optimalSchedule.getOptimalSchedule().hashCode()) {

                    LOG.trace("Making random move");
                    scheduleService.findFeasibleEdgeAndFlip(schedule);

                } else {
                    continue;
                }
            }

            LOG.debug("Current FA Makespan: {}", schedule.getMakespan());

            if (schedule.getMakespan() < optimalSchedule.getOptimalSchedule().getMakespan()) {

                LOG.debug("Setting new optimal: {}, old: {}", schedule.getMakespan(), optimalSchedule
                        .getOptimalSchedule().getMakespan());
                optimalSchedule.setOptimalSchedule(schedule, Services.FIREFLY);
            }
        }
    }

    /**
     * Shuts down executor service when all threads complete.
     */
    public boolean removeCompletedThreads() {

        LOG.debug("Removing completed threads from cache, size: {}", allRunningThreads.size());

        scheduleService.removeCompletedThreads(allRunningThreads);

        LOG.debug("Finished removing completed threads from cache, size: {}", allRunningThreads.size());

        return allRunningThreads.isEmpty();
    }

    /**
     * Shuts down executor service.
     */
    public void shutDownExecutorService() {

        executorService.shutdown();
        simulatedAnnealingService.shutdownExecutorService();
    }

    /**
     * Restarts thread executor
     */
    public void restartThreadExecutor() {

        executorService = Executors.newFixedThreadPool(5);
        allRunningThreads = new ArrayList<>();
    }

    /**
     * Checks if executor services are terminated.
     *
     * @return true/false
     */
    public boolean executorsTerminated() {
        boolean safaExecutorTerminated = executorService.isTerminated();
        boolean saExecutorTerminated = simulatedAnnealingService.executorTerminated();

        return safaExecutorTerminated & saExecutorTerminated;
    }

    /**
     * Calculates acceptance criteria based on temperature.
     *
     * @param previous
     *         Previous schedule makespan
     * @param current
     *         Current schedule makespan
     * @param temp
     *         Temperature
     * @return Double representation of acceptance in range [0-1].
     */
    public Double acceptanceProbability(final Integer previous, final Integer current, final Double temp,
                                        final Double startTemp) {

        LOG.trace("Previous makespan: {}, current makespan: {}", previous, current);

        if (current < previous) {
            return 1.0;
        }
        return (temp / startTemp);
    }

    /**
     * Returns optimal schedule.
     *
     * @return {@link Schedule}
     */
    public Schedule getOptimal() {
        return optimalSchedule.getOptimalSchedule();
    }

}
