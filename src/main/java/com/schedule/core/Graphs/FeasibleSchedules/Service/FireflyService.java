package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import com.schedule.core.Graphs.FeasibleSchedules.Patterns.OptimalSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

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

        boolean acceptedFlip = false;
        final ArrayList<Edge> allMachineEdges = schedule.getAllMachineEdgesManually();

        Optional<Edge> edgeFlip = findEdgeToSwitchInList(allMachineEdges);

        while (!acceptedFlip) {
            if (edgeFlip.isPresent()) {

                final Edge edge = edgeFlip.get();
                allMachineEdges.remove(edge);
                scheduleService.switchEdge(edge);

                if (feasibilityService.scheduleIsFeasibleProof(edge.getOperationFrom(), edge.getOperationTo())) {

                    LOG.trace("Edge flip created feasible schedule");

                    scheduleService.calculateMakeSpan(schedule);

                    acceptedFlip = true;
                } else {

                    LOG.trace("Edge flip created infeasible schedule, remaining options size: {}",
                              allMachineEdges.size());

                    scheduleService.switchEdge(edgeFlip.get());
                    edgeFlip = findEdgeToSwitchInList(allMachineEdges);
                }

            } else {
                if (schedule.hashCode() != optimal.hashCode()) {

                    LOG.debug("Can't get any closer to optimal using firefly");
                } else {

                    LOG.debug("Reached optimal using firefly");
                }
                return false;
            }
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
    private Optional<Edge> findEdgeToSwitchInList(final ArrayList<Edge> edges) {

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
