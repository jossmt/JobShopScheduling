package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
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
public class SAFAService implements Observer {

    /** logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SAFAService.class);

    /** {@link ScheduleService}. */
    private ScheduleService scheduleService = new ScheduleService();

    /** {@link FeasibilityService}. */
    private FeasibilityService feasibilityService = new FeasibilityService();

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

    /**
     * Constructor.
     *
     * @param fireflyService
     *         {@link FireflyService}
     */
    public SAFAService(final FireflyService fireflyService, final SimulatedAnnealingService simulatedAnnealingService,
                       final OptimalSchedule optimalSchedule) {
        this.fireflyService = fireflyService;
        this.simulatedAnnealingService = simulatedAnnealingService;
        this.optimalSchedule = optimalSchedule;

        executorService = Executors.newFixedThreadPool(5);
        allRunningThreads = new ArrayList<>();
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

        //Adds shutdown thread to handle ending threads
        final ShutDownThreadsCallable shutDownThreadsCallable = new ShutDownThreadsCallable(this);
        executorService.submit(shutDownThreadsCallable);
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
        schedule.initialiseCache();

        // Starting temp
        Double startTemp = 3000.0;
        Double temp = startTemp;
        // Cooling rate
        final Double coolingRate = 0.02;

        int iterations = 0;
        while (temp > 1) {

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
                final ArrayList<Edge> allMachineEdges = new ArrayList<>(schedule.getAllMachineEdgesManually());

                final Optional<Edge> edge = scheduleService.findFeasibleEdgeToFlip(allMachineEdges);

                if (edge.isPresent()) {
                    scheduleService.switchEdge(edge.get());
                    scheduleService.calculateMakeSpan(schedule);
                }
            }

            if (schedule.getMakespan() < this.optimalSchedule.getOptimalSchedule().getMakespan()) {

                LOG.trace("Setting new optimal");
                optimalSchedule.setOptimalSchedule(schedule);
                break;
            }

            iterations++;
            temp *= 1 - coolingRate;
        }

        LOG.debug("Finished SAFA execution after {} iterations", iterations);
    }

    /**
     * Simulated Annealing Firefly Algorithm Formula
     * Utilises parameters starting temperature (default 3000) and a cooling rate (default 0.02) to dictate number
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

        // Starting temp
        Double startTemp = 1000.0;
        Double temp = startTemp;
        // Cooling rate
        final Double coolingRate = 0.03;

        int iteration = 0;
        while (temp > 1) {

            if (schedules.isEmpty()) {
                break;
            }

            LOG.debug("Iteration: {}", iteration);
            final Iterator<Schedule> scheduleIterator = schedules.iterator();
            while (scheduleIterator.hasNext()) {

                final Schedule schedule = scheduleIterator.next();

                schedule.initialiseCache();

                LOG.trace("\n_________________________\n");

                final Double acceptanceProb = fireflyService.acceptanceProbability(temp, startTemp);
                final Double randomProb = scheduleService.randomDouble();
                if (!(acceptanceProb > randomProb)) {
                    LOG.trace("Firefly move toward optimal");

                    final boolean successMove = fireflyService.moveToOptimalNew(schedule);

                    if (!successMove) {

                        LOG.debug("No more move options, check if equal to optimal: {}",
                                  schedule.hashCode() == optimalSchedule.getOptimalSchedule().hashCode());

                        scheduleIterator.remove();
                    }

                } else {

                    LOG.trace("Making random move");
                    final ArrayList<Edge> allMachineEdges = new ArrayList<>(schedule.getAllMachineEdgesManually());

                    final Optional<Edge> edge = scheduleService.findFeasibleEdgeToFlip(allMachineEdges);

                    if (edge.isPresent()) {
                        scheduleService.switchEdge(edge.get());
                        scheduleService.calculateMakeSpan(schedule);
                    }
                }

                if (schedule.getMakespan() < this.optimalSchedule.getOptimalSchedule().getMakespan()) {

                    LOG.trace("Setting new optimal");

                    optimalSchedule.setOptimalSchedule(schedule);
                }
            }

            iteration++;
            temp *= 1 - coolingRate;
        }

        shutDownExecutors();
    }

    /**
     * Shuts down executor service when all threads complete.
     */
    public boolean removeCompletedThreads() {

        LOG.trace("Removing completed threads from cache, size: {}", allRunningThreads.size());

        if (!allRunningThreads.isEmpty()) {

            Iterator<Future<Schedule>> threadIterator = allRunningThreads.iterator();
            while (threadIterator.hasNext()) {

                final Future<Schedule> currentThread = threadIterator.next();

                try {
                    currentThread.get();

                    if (currentThread.isDone()) {
                        threadIterator.remove();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        return allRunningThreads.isEmpty();
    }

    /**
     * Shuts down executors.
     */
    public void shutDownExecutors() {

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
