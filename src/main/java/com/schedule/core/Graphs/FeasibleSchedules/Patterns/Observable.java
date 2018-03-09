package com.schedule.core.Graphs.FeasibleSchedules.Patterns;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;

public interface Observable {

    void addObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObservers(Schedule oldOptimalSchedule);
}
