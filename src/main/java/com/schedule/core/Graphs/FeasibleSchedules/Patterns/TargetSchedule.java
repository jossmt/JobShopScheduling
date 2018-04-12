package com.schedule.core.Graphs.FeasibleSchedules.Patterns;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;

/**
 * Target schedule.
 */
public class TargetSchedule {

    /** Optimal {@link Schedule} instance. */
    private Schedule targetSchedule;


    /**
     * Sets new Optimal {@link Schedule} instance..
     *
     * @param targetSchedule
     *         New value of Optimal {@link Schedule} instance..
     */
    public void setTargetSchedule(Schedule targetSchedule) {
        this.targetSchedule = targetSchedule;
    }

    /**
     * Gets Optimal {@link Schedule} instance..
     *
     * @return Value of Optimal {@link Schedule} instance..
     */
    public Schedule getTargetSchedule() {
        return targetSchedule;
    }
}
