package com.schedule.core.Graphs.FeasibleSchedules.Model.Core;

import com.rits.cloning.Cloner;
import com.schedule.core.Graphs.FeasibleSchedules.Model.Other.LRUCache;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * StaticSchedule essentially representing origin vertex for job shop scheduling
 */
public class Schedule implements Serializable {

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

    /** Makespan. */
    private Integer makespan;

    /** Machine edges on longest path. */
    private ArrayList<Edge> machineEdgesOnLP;

    /** Machine edges NOT on longest path. */
    private Set<Edge> machineEdgesNotOnLP;

    /** BackBone Score (Firefly). */
    private Integer backBoneScore = 0;

    /** Least Recently Used Cache of flipped Edges. */
    private LRUCache<Edge, Double> lruEdgeCache;

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
     * Copy constructor.
     *
     * @param schedule
     *         {@link Schedule}
     */
    public Schedule(final Schedule schedule) {

        this.jobHashMap = schedule.getJobHashMap();
        this.makespan = schedule.getMakespan();
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
            final Operation relevantOperationForJob = findVertexForMachine(operation.getMachine(),
                                                                           jobHashMap.get(jobToCheck));

            //If not null, run depth first update vertices to add disjunctive edges
            if (relevantOperationForJob != null) {
                final Set<Integer> updatedEdges = depthFirstUpdateVertices(new HashSet<>(), operation,
                                                                           relevantOperationForJob);
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
                    depthFirstUpdatedVertices.addAll(depthFirstUpdateVertices(depthFirstUpdatedVertices, newOperation,
                                                                              disjunctiveEdge.getOperationTo()));
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
     * Returns all active machine edges.
     *
     * @return set of {@link Edge}
     */
    public Set<Edge> getAllMachineEdges() {

        final Set<Edge> allMachineEdges = new HashSet<>();

        if (machineEdgesOnLP != null) {
            allMachineEdges.addAll(machineEdgesOnLP);
        }
        if (machineEdgesNotOnLP != null) {
            allMachineEdges.addAll(machineEdgesNotOnLP);
        }

        return allMachineEdges;
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
     * @param taskParams
     *         Params of Operation
     */
    public void setActiveEdge(final Integer job, final Integer lastJob, final Integer[] taskParams) {

        final Operation operationTo = findJobTaskWithMachine(jobHashMap.get(job), taskParams[0]);

        if (lastJob != null) {

            final Operation operationFrom = findJobTaskWithMachine(jobHashMap.get(lastJob), taskParams[0]);

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
     * Gets Number of machines and therefore jobs..
     *
     * @return Value of Number of machines and therefore jobs..
     */
    public Integer getNumMachines() {
        return numMachines;
    }

    /**
     * Get backbone score.
     *
     * @return Backbone score.
     */
    public Integer getBackBoneScore() {
        return backBoneScore;
    }

    /**
     * Updates backbone score.
     *
     * @param value
     *         Score.
     */
    public void updateBackBoneScore(final Integer value) {

        backBoneScore += value;
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
     * Sets new Machine edges on longest path..
     *
     * @param machineEdgesOnLP
     *         New value of Machine edges on longest path..
     */
    public void setMachineEdgesOnLP(ArrayList<Edge> machineEdgesOnLP) {
        this.machineEdgesOnLP = machineEdgesOnLP;

        calculateAllMachineEdgesNotOnLP();
    }

    /**
     * Gets Machine edges NOT on longest path..
     *
     * @return Value of Machine edges NOT on longest path..
     */
    public Set<Edge> getMachineEdgesNotOnLP() {
        return machineEdgesNotOnLP;
    }

    /**
     * Sets new Machine edges NOT on longest path..
     *
     * @param machineEdgesNotOnLP
     *         New value of Machine edges NOT on longest path..
     */
    public void setMachineEdgesNotOnLP(Set<Edge> machineEdgesNotOnLP) {
        this.machineEdgesNotOnLP = machineEdgesNotOnLP;
    }

    /**
     * Returns all active disjunctive edges not on longest path.
     */
    private void calculateAllMachineEdgesNotOnLP() {


        final Set<Edge> machineEdges = getAllMachineEdgesManually();

        machineEdges.removeAll(machineEdgesOnLP);

        machineEdgesNotOnLP = machineEdges;
    }

    /**
     * Returns all active disjunctive edges.
     *
     * @return set of {@link Edge}
     */
    public Set<Edge> getAllMachineEdgesManually() {

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

        return machineEdges;
    }

    /**
     * Gets Machine edges on longest path..
     *
     * @return Value of Machine edges on longest path..
     */
    public ArrayList<Edge> getMachineEdgesOnLP() {
        return machineEdgesOnLP;
    }

    /**
     * Gets Machine edges on longest path..
     *
     * @return Value of Machine edges on longest path..
     */
    public Set<Edge> getMachineEdgesOnLPSet() {
        return new HashSet<>(machineEdgesOnLP);
    }

    /**
     * Initialises least recently used cache
     * with size based on size of schedule.
     */
    public void initialiseCache() {

        final Integer cacheSize = Math.max(numJobs, numMachines);

        lruEdgeCache = new LRUCache<>(cacheSize);
    }

    /**
     * Sets new Least Recently Used Cache of flipped Edges..
     *
     * @param lruEdge
     *         New value of Least Recently Used Cache of flipped Edges..
     */
    public void updateLruEdgeCache(final Edge lruEdge) {

        final Cloner cloner = new Cloner();

        if (lruEdgeCache.containsKey(lruEdge)) {

            final Double priorProbability = lruEdgeCache.get(lruEdge);

            lruEdgeCache.put(cloner.deepClone(lruEdge), priorProbability * 0.9);
        } else {

            lruEdgeCache.put(cloner.deepClone(lruEdge), 0.9);
        }
    }

    /**
     * Gets Least Recently Used Cache of flipped Edges..
     *
     * @return Value of Least Recently Used Cache of flipped Edges..
     */
    public LRUCache getLruEdgeCache() {
        return lruEdgeCache;
    }

    /**
     * Clears cache of least recently used edges.
     */
    public void clearCache() {

        initialiseCache();
    }

    /**
     * Returns acceptance probability of cached edge.
     *
     * @param edge
     *         {@link Edge}
     * @return Optional value.
     */
    public Optional<Double> getCachedEdgeAcceptanceProb(final Edge edge) {

        Double value = null;
        if (lruEdgeCache.containsKey(edge)) {
            value = lruEdgeCache.get(edge);
        }

        return Optional.ofNullable(value);
    }

    /**
     * Adds edge to longest path list.
     *
     * @param edge
     *         {@link Edge}
     */
    public void addLongestPathEdge(final Edge edge) {

        machineEdgesOnLP.add(edge);
    }

    /**
     * Adds edges to longest path list.
     *
     * @param edges
     *         {@link Edge}
     */
    public void addLongestPathEdges(final Set<Edge> edges) {

        machineEdgesOnLP.addAll(edges);
    }

    /**
     * Clears longest path.
     */
    public void clearLongestPaths() {
        machineEdgesOnLP = new ArrayList<>();
    }

    /**
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof Schedule)) {
            return false;
        }

        final Schedule compareSchedule = (Schedule) obj;

        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(getAllMachineEdges(), compareSchedule.getAllMachineEdges());
        equalsBuilder.append(getJobHashMap(), compareSchedule.getJobHashMap());
        equalsBuilder.append(getMakespan(), compareSchedule.getMakespan());

        return equalsBuilder.isEquals();
    }

    /**
     * Equals.
     */
    @Override
    public int hashCode() {

        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(getJobHashMap());
        hashCodeBuilder.append(getMakespan());
        hashCodeBuilder.append(getAllMachineEdges());

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
