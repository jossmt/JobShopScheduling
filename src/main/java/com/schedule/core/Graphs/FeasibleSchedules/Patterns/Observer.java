package com.schedule.core.Graphs.FeasibleSchedules.Patterns;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;

/**
 * Observer interface.
 */
public interface Observer {

    /** Notifies update and passes old optimal. */
    void update(final Schedule schedule);
}
