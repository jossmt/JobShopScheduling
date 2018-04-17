package com.schedule.core.Graphs.FeasibleSchedules.Patterns;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;

/**
 * Observable Interface.
 */
public interface Observable {

    /**
     * Adds new observer.
     *
     * @param observer
     *         {@link Observer}
     */
    void addObserver(Observer observer);

    /**
     * Removes observer.
     *
     * @param observer
     *         {@link Observer}
     */
    void removeObserver(Observer observer);

    /**
     * Notifies observers to apply update.
     *
     * @param oldOptimalSchedule
     *         {@link Schedule}
     */
    void notifyObservers(Schedule oldOptimalSchedule);
}
