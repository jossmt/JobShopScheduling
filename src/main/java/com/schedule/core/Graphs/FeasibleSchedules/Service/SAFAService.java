package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Observer;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.Services;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.SAFACallable;
import com.schedule.core.Graphs.FeasibleSchedules.Threads.ShutDownThreadsCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Simulated Annealing-Firefly Algorithm service layer.
 */
public class SAFAService implements Observer {

    /** logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SAFAService.class);

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
    public SAFAService(final FireflyService fireflyService, final SimulatedAnnealingService simulatedAnnealingService,
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
     * Generates and executes SAFA threads using population of Schedule instances.
     *
     * @param scheduleSet
     *         Set of {@link Schedule}
     */
    public void executeSimulatedAnnealingFirefly(final Set<Schedule> scheduleSet) {

        // Build threads
        final List<Callable<Schedule>> callables = new ArrayList<>();

        scheduleSet.remove(optimalSchedule.getOptimalSchedule());

        // Runs SA/FA for all other schedules
        for (final Schedule schedule : scheduleSet) {

            final SAFACallable safaCallable = new SAFACallable(this, schedule);
            callables.add(safaCallable);
        }

        try {
            allRunningThreads.addAll(executorService.invokeAll(callables));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds new SAFA thread to executor service.
     *
     * @param schedule
     *         {@link Schedule}
     */
    private synchronized void addSimulatedAnnealingFireflyThread(final Schedule schedule) {

        final SAFACallable safaCallable = new SAFACallable(this, schedule);
        final Future<Schedule> future = executorService.submit(safaCallable);

        allRunningThreads.add(future);
    }

    /**
     * Simulated Annealing Firefly Algorithm Formula
     * Utilises parameters starting temperature (default 3000) and a cooling rate (default 0.02) to dictate number
     * of iterations and the rate of temperature change. When temperature is high, our acceptance rate is high, leading
     * to the acceptance of a sporadic/random movement. As our temperature diminishes according to the cooling rate
     * and our acceptance rate diminishes, execution of the desired transition function, in this case the firefly
     * movement towards the beacon, increases.
     * <p>
     * <p>
     * This approach depends on threading, whereby each firefly is represented by an individual thread that moves
     * of it's own accord.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public void iterateAndUpdateOptimalFirefly(final Schedule schedule) {

        scheduleService.calculateMakeSpan(schedule);

        Double temp = startTemp;

        int iterations = 0;
        while (temp > 1) {

            LOG.trace("SAFA iterations: {}", iterations);

            LOG.trace("\n_________________________\n");

            final Double acceptanceProb = fireflyService.acceptanceProbability(temp, startTemp);
            final Double randomProb = scheduleService.randomDouble();
            if (!(acceptanceProb > randomProb)) {
                LOG.trace("Firefly move toward optimal");

                final boolean successMove = fireflyService.moveToOptimalNew(schedule);
                if (!successMove) {
                    LOG.debug("No more move options, check if equal to optimal: {}",
                              schedule.hashCode() == optimalSchedule.getOptimalSchedule().hashCode());
                    break;
                }

            } else {

                LOG.trace("Making random move");
                scheduleService.findFeasibleEdgeAndFlip(schedule);
            }

            if (schedule.getMakespan() < optimalSchedule.getOptimalSchedule().getMakespan()) {

                LOG.trace("Setting new optimal");
                optimalSchedule.setOptimalSchedule(schedule, Services.FIREFLY);
                break;
            }

            iterations++;
            temp *= 1 - coolingRate;
        }

        LOG.debug("Finished SAFA execution after {} iterations", iterations);
    }

    /**
     * Simulated Annealing Firefly Algorithm Formula
     * Utilises parameters starting temperature  and a cooling rate to dictate number
     * of iterations and the rate of temperature change. When temperature is high, our acceptance rate is high, leading
     * to the acceptance of a sporadic/random movement. As our temperature diminishes according to the cooling rate
     * and our acceptance rate diminishes, execution of the desired transition function, in this case the firefly
     * movement towards the beacon, increases.
     * <p>
     * A threadless implementation of the SAFA algorithm, executing a single iteration for each firefly in the
     * population per iteration.
     *
     * @param scheduleSet
     *         Set of {@link Schedule}
     */
    public void iterativeApproachSAFA(final Set<Schedule> scheduleSet) {

        // Must use arraylist for object reference as hashcode reference is immutable
        // And we're constantly changing hash value therefore cant iterator.remove()
        final ArrayList<Schedule> schedules = new ArrayList<>(scheduleSet);

        schedules.remove(optimalSchedule.getOptimalSchedule());

        Double temp = startTemp;

        int iteration = 0;
        while (temp > 1) {

            if (schedules.isEmpty()) {
                break;
            }

            LOG.debug("SAFA Iteration: {}, SchedulesSize: {}", iteration, schedules.size());
            int randomCount = 0;
            int fireflyMoveCount = 0;

            final Iterator<Schedule> scheduleIterator = schedules.iterator();
            while (scheduleIterator.hasNext()) {

                final Schedule schedule = scheduleIterator.next();

                if (schedule.hashCode() == optimalSchedule.getOptimalSchedule().hashCode()) {
                    continue;
                }

                LOG.trace("\n_________________________\n");

                final Double acceptanceProb = fireflyService.acceptanceProbability(temp, startTemp);
                final Double randomProb = scheduleService.randomDouble();
                if (acceptanceProb > randomProb) {

                    LOG.trace("Making random move");
                    scheduleService.findFeasibleEdgeAndFlip(schedule);

                    randomCount++;

                } else {

                    LOG.trace("Firefly move toward optimal");

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
                    fireflyMoveCount++;
                }
                if (schedule.getMakespan() < optimalSchedule.getOptimalSchedule().getMakespan()) {

                    LOG.trace("Setting new optimal: {}, old: {}", schedule.getMakespan(), optimalSchedule
                            .getOptimalSchedule().getMakespan());
                    optimalSchedule.setOptimalSchedule(schedule, Services.FIREFLY);
                    scheduleIterator.remove();
                }
            }

            LOG.debug("Ratios => Random move:FireflyMove => {}:{}", randomCount, fireflyMoveCount);

            //All fireflies equal beacon
            if (randomCount + fireflyMoveCount == 0) {
                break;
            }

            iteration++;
            temp *= 1 - coolingRate;
        }

        LOG.debug("Finished SAFA execution after {} iterations", iteration);
        beginShuttingDownThreads();
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
     * Begins shutting down executor threads.
     */
    public void beginShuttingDownThreads() {

        ShutDownThreadsCallable shutDownThreadsCallable = new ShutDownThreadsCallable(simulatedAnnealingService, this);
        executorService.submit(shutDownThreadsCallable);
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
     * Returns optimal schedule.
     *
     * @return {@link Schedule}
     */
    public Schedule getOptimal() {
        return optimalSchedule.getOptimalSchedule();
    }

    /**
     * Starts new SAFA thread for old optimal
     */
    @Override
    public void update(final Schedule schedule) {

        LOG.debug("Updated max, starting new SAFA thread");

        if (schedule != null) {

            addSimulatedAnnealingFireflyThread(schedule);
        }
    }
}
