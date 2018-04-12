package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.rits.cloning.Cloner;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Services;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.ShutDownThreadsSAOnlyCallable;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.SimulatedAnnealingCallable;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Simulated Annealing Service Layer.
 */
public class SimulatedAnnealingService implements Observer {

    /** logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SimulatedAnnealingService.class);

    /** {@link ScheduleService}. */
    private ScheduleService scheduleService = new ScheduleService();

    /** {@link ScheduleService}. */
    private FeasibilityService feasibilityService = new FeasibilityService();

    /** Global optimal schedule. */
    private OptimalSchedule optimalSchedule;

    /** {@link ExecutorService}. */
    private ExecutorService executorService;

    /** {@link Cloner}. */
    private Cloner cloner = new Cloner();

    /** {@link Future} of type {@link Schedule}. */
    private List<Future<Schedule>> runningThreads;

    /** Start temperature. */
    private Double startTemp;

    /** Cooling rate. */
    private Double coolingRate;

    /** true when Executorservice is shutting down. */
    private boolean shuttingDownService = false;

    /**
     * Constructor.
     */
    public SimulatedAnnealingService(final OptimalSchedule optimalSchedule, final Double startTemp,
                                     final Double coolingRate) {
        this.optimalSchedule = optimalSchedule;
        executorService = Executors.newFixedThreadPool(5);
        runningThreads = new CopyOnWriteArrayList<>(new ArrayList<>());

        this.startTemp = startTemp;
        this.coolingRate = coolingRate;
    }

    /**
     * Executes SA thread using schedule instance.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public void executeSimulatedAnnealing(final Schedule schedule) {

        LOG.trace("Executing SA with schedule hash: {}", schedule.hashCode());

        // Build threads
        final SimulatedAnnealingCallable simulatedAnnealingCallable =
                new SimulatedAnnealingCallable(this, schedule);
        final Future<Schedule> runningThread = executorService.submit(simulatedAnnealingCallable);
        runningThreads.add(runningThread);
    }

    /**
     * Simulated Annealing Algorithm
     * <p>
     * Utilises parameters starting temperature (default 3000) and a cooling rate (default 0.02) to dictate number
     * of iterations and the rate of temperature change. When temperature is high, our acceptance rate is high, leading
     * to the acceptance of a sporadic/random movement. As our temperature diminishes according to the cooling rate
     * and our acceptance rate diminishes, execution of the desired transition function increases, thereby effectively
     * covering the entire solution space.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public void iterateAndUpdateOptimal(final Schedule schedule) {

        scheduleService.calculateMakeSpan(schedule);

        Double temp = startTemp;

        ArrayList<Edge> allMachineEdges = schedule.getAllMachineEdgesManually();

        int count = 0;
        while (temp > 1) {

            LOG.trace("Iteration: {}, Makespan: {}", count, schedule.getMakespan());

            // Makespan before flipping edge.
            final Integer prevMakespan = schedule.getMakespan();

            // Flipping most visited edge on longest path
            final Optional<Edge> edgeOptional = scheduleService.findRandomEdge(allMachineEdges);

            if (!edgeOptional.isPresent()) {
                LOG.trace("Local minima reached");
                allMachineEdges = schedule.getAllMachineEdgesManually();
                continue;
            }

            final Edge edge = edgeOptional.get();

            LOG.trace("Switching edge: {}", edge);
            scheduleService.switchEdge(edge);

            final Operation opFrom = edge.getOperationFrom();
            final Operation opTo = edge.getOperationTo();

            if (feasibilityService.scheduleIsFeasibleProof(opFrom, opTo)) {

                scheduleService.calculateMakeSpan(schedule);

                // New makespan calculated
                final Integer currentMakespan = schedule.getMakespan();

                // Calculates probability of accepting new schedule
                final Double acceptanceProb = acceptanceProbability(prevMakespan, currentMakespan, temp, startTemp);
                final Double random = scheduleService.randomDouble();

                LOG.trace("Acceptance prob: {}, Random generated: {}, temp: {}", acceptanceProb, random, temp);

                // Update external reference to optimal if makespan preferred
                if (currentMakespan < optimalSchedule.getOptimalSchedule().getMakespan()) {

                    LOG.debug("Setting new optimal: {} old: {}", currentMakespan, optimalSchedule.getOptimalSchedule
                            ().getMakespan());
                    optimalSchedule.setOptimalSchedule(schedule, Services.SIMULATED_ANNEALING);
                    break;
                }

                // If acceptance prob exceeds threshold, flip edge back
                if (acceptanceProb > random) {

                    LOG.trace("Accepted flip");

                } else {
                    LOG.trace("Not accepting edge flip");

                    // Switching same edge back
                    scheduleService.switchEdge(edgeOptional.get());
                    scheduleService.calculateMakeSpan(schedule);
                }
            } else {
                LOG.trace("Infeasible edge flip");

                // Switching same edge back - continue so as not to count iteration in temp.
                scheduleService.switchEdge(edgeOptional.get());
                scheduleService.calculateMakeSpan(schedule);
                continue;
            }

            count++;
            temp *= 1 - coolingRate;
        }

        LOG.trace("Finished SA execution");
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

    /**
     * Shuts down executor service.
     */
    public void shutdownExecutorService() {
        executorService.shutdown();
    }

    /**
     * Shuts down executor service.
     */
    public void manualShutdownExecutorService() {

        LOG.debug("Manually shutting down executor service for SA");

        final ShutDownThreadsSAOnlyCallable shutDownThreadsSAOnlyCallable = new ShutDownThreadsSAOnlyCallable(this);
        executorService.submit(shutDownThreadsSAOnlyCallable);
    }

    /**
     * Shuts down executor service when all threads complete.
     */
    public boolean removeCompletedThreads() {

        LOG.debug("Removing completed threads from cache, size: {}", runningThreads.size());

        if (runningThreads.isEmpty()) {
            return true;
        }

        scheduleService.removeCompletedThreads(runningThreads);

        LOG.debug("Finished removing completed threads from cache, size: {}", runningThreads.size());

        return runningThreads.isEmpty();
    }

    /**
     * Restarts thread executor
     */
    public void restartThreadExecutor() {
        shuttingDownService = false;
        executorService = Executors.newFixedThreadPool(5);
        runningThreads = new ArrayList<>();
    }

    public boolean executorTerminated(){
        return executorService.isTerminated();
    }

    /**
     * Checks whether SA is executing or stagnant.s
     *
     * @return true/false
     */
    public boolean inactive() {
        return runningThreads.size() < 5 && !shuttingDownService;
    }

    /**
     * Tells new sa threads to stop running.
     */
    public void shuttingDownService() {
        shuttingDownService = true;
    }

    /**
     * Starts new SA thread with new optimal.
     *
     * @param schedule
     *         old optimal {@link Schedule}
     */
    @Override
    public void update(Schedule schedule) {

        final Schedule beaconCopy = cloner.deepClone(optimalSchedule.getOptimalSchedule());
        executeSimulatedAnnealing(beaconCopy);
    }
}
