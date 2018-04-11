package com.schedule.core.Graphs.FeasibleSchedules.Service;

import com.github.rkumsher.collection.IterableUtils;
import com.schedule.core.Graphs.FeasibleSchedules.Config.FileDataPaths;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.EndVertex;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Operation;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Schedule;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Schedule Service handling functionality directly related to the {@link Schedule} object.
 */
public class ScheduleService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleService.class);

    /** {@link FeasibilityService}. */
    private FeasibilityService feasibilityService = new FeasibilityService();

    /**
     * Constructor.
     */
    public ScheduleService() {
    }

    /**
     * Switches edges on machine path.
     *
     * @param edge
     *         {@link Edge}
     */
    public void switchEdge(final Edge edge) {

        LOG.trace("Switching edges");

        Edge forwardEdge = edge.getOperationTo().getDisjunctiveEdge();
        Edge backwardEdge = edge.getOperationFrom().getDisjunctiveParent();

        LOG.trace("Edges forward: {} backward: {}", forwardEdge, backwardEdge);

        if (backwardEdge != null) {
            backwardEdge.setOperationTo(edge.getOperationTo());
            backwardEdge.getOperationFrom().setDisjunctiveEdge(backwardEdge);
            edge.getOperationTo().setDisjunctiveParent(backwardEdge);
        } else {
            edge.getOperationTo().deactivateDisjunctiveParent();
        }

        if (forwardEdge != null) {
            forwardEdge.setOperationFrom(edge.getOperationFrom());
            forwardEdge.setProcessingTime(edge.getOperationFrom().getProcessingTime());
            forwardEdge.getOperationTo().setDisjunctiveParent(forwardEdge);
            edge.getOperationFrom().setDisjunctiveEdge(forwardEdge);
        } else {
            edge.getOperationFrom().deactivateDisjunctive();
        }

        LOG.trace("Surrounding edges updates: forward {} back {}", forwardEdge, backwardEdge);

        edge.flipDisjunctive();

        LOG.trace("Edge after flipping: {}", edge);

    }

    /**
     * Finds random edge
     *
     * @param edgeOptions
     *         set of {@link Edge}
     * @return {@link Edge}
     */
    public Optional<Edge> findRandomEdge(final ArrayList<Edge> edgeOptions) {

        Edge edgeFlipped = null;
        if (!edgeOptions.isEmpty()) {
            edgeFlipped = IterableUtils.randomFrom(edgeOptions);
            edgeOptions.remove(edgeFlipped);
        }

        return Optional.ofNullable(edgeFlipped);
    }

    /**
     * Topologically sorts graph.
     *
     * @return Topologically sorted queue.
     */
    public Deque<Operation> topologicalSort(final Schedule schedule) {

        //Holds topologically sorted vertices
        final Deque<Operation> stack = new ArrayDeque<>();

        //Visited vertices
        final Set<Operation> visited = new HashSet<>();
        for (final Operation operation : schedule.getAllVertices()) {

            if (visited.contains(operation)) continue;

            topologicalSort(operation, stack, visited);
        }

        return stack;
    }

    /**
     * Returns a topologically sorted stack given parent operation.
     *
     * @param operation
     *         Operation
     * @param stack
     *         Stack
     * @param visited
     *         Visited vertices
     */
    private void topologicalSort(final Operation operation, final Deque<Operation> stack, final Set<Operation>
            visited) {

        LOG.trace("Topologically sorting operation: {} with stack: {} and visited: {}", operation, stack, visited);

        visited.add(operation);

        //DFS active edges & add to stack
        if (operation.hasActiveEdges()) {

            for (final Edge activeEdge : operation.getActiveEdges()) {

                final Operation childOperation = activeEdge.getOperationTo();
                if (visited.contains(childOperation)) {
                    continue;
                }

                topologicalSort(childOperation, stack, visited);
            }
        }
        stack.offerFirst(operation);
    }


    /**
     * Calculates makespan using reverse modification of Dijkstras Algorithm.
     *
     * @return Makespan
     */
    public void calculateMakeSpan(final Schedule schedule) {

        LOG.trace("Calculating Makespan");

        //Sorts vertices
        final Deque<Operation> topologicalSort = topologicalSort(schedule);

        //Array of longest paths from route.
        Integer[] dist = new Integer[topologicalSort.size()];
        dist[topologicalSort.peek().getId()] = -topologicalSort.peek().getConjunctiveEdge().getProcessingTime();

        // Loop through vertices updating longest paths
        while (!topologicalSort.isEmpty()) {

            final Operation operation = topologicalSort.pop();

            // If the start operation of a new path set dist to processing time
            if (dist[operation.getId()] == null) {
                dist[operation.getId()] = -operation.getConjunctiveEdge().getProcessingTime();
            }

            //Updates adjacent edges with largest distance
            final Set<Edge> adjacentEdges = operation.getActiveEdges();
            for (final Edge edge : adjacentEdges) {

                //Find a better way of doing this with edge processing time
                int newDist = dist[operation.getId()];
                if (!(edge.getOperationTo() instanceof EndVertex))
                    newDist -= edge.getOperationTo().getConjunctiveEdge().getProcessingTime();

                if (dist[edge.getOperationTo().getId()] == null)
                    dist[edge.getOperationTo().getId()] = newDist;

                    //Sets minimum of negative distance (modification of shortest path search)
                else dist[edge.getOperationTo().getId()] = Math.min(dist[edge.getOperationTo().getId()], newDist);

                edge.setMaxDistanceToMe(-Math.min(dist[edge.getOperationTo().getId()], newDist));
                LOG.trace("New distance from: {} to {} == {}", operation, edge.getOperationTo(), newDist);
            }
        }

        final Integer makespan = -dist[dist.length - 1];
        schedule.setMakespan(makespan);

        LOG.trace("Makespan: {}", makespan);
    }

    /**
     * Finds feasible edge to flip from given list.
     *
     * @param schedule
     *         {@link Schedule}
     * @return Optional {@link Edge}
     */
    public void findFeasibleEdgeAndFlip(final Schedule schedule) {

        final ArrayList<Edge> allMachineEdges = schedule.getAllMachineEdgesManually();

        Optional<Edge> edgeOptional = findRandomEdge(allMachineEdges);
        while (edgeOptional.isPresent()) {
            final Edge edge = edgeOptional.get();

            switchEdge(edge);
            final Operation opFrom = edge.getOperationFrom();
            final Operation opTo = edge.getOperationTo();

            if (feasibilityService.scheduleIsFeasibleProof(opFrom, opTo)) {

                calculateMakeSpan(schedule);
                break;
            } else {
                switchEdge(edge);
                edgeOptional = findRandomEdge(allMachineEdges);
            }
        }
    }

    /**
     * Determines whether vertex one is before/after in machine path.
     *
     * @param operationOne
     *         {@link Operation}
     * @param operationTwo
     *         {@link Operation}
     * @return true/false
     */
    public boolean isInOrder(final Operation operationOne, final Operation operationTwo) {

        Operation currentOperation = operationOne;
        while (currentOperation.hasDisjunctiveEge()) {

            if (currentOperation.getDisjunctiveEdge().getOperationTo().equals(operationTwo)) {
                return true;
            }

            currentOperation = currentOperation.getDisjunctiveEdge().getOperationTo();
        }

        return false;
    }

    /**
     * Calculates random double between 0 and 1
     *
     * @return [0-1]
     */
    public Double randomDouble() {

        final Random random = new Random();
        return random.nextInt(100) / 100.0;
    }

    /**
     * Shuts down executor service when all threads complete.
     */
    public void removeCompletedThreads(final List<Future<Schedule>> allRunningThreads) {

        LOG.trace("Removing completed threads from cache, size: {}", allRunningThreads.size());

        final List<Future<Schedule>> toRemove = new ArrayList<>();
        if (!allRunningThreads.isEmpty()) {

            for (Future<Schedule> currentThread : allRunningThreads) {

                try {
                    currentThread.get();

                    if (currentThread.isDone()) {
                        toRemove.add(currentThread);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        allRunningThreads.removeAll(toRemove);
    }

    /**
     * Generates graph code: stringified code for graphviz converter.
     */
    public void generateGraphCode(final Schedule schedule, final String fileName) {

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("digraph G { \nrankdir=LR;\n");

        final StringBuilder clusters = new StringBuilder();
        final StringBuilder directedEdges = new StringBuilder();
        final StringBuilder endEdges = new StringBuilder();
        final StringBuilder startEdges = new StringBuilder();

        for (final Integer job : schedule.getJobHashMap().keySet()) {

            final Operation firstOperation = schedule.getJobHashMap().get(job);
            startEdges.append("start ->").append("J").append(firstOperation.getJob()).append("M")
                    .append(firstOperation.getMachine()).append(";");

            final StringBuilder cluster = new StringBuilder();
            cluster.append("subgraph cluster_J").append(job).append("{\n");

            Operation currentOperation = firstOperation;
            while (currentOperation.hasConjunctiveEdge()) {

                if (!(currentOperation instanceof EndVertex)) {
                    cluster.append("J").append(currentOperation.getJob()).append("M").append(currentOperation
                                                                                                     .getMachine());

                    if (!(currentOperation.getConjunctiveEdge().getOperationTo() instanceof EndVertex)) {
                        cluster.append(" -> ");
                    } else {
                        endEdges.append("J").append(currentOperation.getJob()).append("M").append(currentOperation
                                                                                                          .getMachine
                                                                                                                  ())
                                .append(" -> ").append("E;\n");
                    }
                }

                if (currentOperation.getDisjunctiveEdge() != null) {
                    directedEdges.append("J").append(currentOperation.getJob()).append("M").append(currentOperation
                                                                                                           .getMachine
                                                                                                                   ()
                    ).append(" -> ")
                            .append("J").append(currentOperation.getDisjunctiveEdge().getOperationTo().getJob())
                            .append("M")
                            .append(currentOperation.getDisjunctiveEdge().getOperationTo().getMachine())
                            .append("[constraint=false];\n");

                }

                currentOperation = currentOperation.getConjunctiveEdge().getOperationTo();
            }

            cluster.append("\n};\n");
            clusters.append(cluster);

        }

        stringBuilder.append(startEdges);
        stringBuilder.append(clusters);
        stringBuilder.append(endEdges);
        stringBuilder.append(directedEdges);

        stringBuilder.append("\n}");

        try {
            MutableGraph g = Parser.read(stringBuilder.toString());

            Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(
                    new File(FileDataPaths.GENERATED_GRAPH_PATH + "schedule-" + fileName + ".png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
