package com.schedule.core.Graphs.FeasibleSchedules.Patterns;

import com.rits.cloning.Cloner;
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
    public synchronized void setOptimalSchedule(final Schedule optimalSchedule) {

        LOG.debug("Optimal schedule update");

        Schedule oldOptimal = this.optimalSchedule;
        this.optimalSchedule = optimalSchedule;

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
}
