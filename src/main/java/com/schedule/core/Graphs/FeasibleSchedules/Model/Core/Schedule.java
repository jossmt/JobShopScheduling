package com.schedule.core.Graphs.FeasibleSchedules.Model.Core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Schedule representing origin vertex (SOURCE).
 */
public class Schedule {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Schedule.class);

    /** HashMap of first task for each job. */
    private HashMap<Integer, Operation> jobHashMap;

    /** Number of machines. */
    private Integer numMachines;

    /** Number of jobs. */
    private Integer numJobs;

    /** End pointing vertex. */
    private EndVertex endVertex;

    /** Set of all vertices. */
    private Set<Operation> allVertices;

    /** Set of all edges. */
    private Set<Edge> allEdges;

    /** Makespan. */
    private Integer makespan;

    /**
     * Constructor.
     */
    public Schedule(final Integer numJobs, final Integer numMachines) {

        jobHashMap = new HashMap<>();
        /* Active Edges from root vertex. */
        this.numJobs = numJobs;
        this.numMachines = numMachines;

        final Integer taskNumber = numJobs * numMachines;
        endVertex = new EndVertex(taskNumber, -1, -1);
    }

    /**
     * Adds operation to tree
     *
     * @param processingTime
     *         Time for operation to process
     * @param operation
     *         TaskModel info.
     */
    public void addVertex(final Integer processingTime, final Operation operation) {

        updateConjunctiveEdges(processingTime, operation);
        updateDisjunctiveEdges(operation);

    }

    /**
     * Adds conjunctive arc between vertices.
     *
     * @param processingTime
     *         Time for operation to process
     * @param operation
     *         TaskModel info.
     */
    private void updateConjunctiveEdges(final Integer processingTime, final Operation operation) {

        if (jobHashMap.containsKey(operation.getJob())) {

            final Operation lastOperation = findFinalConjunctiveVertexForJob(jobHashMap.get(operation.getJob()));
            lastOperation.getConjunctiveEdge().setOperationTo(operation);
            operation.setConjunctiveParent(lastOperation.getConjunctiveEdge());

        } else {

            operation.setConjunctiveEdge(operation.getConjunctiveEdge());
            jobHashMap.put(operation.getJob(), operation);
        }
        operation.setConjunctiveEdge(new Edge(operation, endVertex, processingTime));
        endVertex.addEndParentEdge(operation.getConjunctiveEdge());
    }

    /**
     * Adds disjunctive arcs between vertices.
     *
     * @param operation
     *         TaskModel info.
     */
    private void updateDisjunctiveEdges(final Operation operation) {

        //Updates current job as checked
        final Set<Integer> uncheckedJobs = new HashSet<>(jobHashMap.keySet());
        if (uncheckedJobs.contains(operation.getJob())) {
            uncheckedJobs.remove(operation.getJob());
        }

        //Continues through unchecked jobs
        while (!uncheckedJobs.isEmpty()) {

            final Integer jobToCheck = uncheckedJobs.iterator().next();

            //Finds first available job/machine match
            final Operation relevantOperationForJob = findVertexForMachine(operation.getMachine(), jobHashMap.get
                    (jobToCheck));

            //If not null, run depth first update vertices to add disjunctive edges
            if (relevantOperationForJob != null) {
                final Set<Integer> updatedEdges =
                        depthFirstUpdateVertices(new HashSet<>(), operation, relevantOperationForJob);
                uncheckedJobs.removeAll(updatedEdges);

            } else {

                uncheckedJobs.remove(jobToCheck);
            }
        }
    }

    /**
     * Does a depth-first-search on vertices, adding disjunctive arcs between vertices that haven't yet been updated
     *
     * @param newOperation
     *         New vertex we're adding
     * @param oldOperation
     *         A vertex that already exists (running on same machine)
     * @return Jobs that have now been checked for given machine
     */
    private Set<Integer> depthFirstUpdateVertices(final Set<Integer> depthFirstUpdatedVertices, final Operation
            newOperation, final Operation oldOperation) {

        newOperation.addDisjunctiveEdge(new Edge(newOperation, oldOperation, newOperation.getConjunctiveEdge()
                .getProcessingTime()));

        depthFirstUpdatedVertices.add(oldOperation.getJob());
        if (oldOperation.hasDisjunctiveEdges()) {

            for (final Edge disjunctiveEdge : oldOperation.getInactiveDisjunctiveEdges()) {

                if (!depthFirstUpdatedVertices.contains(disjunctiveEdge.getOperationTo().getJob())) {
                    depthFirstUpdatedVertices
                            .addAll(depthFirstUpdateVertices(
                                    depthFirstUpdatedVertices, newOperation, disjunctiveEdge.getOperationTo()));
                }
            }

        }
        oldOperation.addDisjunctiveEdge(
                new Edge(oldOperation, newOperation, oldOperation.getConjunctiveEdge().getProcessingTime()));

        return depthFirstUpdatedVertices;
    }


    /**
     * Finds final conjunctive operation for the job.
     *
     * @param operation
     *         TaskModel details.
     * @return {@link Operation}
     */
    private Operation findFinalConjunctiveVertexForJob(final Operation operation) {

        if (operation.hasNeighbour()) {

            return findFinalConjunctiveVertexForJob(operation.getConjunctiveEdge().getOperationTo());
        }
        return operation;
    }

    /**
     * Finds operation that runs on machine x for given job route operation start y
     *
     * @param machine
     *         Machine operation should run on.
     * @param operation
     *         Starts withh first operation in job.
     * @return ScheduleTask that runs on given machine.
     */
    private Operation findVertexForMachine(final Integer machine, final Operation operation) {

        if (operation.getMachine().equals(machine)) {
            return operation;
        } else if (operation.hasNeighbour()) {

            return findVertexForMachine(machine, operation.getConjunctiveEdge().getOperationTo());
        }

        return null;
    }

    /**
     * Returns all vertices.
     *
     * @return Operation set.
     */
    public Set<Operation> getAllVertices() {

        if (allVertices != null) return allVertices;

        else {
            final Set<Operation> vertices = new HashSet<>();
            for (final Integer job : jobHashMap.keySet()) {

                Operation operation = jobHashMap.get(job);
                while (operation.hasNeighbour()) {

                    vertices.add(operation);
                    operation = operation.getConjunctiveEdge().getOperationTo();
                }
                //adds final operation machine too
                vertices.add(operation);
            }

            allVertices = vertices;

            LOG.trace("Number of vertices: {}", vertices.size());
            return vertices;
        }
    }


    /**
     * Returns operation in job that runs on given machine adding active edges until reached.
     *
     * @param operation
     *         {@link Operation}
     * @param machine
     *         Machine.
     * @return {@link Operation}
     */
    private Operation findJobTaskWithMachine(final Operation operation, final Integer machine) {

        LOG.trace("Checking operation: {} for machine: {}", operation, machine);

        if (operation.getMachine() == machine) {
            return operation;
        }

        if (operation.hasNeighbour()) {
            return findJobTaskWithMachine(operation.getConjunctiveEdge().getOperationTo(), machine);
        }

        throw new IllegalStateException("Missing machine:" + machine + " from job " + operation.getJob());
    }

    /**
     * Returns vertex for job and machine params given.
     *
     * @param job
     *         Job
     * @param machine
     *         Machine.
     * @return {@link Operation}
     */
    public Operation locateOperation(final Integer job, final Integer machine) {

        LOG.trace("Locating operation for job: {} and machine: {}", job, machine);

        Operation operation = jobHashMap.get(job);
        while (!Objects.equals(operation.getMachine(), machine)) {

            LOG.trace("Continue looking machine: {} incorrect", operation.getMachine());

            operation = operation.getConjunctiveEdge().getOperationTo();
        }

        LOG.trace("Found machine");

        return operation;
    }

    /**
     * Sets active edges (based on random schedule builder)
     *
     * @param job
     *         Job of Operation
     * @param lastJob
     *         Last job in task order
     * @param machine
     *         Machine of Operation
     */
    public void setActiveEdge(final Integer lastJob, final Integer job, final Integer machine) {

        final Operation operationTo = findJobTaskWithMachine(jobHashMap.get(job), machine);

        if (lastJob != null) {

            final Operation operationFrom = findJobTaskWithMachine(jobHashMap.get(lastJob), machine);

            final Edge disjunctiveEdge = new Edge(operationFrom, operationTo, operationFrom.getProcessingTime());
            operationFrom.setDisjunctiveEdge(disjunctiveEdge);
            operationTo.setDisjunctiveParent(disjunctiveEdge);
        }
    }

    /**
     * Sets new Hashmap of vertices..
     *
     * @param jobHashMap
     *         New value of Hashmap of vertices..
     */
    public void setJobHashMap(final HashMap<Integer, Operation> jobHashMap) {
        this.jobHashMap = jobHashMap;
    }

    /**
     * Gets Hashmap of vertices..
     *
     * @return Value of Hashmap of vertices..
     */
    public HashMap<Integer, Operation> getJobHashMap() {
        return jobHashMap;
    }

    /**
     * Gets End pointing vertex..
     *
     * @return Value of End pointing vertex..
     */
    public EndVertex getEndVertex() {
        return endVertex;
    }

    /**
     * Gets Makespan..
     *
     * @return Value of Makespan..
     */
    public Integer getMakespan() {
        return makespan;
    }

    /**
     * Sets new Makespan..
     *
     * @param makespan
     *         New value of Makespan..
     */
    public void setMakespan(Integer makespan) {
        this.makespan = makespan;
    }


    /**
     * Gets Number of jobs..
     *
     * @return Value of Number of jobs..
     */
    public Integer getNumJobs() {
        return numJobs;
    }

    /**
     * Returns all active disjunctive edges.
     *
     * @return set of {@link Edge}
     */
    public ArrayList<Edge> getAllMachineEdgesManually() {

        final Set<Edge> machineEdges = new HashSet<>();
        for (final Operation operation : jobHashMap.values()) {

            Operation current = operation;
            while (current.hasConjunctiveEdge()) {

                if (current.getDisjunctiveEdge() != null) {
                    machineEdges.add(current.getDisjunctiveEdge());
                }

                current = current.getConjunctiveEdge().getOperationTo();
            }
        }

        allEdges = machineEdges;

        return new ArrayList<>(machineEdges);
    }

    /**
     * Gets Set of all edges..
     *
     * @return Value of Set of all edges..
     */
    public Set<Edge> getAllEdges() {
        return allEdges;
    }

    /**
     * Equals builder.
     */
    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof Schedule)) {
            return false;
        }

        final Schedule compareSchedule = (Schedule) obj;

        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(getAllEdges(), compareSchedule.getAllEdges());
        equalsBuilder.append(getJobHashMap(), compareSchedule.getJobHashMap());
        equalsBuilder.append(getMakespan(), compareSchedule.getMakespan());

        return equalsBuilder.isEquals();
    }

    /**
     * Hash builder.
     */
    @Override
    public int hashCode() {

        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(getJobHashMap());
        hashCodeBuilder.append(getMakespan());
        hashCodeBuilder.append(getAllEdges());

        return hashCodeBuilder.toHashCode();
    }

    /**
     * Stringified graph formatting for visual debugging.
     *
     * @return String.
     */
    @Override
    public String toString() {

        final StringBuilder stringBuilder = new StringBuilder();
        for (final Integer job : jobHashMap.keySet()) {

            stringBuilder.append(printVertex(jobHashMap.get(job)));
            stringBuilder.append("\n\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Returns stringified operation details.
     *
     * @param operation
     *         TaskModel info.
     * @return String.
     */
    private String printVertex(final Operation operation) {

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\n(J:").append(operation.getJob()).append(", M:").append(operation.getMachine()).append
                (")\n");

        // Prints active edges.
        if (operation.hasActiveEdges()) {
            for (final Edge edge : operation.getActiveEdges()) {
                stringBuilder.append("---").append(edge.getProcessingTime()).append("--->")
                        .append("(J:").append(edge.getOperationTo().getJob()).append(", M:")
                        .append(edge.getOperationTo().getMachine()).append(")\n");
            }
        }

        // Prints parent edges
        if (operation.hasParentEdges()) {
            for (final Edge edge : operation.getParentEdges()) {
                stringBuilder.append("<---").append(edge.getProcessingTime()).append("---")
                        .append("(J:").append(edge.getOperationFrom().getJob()).append(", M:")
                        .append(edge.getOperationFrom().getMachine()).append(")\n");

            }
        }

        if (operation.hasNeighbour()) {
            stringBuilder.append(printVertex(operation.getConjunctiveEdge().getOperationTo()));
        }
        return stringBuilder.toString();
    }
}
