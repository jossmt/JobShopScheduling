package com.schedule.core.Graphs.FeasibleSchedules.Patterns;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Optimal schedule reference.
 */
public class OptimalSchedule implements Observable {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(OptimalSchedule.class);

    /** List of {@link Observer}. */
    private ArrayList<Observer> services = new ArrayList<>();

    /** Optimal {@link Schedule} instance. */
    private Schedule optimalSchedule;

    /** Rate of optimal schedule update for ls/sa/fa */
    private Integer lsUpdateCount = 0;
    private Integer saUpdateCount = 0;
    private Integer faUpdateCount = 0;

    /**
     * Gets optimalSchedule.
     *
     * @return Value of optimalSchedule.
     */
    public Schedule getOptimalSchedule() {
        return optimalSchedule;
    }

    /**
     * Sets optimal schedule.
     *
     * @param optimalSchedule
     *         Optimal Schedule.
     */
    public synchronized void setOptimalSchedule(final Schedule optimalSchedule, Services isSA) {

        Schedule oldOptimal = this.optimalSchedule;
        this.optimalSchedule = optimalSchedule;

        if (oldOptimal != null) {
            LOG.debug("Optimal schedule update: new: {}, before: {}", optimalSchedule.getMakespan(), oldOptimal
                    .getMakespan());
        }

        // Updates given counter depending on which service updated optimal.
        switch (isSA) {
            case FIREFLY:
                faUpdateCount++;
                break;
            case LOCAL_SEARCH:
                lsUpdateCount++;
                break;
            case SIMULATED_ANNEALING:
                saUpdateCount++;
                break;
        }

        notifyObservers(oldOptimal);
    }

    /**
     * Sets optimal schedule.
     *
     * @param optimalSchedule
     *         Optimal Schedule.
     */
    public synchronized void setOptimalScheduleWithoutNotifyingObservers(final Schedule optimalSchedule) {

        LOG.debug("Optimal schedule update");
        this.optimalSchedule = optimalSchedule;
    }

    /**
     * Gets Rate of optimal schedule update for safa.
     *
     * @return Value of Rate of optimal schedule update for safa.
     */
    public Integer getSaUpdateCount() {
        return saUpdateCount;
    }

    /**
     * Gets faUpdateCount.
     *
     * @return Value of faUpdateCount.
     */
    public Integer getFaUpdateCount() {
        return faUpdateCount;
    }

    /**
     * Gets Rate of optimal schedule update for lssafa.
     *
     * @return Value of Rate of optimal schedule update for lssafa.
     */
    public Integer getLsUpdateCount() {
        return lsUpdateCount;
    }

    /**
     * Adds observer.
     *
     * @param service
     *         Service class.
     */
    @Override
    public void addObserver(Observer service) {
        services.add(service);
    }

    /**
     * Removes observer.
     *
     * @param service
     *         Service class.
     */
    @Override
    public void removeObserver(Observer service) {
        services.remove(service);
    }

    /**
     * Notifies all services of an update of the optimal schedule.
     *
     * @param oldOptimalSchedule
     *         Previous optimal {@link Schedule}
     */
    @Override
    public void notifyObservers(final Schedule oldOptimalSchedule) {

        for (final Observer service : services) {
            service.update(oldOptimalSchedule);
        }
    }

    /**
     * String builder.
     *
     * @return Stringified object.
     */
    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(optimalSchedule.getMakespan());
        return stringBuilder.toString();
    }
}
