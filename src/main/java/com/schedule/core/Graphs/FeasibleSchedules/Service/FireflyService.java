package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.rits.cloning.Cloner;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Wrapper.SchedulePaths;
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
     * Comparing schedules by backbone
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
     * Checks the backbone similarity between two schedules.
     *
     * @param schedule
     *         {@link Schedule}
     * @param compareSchedule
     *         {@link Schedule}
     * @return Backbone score
     */
    private Integer checkBackBoneSimilarity(final Schedule schedule, final Schedule compareSchedule) {

        Integer compareScore = 0;

        for (final Set<Edge> path : schedule.getLongestPaths()) {

            for (final Set<Edge> comparePath : compareSchedule.getLongestPaths()) {

                final Set<Edge> pathCopy = new HashSet<>(path);

                LOG.trace("Path to string: {}", path.toString());
                LOG.trace("Path compare to string: {}", comparePath.toString());

                pathCopy.retainAll(comparePath);

                LOG.trace("Copy path: {}", pathCopy.toString());
                LOG.trace("Path copy size: {}", pathCopy.size());
                compareScore += pathCopy.size();
            }
        }

        return compareScore;

    }


    /**
     * Moves toward optimal by flipping local edges.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public boolean moveToOptimalNew(final Schedule schedule) {

        final Schedule optimal = optimalSchedule.getOptimalSchedule();

        //Attempts to move toward optimal using edges on local longest paths
        final Set<Edge> longestPathEdges = schedule.getLongestPathEdges();
        final Optional<Edge> edgeFlipped = findEdgeAndSwitchInSet(longestPathEdges);

        LOG.trace("Found edge on longest path: {}", edgeFlipped);

        if (!edgeFlipped.isPresent()) {

            boolean acceptedFlip = false;
            final Set<Edge> machineEdgesNotOnLongestPath = schedule.getAllMachineEdgesNotOnLongestPath();

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

        scheduleService.calculateScheduleData(schedule);

        return true;
    }

    /**
     * Using local edge, determines if optimal has equal edge, if not, switches edge if order needs changing, otherwise
     * continue looking for edge options.
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
