package com.schedule.core.Graphs.FeasibleSchedules.Model.Core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.*;

/**
 * Schedule operation.
 */
public class Operation implements Serializable {

    /** Identifier. */
    private Integer id;

    /** Job identifier. */
    private Integer job;

    /** Machine required. */
    private Integer machine;

    /** Edges going in and out of vertex. */
    private Edge disjunctiveParent;
    private Edge disjunctiveEdge;
    private Edge conjunctiveEdge;
    private Edge conjunctiveParent;

    /** Disjunctive edge to other jobs task, same machine requirement. */
    private ArrayList<Edge> inactiveDisjunctiveEdges;

    /**
     * Constructor.
     *
     * @param id
     *         Identifier.
     * @param job
     *         Job id.
     * @param machine
     *         Machine id.
     */
    public Operation(final Integer id, final Integer job, final Integer machine) {

        this.id = id;
        this.job = job;
        this.machine = machine;
        inactiveDisjunctiveEdges = new ArrayList<>();
    }

    /**
     * Checks conjunctiveEdge present.
     *
     * @return Value of conjunctiveEdge existence..
     */
    public boolean hasConjunctiveEdge() {

        return conjunctiveEdge != null;
    }

    /**
     * Checks conjunctiveParent present.
     *
     * @return Value of conjunctive parent existence.
     */
    public boolean hasConjunctiveParent() {
        return conjunctiveParent != null;
    }

    /**
     * Gets conjunctiveEdge.
     *
     * @return Value of conjunctiveEdge.
     */
    public Edge getConjunctiveEdge() {
        return conjunctiveEdge;
    }

    /**
     * Gets machine.
     *
     * @return Value of machine.
     */
    public Integer getMachine() {
        return machine;
    }

    /**
     * Sets new machine.
     *
     * @param machine
     *         New value of machine.
     */
    public void setMachine(Integer machine) {
        this.machine = machine;
    }

    /**
     * Sets new conjunctiveEdge.
     *
     * @param conjunctiveEdge
     *         New value of conjunctiveEdge.
     */
    public void setConjunctiveEdge(Edge conjunctiveEdge) {
        this.conjunctiveEdge = conjunctiveEdge;
    }

    /**
     * Asserts whether another task left for job.
     *
     * @return True if current task precedes another
     */
    public boolean hasNeighbour() {

        if (conjunctiveEdge != null) {

            if (conjunctiveEdge.getOperationTo().getJob() == -1) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Gets job.
     *
     * @return Value of job.
     */
    public Integer getJob() {
        return job;
    }

    /**
     * Sets new job.
     *
     * @param job
     *         New value of job.
     */
    public void setJob(Integer job) {
        this.job = job;
    }

    /**
     * Sets new Disjunctive edge to other jobs task, same machine requirement..
     *
     * @param inactiveDisjunctiveEdges
     *         New value of Disjunctive edge to other jobs task, same machine requirement..
     */
    public void setInactiveDisjunctiveEdges(final ArrayList<Edge> inactiveDisjunctiveEdges) {
        this.inactiveDisjunctiveEdges = inactiveDisjunctiveEdges;
    }

    /**
     * Gets Disjunctive edge to other jobs task, same machine requirement..
     *
     * @return Value of Disjunctive edge to other jobs task, same machine requirement..
     */
    public ArrayList<Edge> getInactiveDisjunctiveEdges() {
        return inactiveDisjunctiveEdges;
    }

    /**
     * Adds new disjunctive edge.
     *
     * @param edge
     *         {@link Edge}
     */
    public void addDisjunctiveEdge(final Edge edge) {

        inactiveDisjunctiveEdges.add(edge);
    }

    /**
     * Removes disjunctive edge.
     */
    public void deactivateDisjunctive() {
        disjunctiveEdge = null;
    }

    /**
     * Removes disjunctive parent edge.
     */
    public void deactivateDisjunctiveParent() {
        disjunctiveParent = null;
    }

    /**
     * Sets new Identifier..
     *
     * @param id
     *         New value of Identifier..
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets Identifier..
     *
     * @return Value of Identifier..
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets disjunctiveEdge.
     *
     * @return Value of disjunctiveEdge.
     */
    public Edge getDisjunctiveEdge() {
        return disjunctiveEdge;
    }

    /**
     * Has disjunctive edge.
     *
     * @return true/false
     */
    public boolean hasDisjunctiveEge() {
        if (getDisjunctiveEdge() != null) {
            return true;
        }
        return false;
    }

    /**
     * Gets Edges going in and out of vertex..
     *
     * @return Value of Edges going in and out of vertex..
     */
    public Edge getDisjunctiveParent() {
        return disjunctiveParent;
    }

    /**
     * Sets new Edges going in and out of vertex..
     *
     * @param disjunctiveParent
     *         New value of Edges going in and out of vertex..
     */
    public void setDisjunctiveParent(Edge disjunctiveParent) {
        this.disjunctiveParent = disjunctiveParent;
    }

    /**
     * Sets new conjunctiveParent.
     *
     * @param conjunctiveParent
     *         New value of conjunctiveParent.
     */
    public void setConjunctiveParent(Edge conjunctiveParent) {
        this.conjunctiveParent = conjunctiveParent;
    }

    /**
     * Gets conjunctiveParent.
     *
     * @return Value of conjunctiveParent.
     */
    public Edge getConjunctiveParent() {
        return conjunctiveParent;
    }

    /**
     * Sets new disjunctiveEdge.
     *
     * @param disjunctiveEdge
     *         New value of disjunctiveEdge.
     */
    public void setDisjunctiveEdge(Edge disjunctiveEdge) {
        this.disjunctiveEdge = disjunctiveEdge;
    }

    public Integer getProcessingTime() {

        return conjunctiveEdge.getProcessingTime();
    }

    /**
     * Returns edges going out of vertex.
     *
     * @return Set of {@link Edge}
     */
    public Set<Edge> getActiveEdges() {

        final Set<Edge> activeEdges = new HashSet<>();
        if (conjunctiveEdge != null) {
            activeEdges.add(conjunctiveEdge);
        }
        if (disjunctiveEdge != null) {
            activeEdges.add(disjunctiveEdge);
        }
        return activeEdges;
    }

    /**
     * Has active edges.
     *
     * @return true/false
     */
    public boolean hasActiveEdges() {

        return !(conjunctiveEdge == null && disjunctiveEdge == null);
    }

    /**
     * Returns edges going in to vertex.
     *
     * @return Set of {@link Edge}
     */
    public Set<Edge> getParentEdges() {

        final Set<Edge> parentEdges = new HashSet<>();
        if (conjunctiveParent != null) {
            parentEdges.add(conjunctiveParent);
        }
        if (disjunctiveParent != null) {
            parentEdges.add(disjunctiveParent);
        }

        return parentEdges;
    }

    /**
     * Has disjunctive parent edge.
     *
     * @return true/false
     */
    public boolean hasDisjunctiveParent() {

        return disjunctiveParent != null;
    }

    /**
     * Has disjunctive edges set.
     *
     * @return true/false
     */
    public boolean hasDisjunctiveEdges() {
        return !inactiveDisjunctiveEdges.isEmpty();
    }

    /**
     * Has parent edges set.
     *
     * @return true/false
     */
    public boolean hasParentEdges() {
        if (disjunctiveParent == null && conjunctiveParent == null) {
            return false;
        }
        return true;
    }

    /**
     * Equals.
     *
     * @param obj
     *         Object Compare.
     * @return True/False
     */
    @Override
    public boolean equals(final Object obj) {

        final Operation compareOperation = (Operation) obj;

        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(getJob(), compareOperation.getJob());
        equalsBuilder.append(getMachine(), compareOperation.getMachine());
        equalsBuilder.append(getId(), compareOperation.getId());

        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {

        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(getJob()).append(getMachine()).append(getId());

        return hashCodeBuilder.toHashCode();
    }


    /**
     * String representation of job.
     *
     * @return String.
     */
    @Override
    public String toString() {

        final StringBuilder stringBuilder = new StringBuilder();

        if (conjunctiveEdge != null) {
            stringBuilder.append("J: " + job + " M: " + machine + " PT: " + conjunctiveEdge.getProcessingTime());
        } else if (job == -1) {
            stringBuilder.append("Final Dummy Operation");

        } else {
            stringBuilder.append("J: " + job + " M: " + machine);
        }

        return stringBuilder.toString();
    }
}
