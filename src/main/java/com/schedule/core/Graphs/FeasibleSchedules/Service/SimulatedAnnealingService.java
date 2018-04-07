package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.rits.cloning.Cloner;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
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
    private Future<Schedule> runningThread;

    /**
     * Constructor.
     */
    public SimulatedAnnealingService(final OptimalSchedule optimalSchedule) {
        this.optimalSchedule = optimalSchedule;
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Executes SA thread using schedule instance.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public void executeSimulatedAnnealing(final Schedule schedule) {

        LOG.trace("Executing SA with schedule hash: {}", schedule.hashCode());

        if (runningThread != null) {

            runningThread.cancel(true);
        }

        // Build threads
        final SimulatedAnnealingCallable simulatedAnnealingCallable =
                new SimulatedAnnealingCallable(this, schedule);
        runningThread = executorService.submit(simulatedAnnealingCallable);
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
        schedule.initialiseCache();

        // Starting temp
        Double startTemp = 1000.0;
        Double temp = startTemp;
        // Cooling rate
        final Double coolingRate = 0.03;

        ArrayList<Edge> allMachineEdges = new ArrayList<>(schedule.getAllMachineEdgesManually());

        int count = 0;
        while (temp > 1) {

            LOG.debug("Iteration: {}, Makespan: {}", count, schedule.getMakespan());

            // Makespan before flipping edge.
            final Integer prevMakespan = schedule.getMakespan();

            // Flipping most visited edge on longest path
            final Optional<Edge> edgeOptional = scheduleService.findRandomEdge(allMachineEdges);

            if (!edgeOptional.isPresent()) {
                LOG.trace("Local minima reached");
                schedule.clearCache();
                allMachineEdges = new ArrayList<>(schedule.getAllMachineEdgesManually());
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

                    optimalSchedule.setOptimalSchedule(schedule);
                }

                // If acceptance prob exceeds threshold, flip edge back
                if (!(acceptanceProb > random)) {

                    LOG.debug("Not accepting edge flip");

                    // Switching same edge back
                    scheduleService.switchEdge(edgeOptional.get());
                    scheduleService.calculateMakeSpan(schedule);

                } else {
                    LOG.debug("Accepted flip");
                }
            } else {
                LOG.debug("Infeasible edge flip");

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

        if (runningThread.isDone()) {
            executorService.shutdown();
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            manualShutdownExecutorService();
        }
    }

    /**
     * Restarts thread executor
     */
    public void restartThreadExecutor() {

        executorService = Executors.newSingleThreadExecutor();
        runningThread = null;
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
