package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.EndVertex;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Methods handling schedule feasibility checking, fixing, detecting etc.
 */
public class FeasibilityService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FeasibilityService.class);

    /**
     * Default constructor.
     */
    public FeasibilityService() {
    }

    /**
     * Schedule feasibility check following an edge switch.
     * Using operation edge flipped from, and operation edge flipped to, method asserts if the new
     * schedule instance is feasible. With the help of a proof, the method checks that all paths leading from
     * operationTO do not lead back to any job preceding the new operationFrom.
     *
     * @param from
     *         after being flipped
     *         {@link Operation}
     * @param to
     *         after being flipped
     *         {@link Operation}
     * @return true/false
     */
    public boolean scheduleIsFeasibleProof(final Operation from, final Operation to) {

        LOG.trace("Checking feasibility for edge flip task from: {}, task to: {}", from, to);

        // Check if new operation to has no path ahead
        Operation postJobPathTo = null;
        if (to.hasConjunctiveEdge()) {
            postJobPathTo = to.getConjunctiveEdge().getOperationTo();
        }

        LOG.trace("A");

        // If not, no risk of cycle
        if (postJobPathTo instanceof EndVertex) {
            return true;
        }

        // Gets all operations preceding new from on job path
        final Set<Operation> preJobPathsFrom = getOperationsOnJobPath(from, false);

        LOG.trace("-J(from): {}", preJobPathsFrom);
        LOG.trace("J(to): {}", postJobPathTo);

        // Gets all possible forward routes ahead of new operation to
        final Set<Operation> forwardMachinePathOps = breadthFirstSearch(postJobPathTo);
        LOG.trace("Forward mach path ops: {}", forwardMachinePathOps);
        forwardMachinePathOps.retainAll(preJobPathsFrom);

        // If there is any overlap, return false, otherwise true
        return forwardMachinePathOps.size() == 0;
    }

    /**
     * Generates operations on job path from given point either forward or backward.
     *
     * @param op
     *         Point to start list
     * @param isForward
     *         true/false
     * @return Set of operations on job path.
     */
    private Set<Operation> getOperationsOnJobPath(final Operation op, final boolean isForward) {

        LOG.trace("Finding ops on job path from: {} direction: {}", op, isForward);

        final Set<Operation> operations = new HashSet<>();
        if (isForward) {
            Operation current = op;
            while (current.hasConjunctiveEdge()) {
                current = current.getConjunctiveEdge().getOperationTo();
                operations.add(current);
            }
        } else {

            Operation current = op;
            LOG.trace("Current: {}", current);
            LOG.trace("Parent: {}", current.getConjunctiveParent());
            while (current.hasConjunctiveParent()) {
                operations.add(current);
                current = current.getConjunctiveParent().getOperationFrom();
            }
            operations.add(current);
        }

        return operations;
    }

    /**
     * Breadth First Search graph from root.
     *
     * @param root
     *         Start Operation.
     * @return Set of {@link Operation}
     */
    private Set<Operation> breadthFirstSearch(final Operation root) {

        final Set<Operation> bfsOps = new HashSet<>();
        //Since queue is a interface
        Queue<Operation> queue = new LinkedList<>();

        bfsOps.add(root);
        //Adds to end of queue
        queue.add(root);

        while (!queue.isEmpty()) {

            Operation op = queue.remove();

            //Visit child first before grandchild
            for (Edge e : op.getActiveEdges()) {
                if (!(e.getOperationTo() instanceof EndVertex)) {

                    final Operation opTo = e.getOperationTo();
                    if (!bfsOps.contains(opTo)) {
                        queue.add(opTo);
                        bfsOps.add(opTo);
                    }
                }
            }
        }

        return bfsOps;
    }

    /**
     * Detects if schedule contains a cycle (uses BFS).
     *
     * @param schedule
     *         {@link Schedule}
     * @return true if cycle
     */
    public boolean hasCycle(final Schedule schedule) {

        LOG.trace("Checking cycle");

        List<Operation> visited;
        boolean response = false;
        for (int i = 0; i < schedule.getNumJobs(); i++) {

            visited = new ArrayList<>();
            final Operation node = schedule.getJobHashMap().get(i);

            if (cycleDetect(node, visited)) {
                response = true;
            }
        }

        LOG.trace("Finished Checking cycle");

        return response;
    }

    /**
     * Detects if path contains a cycle.
     *
     * @param node
     *         Current node.
     * @param visited
     *         Visited nodes.
     * @return true/false
     */
    private boolean cycleDetect(final Operation node, final List<Operation> visited) {

        if (visited.contains(node)) {
            LOG.trace("Node: {}", node);
            return true;
        }

        LOG.trace("Checking node: {}", node.toString());

        visited.add(node);

        LOG.trace("Looping edges: {}", node.getActiveEdges());
        for (final Edge edge : node.getActiveEdges()) {

            if (edge.getOperationTo() instanceof EndVertex) {
                continue;
            } else {
                if (cycleDetect(edge.getOperationTo(), visited)) return true;
            }
        }

        visited.remove(visited.size() - 1);
        return false;
    }
}
