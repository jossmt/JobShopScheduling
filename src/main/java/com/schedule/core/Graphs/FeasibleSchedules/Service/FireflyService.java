package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.rits.cloning.Cloner;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service handling methods surrounding movement of fireflies in FireflyAlgorithm.
 */
public class FireflyService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FireflyService.class);

    /** {@link ScheduleService}. */
    private ScheduleService scheduleService = new ScheduleService();

    /** {@link FeasibilityService}. */
    private FeasibilityService feasibilityService = new FeasibilityService();

    /** {@link Cloner}. */
    private Cloner cloner = new Cloner();

    /** Schedule state with optimal backbone. */
    private OptimalSchedule optimalSchedule;

    /**
     * Constructor.
     *
     * @param optimalSchedule
     *         {@link OptimalSchedule}
     */
    public FireflyService(final OptimalSchedule optimalSchedule) {
        this.optimalSchedule = optimalSchedule;
    }

    /**
     * Sets the beacon sBest depending on whether its based on backbone score or makespan.
     *
     * @param schedules
     *         Set of {@link Schedule}
     * @param isBackBone
     *         true/false
     * @param optimalSchedule
     *         {@link Schedule}
     */
    public void computeOptimal(final Set<Schedule> schedules, final boolean isBackBone, final Schedule
            optimalSchedule) {

        if (isBackBone) {
            computeOptimalBackBone(schedules);
        } else {

            this.optimalSchedule.setOptimalSchedule(optimalSchedule);
        }
    }

    /**
     * Comparing schedules by backbone (number of similar orientations on longest paths).
     *
     * @param schedules
     *         List of highest makespan schedules
     */
    private void computeOptimalBackBone(final Set<Schedule> schedules) {

        final Set<Schedule> schedulesCopy = new HashSet<>();

        Iterator<Schedule> iterator = schedules.iterator();

        while (iterator.hasNext()) {

            final Schedule schedule = iterator.next();
            iterator.remove();

            while (iterator.hasNext()) {

                final Schedule nestedSchedule = iterator.next();

                final Integer backBoneScore = checkBackBoneSimilarity(schedule, nestedSchedule);

                LOG.trace("Updating backbone score with: {}", backBoneScore);
                schedule.updateBackBoneScore(backBoneScore);
                nestedSchedule.updateBackBoneScore(backBoneScore);

            }

            schedulesCopy.add(schedule);
        }

        final Schedule optimalSchedule = Collections.max(schedulesCopy, Comparator.comparing
                (Schedule::getBackBoneScore));

        LOG.trace("Optimal Schedule found with score: {}", optimalSchedule.getBackBoneScore());
        LOG.trace("and makespan: {}", optimalSchedule.getMakespan());

        this.optimalSchedule.setOptimalSchedule(cloner.deepClone(optimalSchedule));

    }

    /**
     * Checks the backbone (number of equal edge orientations on longest path0 similarity between two schedules.
     *
     * @param schedule
     *         {@link Schedule}
     * @param compareSchedule
     *         {@link Schedule}
     * @return Backbone score
     */
    private Integer checkBackBoneSimilarity(final Schedule schedule, final Schedule compareSchedule) {

        Integer compareScore = 0;

        final Set<Edge> machineEdgesOne = schedule.getAllMachineEdges();
        final Set<Edge> machineEdgesTwo = compareSchedule.getAllMachineEdges();

        machineEdgesOne.retainAll(machineEdgesTwo);
        compareScore += machineEdgesOne.size();

        return compareScore;

    }

    /**
     * Moves toward optimal beacon by modifying the underlying structure of the schedule instance
     * using a transition function. The transition function (firefly movement), involves checking the edge orientation
     * of two operations on the optimal schedule instance and mimicking the orientation for the local schedule instance
     * by flipping edges on the longest paths. If no move can be made on the longest path, an effort is made to
     * switch edges not on the longest path.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public boolean moveToOptimalNew(final Schedule schedule) {

        final Schedule optimal = optimalSchedule.getOptimalSchedule();

        //Attempts to move toward optimal using edges on local longest paths
        final Set<Edge> longestPathEdges = schedule.getMachineEdgesOnLPSet();
        final Optional<Edge> edgeFlipped = findEdgeAndSwitchInSet(longestPathEdges);

        LOG.trace("Found edge on longest path: {}", edgeFlipped);

        if (!edgeFlipped.isPresent()) {

            boolean acceptedFlip = false;
            final Set<Edge> machineEdgesNotOnLongestPath = schedule.getMachineEdgesNotOnLP();

            Optional<Edge> edgeFlip = findEdgeAndSwitchInSet(machineEdgesNotOnLongestPath);

            LOG.trace("Found edge not on longest path to flip: {}", edgeFlip);

            while (!acceptedFlip) {
                if (edgeFlip.isPresent()) {

                    final Edge edge = edgeFlip.get();
                    if (feasibilityService.scheduleIsFeasibleProof(edge.getOperationFrom(), edge.getOperationTo())) {

                        LOG.trace("Edge flip created feasible schedule");

                        scheduleService.calculateScheduleData(schedule);

                        acceptedFlip = true;
                    } else {

                        LOG.trace("Edge flip created infeasible schedule");

                        scheduleService.switchEdge(edgeFlip.get());
                        machineEdgesNotOnLongestPath.remove(edge);
                        edgeFlip = findEdgeAndSwitchInSet(machineEdgesNotOnLongestPath);
                    }

                } else {
                    if (schedule.hashCode() != optimal.hashCode()) {

                        LOG.trace("Can't get any closer to optimal using firefly");
                    } else {

                        LOG.trace("Reached optimal using firefly");
                    }
                    return false;
                }
            }

        } else {

            scheduleService.calculateScheduleData(schedule);
        }

        return true;
    }

    /**
     * Locates an edge on the longest path. If the operations on said edge are not in order on the local schedule
     * instance, the edge is flipped. If it is, a new edge option is found.
     *
     * @param edges
     *         Set of {@link Edge}
     * @return Flipped edge or null
     */
    private Optional<Edge> findEdgeAndSwitchInSet(final Set<Edge> edges) {

        final Schedule optimal = optimalSchedule.getOptimalSchedule();
        final Iterator<Edge> edgeIterator = edges.iterator();

        Edge edgeFlipped = null;
        while (edgeIterator.hasNext()) {

            final Edge currentEdge = edgeIterator.next();

            // Checks if the edge is flippable (i.e. on the machine path).
            if (currentEdge.isMachinePath()) {

                final Operation opFrom = optimal.locateOperation(currentEdge.getOperationFrom().getJob(), currentEdge
                        .getOperationFrom().getMachine());

                if (opFrom.hasConjunctiveEdge()) {
                    if (opFrom.getConjunctiveEdge().equals(currentEdge)) {
                        edgeIterator.remove();
                    }
                }

                //If order of operations is different in optimal, reorder local
                final Operation opTo = optimal.locateOperation(currentEdge.getOperationTo().getJob(),
                                                               currentEdge.getOperationTo().getMachine());
                if (!scheduleService.isInOrder(opFrom, opTo)) {

                    edgeFlipped = currentEdge;
                    scheduleService.switchEdge(currentEdge);
                    break;
                }
            } else {
                edgeIterator.remove();
            }
        }

        return Optional.ofNullable(edgeFlipped);
    }

    /**
     * Basic acceptance probability based on temperature.
     *
     * @param temp
     *         Current Temp
     * @param startTemp
     *         Starting temp.
     * @return Ratio.
     */
    public Double acceptanceProbability(final Double temp, final Double startTemp) {

        return (temp / startTemp);
    }
}
