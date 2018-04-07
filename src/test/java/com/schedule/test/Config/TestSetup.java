package com.schedule.test.Config;

import com.schedule.core.Graphs.FeasibleSchedules.DataGenerator.SchedulesBuilder;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Default test setup.
 */
public class TestSetup {

    /** {@link SchedulesBuilder}. */
    protected SchedulesBuilder schedulesBuilder = new SchedulesBuilder();

    /** {@link ScheduleService}. */
    protected ScheduleService scheduleService = new ScheduleService();

    /** {@link ScheduleService}. */
    protected FeasibilityService feasibilityService = new FeasibilityService();

    /** {@link OptimalSchedule}. */
    protected OptimalSchedule optimalSchedule = new OptimalSchedule();

    /** Optimal {@link Schedule}. */
    protected Schedule optimal;

    /** Set of {@link Schedule} to test. */
    protected Set<Schedule> testSchedules;

    /**
     * Setup.
     *
     * @param benchmarkInstance
     *         Benchmark Instance
     * @param setSize
     *         Set size of test schedules.
     */
    public void setUp(final String benchmarkInstance, final Integer setSize) {

        testSchedules = new HashSet<>();

        final Set<Schedule> schedules = schedulesBuilder.generateStartingSchedules(benchmarkInstance, setSize);

        boolean first = true;
        for (final Schedule schedule : schedules) {

            schedule.getAllMachineEdgesManually();
            if (first) {
                optimal = schedule;
                first = false;
            }

            if (schedule.getMakespan() < optimal.getMakespan()) {

                testSchedules.add(optimal);
                optimal = schedule;

            } else {
                testSchedules.add(schedule);
            }
        }
    }

    /**
     * Reads file to string.
     *
     * @param path
     *         Path of file.
     * @return Stringified file contents.
     */
    protected String readFile(String path) {

        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded);
    }
}
