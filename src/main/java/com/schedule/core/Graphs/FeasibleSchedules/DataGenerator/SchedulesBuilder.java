package com.schedule.core.Graphs.FeasibleSchedules.DataGenerator;

import com.schedule.core.Graphs.FeasibleSchedules.Config.FileDataPaths;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Generates schedules from benchmark instances.
 */
public class SchedulesBuilder {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(com.schedule.core.Graphs.FeasibleSchedules
                                                                      .DataGenerator.SchedulesBuilder.class);

    /** {@link ScheduleService}. */
    private ScheduleService scheduleService = new ScheduleService();

    /**
     * Generate starting schedules.
     *
     * @param benchmarkInstance
     *         Benchmark instance.
     * @param setSize
     *         Number of schedules to generate.
     * @return Set of {@link Schedule}
     */
    public Set<Schedule> generateStartingSchedules(final String benchmarkInstance,
                                                   final Integer setSize) {

        final Set<Schedule> startingScheduleSet = new HashSet<>();

        Integer[][][] jobset = getBenchmarkInstance(benchmarkInstance);

        while (startingScheduleSet.size() != setSize) {

            startingScheduleSet.add(buildRandomSchedules(jobset));

        }

        return startingScheduleSet;
    }

    /**
     * Generates schedules for testing purposes (i.e. excludes randomness).
     *
     * @param benchmarkInstance
     *         Benchmark instance.
     * @param setSize
     *         Number of schedules to generate.
     * @return Set of {@link Schedule}
     */
    public Set<Schedule> generateTestSchedules(final String benchmarkInstance,
                                               final Integer setSize) {

        //Generates the same schedules using same random number set.
        Scanner input = null;
        try {
            File file = new File("./src/test/resources/RandomVals.txt");
            LOG.trace("File exists: {}", file.exists());
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        final String line = input.nextLine();
        LOG.trace("Line: {}", line);
        final String[] values = line.split(",");

        //Array where index 0 is count, rest are random values
        final Integer[] randomTestValues = new Integer[values.length];
        for (Integer i = 0; i < values.length; i++) {
            randomTestValues[i] = Integer.valueOf(values[i]);
        }

        final Set<Schedule> startingScheduleSet = new HashSet<>();

        Integer[][][] jobset = getBenchmarkInstance(benchmarkInstance);

        while (startingScheduleSet.size() != setSize) {

            startingScheduleSet.add(buildTestSchedules(jobset, randomTestValues));

            LOG.trace("Set size: {} Starting schedule size: {}", setSize, startingScheduleSet.size());
        }

        return startingScheduleSet;
    }

    /**
     * Builds random schedules using benchmark instance data.
     *
     * @param jobset
     *         Data of benchmark instance.
     * @return {@link Schedule}
     */
    private Schedule buildRandomSchedules(final Integer[][][] jobset) {

        final Integer numMachines = jobset[0].length;
        final Integer numJobs = jobset.length;

        final Schedule schedule = generateTreeTemplate(jobset, numMachines, numJobs);

        //Generate random staticSchedule
        final Random rand = new Random();

        Integer[] count = new Integer[numJobs];
        for (Integer i = 0; i < count.length; i++) {
            count[i] = 0;
        }

        Integer[] lastActiveJob = new Integer[numMachines];

        Integer sum = 0;
        while (sum < (numMachines * numJobs)) {
            final Integer randVal = rand.nextInt(numJobs);

            if (count[randVal] < numMachines) {

                final Integer machine = jobset[randVal][count[randVal]][0];
                schedule.setActiveEdge(randVal, lastActiveJob[machine], jobset[randVal][count[randVal]]);

                lastActiveJob[machine] = randVal;
                count[randVal]++;
                sum++;
            }
            LOG.trace("Sum: {}, total: {}", sum, numMachines * numJobs);
        }

        LOG.debug("Generated schedule, calculating schedule data...");

        scheduleService.calculateMakeSpan(schedule);

        return schedule;
    }

    /**
     * Builds test schedules using random values from a file.s
     *
     * @param jobset
     *         Data for schedule instance.
     * @param randomTestValues
     *         Static random values to generate the same test schedule each time.s
     * @return {@link Schedule}
     */
    private Schedule buildTestSchedules(final Integer[][][] jobset, final Integer[] randomTestValues) {

        final Integer numMachines = jobset[0].length;
        final Integer numJobs = jobset.length;

        final Schedule schedule = generateTreeTemplate(jobset, numMachines, numJobs);

        Integer[] count = new Integer[numJobs];
        for (Integer i = 0; i < count.length; i++) {
            count[i] = 0;
        }

        Integer[] lastActiveJob = new Integer[numMachines];

        Integer sum = 0;
        while (sum < (numMachines * numJobs)) {
            LOG.trace("Val: {}", randomTestValues[randomTestValues[0]]);
            final Integer jobVal = randomTestValues[randomTestValues[0]] % numJobs;

            LOG.trace("Job value: {}", jobVal);

            if (count[jobVal] < numMachines) {

                LOG.trace("Count of job: {}", count[jobVal]);

                final Integer machine = jobset[jobVal][count[jobVal]][0];
                schedule.setActiveEdge(jobVal, lastActiveJob[machine], jobset[jobVal][count[jobVal]]);

                lastActiveJob[machine] = jobVal;
                count[jobVal]++;
                sum++;
            }
            LOG.trace("Sum: {}, total: {}", sum, numMachines * numJobs);

            randomTestValues[0]++;
        }

        scheduleService.calculateMakeSpan(schedule);

        return schedule;
    }

    /**
     * Generates a schedule template using provided size parameters.
     *
     * @param jobset
     *         Schedule data.
     * @param numMachines
     *         Number of machines.
     * @param numJobs
     *         Number of jobs.
     * @return {@link Schedule}
     */
    private Schedule generateTreeTemplate(final Integer[][][] jobset, final Integer numMachines,
                                          final Integer numJobs) {

        final Schedule schedule = new Schedule(numMachines, numJobs);

        Integer jobValue = 0;
        Integer taskValue = 0;
        for (final Integer[][] jobs : jobset) {

            for (final Integer[] job : jobs) {

                LOG.trace("Adding operation: J:{}, M: {}, PT: {}", jobValue, job[0], job[1]);

                final Operation operation = new Operation(taskValue, jobValue, job[0]);

                schedule.addVertex(job[1], operation);
                taskValue++;
            }

            jobValue++;
        }

        schedule.getEndVertex().removeOverridenParentEdges();

        return schedule;
    }

    /**
     * Gets the benchmark instance data from a saved file.s
     *
     * @param instance
     *         String of instance name.
     * @return Data required to generate schedule.
     */
    public Integer[][][] getBenchmarkInstance(final String instance) {

        //Generates the same schedules using same random number set.
        Scanner input = null;
        try {
            File file = new File(FileDataPaths.BENCHMARK_INSTANCES_PATH + instance);
            LOG.trace("File exists: {}", file.exists());
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        final String line = input.nextLine();
        LOG.trace("Line: {}", line);
        final String[] size = line.split("\t");
        final Integer jobNum = Integer.valueOf(size[0]);
        final Integer machineNum = Integer.valueOf(size[1]);

        LOG.trace("Job num: {}, machine num: {}", jobNum, machineNum);

        final Integer[][][] benchmarkInstance = new Integer[jobNum][2][machineNum];

        int jobCount = 0;
        while (input.hasNext()) {

            final Integer[][] row = new Integer[machineNum][2];
            final String[] values = input.nextLine().split("\\t");

            Integer machine;
            Integer processingTime;
            Integer count = 0;
            for (int i = 0; i < values.length; i += 2) {

                machine = Integer.valueOf(values[i]);
                processingTime = Integer.valueOf(values[i + 1]);
                row[count] = new Integer[]{machine, processingTime};
                count++;
            }

            benchmarkInstance[jobCount] = row;
            jobCount++;
        }
        return benchmarkInstance;
    }
}