package com.schedule.core.Graphs.FeasibleSchedules.Model.Core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Edge between vertices (tasks)
 */
public class Edge implements Serializable {

    /** Job edge points from. */
    private Operation operationFrom;

    /** Job edge points to. */
    private Operation operationTo;

    /** Time taken to process. */
    private Integer processingTime;

    /** Max dist for this edge. */
    private Integer maxDistanceToMe;

    /**
     * Constructor.
     *
     * @param operationFrom
     *         Job edges points from.
     * @param operationTo
     *         Job edge points to.
     * @param processingTime
     *         Processing Time.
     */
    public Edge(final Operation operationFrom, final Operation operationTo, final Integer processingTime) {

        this.operationFrom = operationFrom;
        this.operationTo = operationTo;
        this.processingTime = processingTime;
    }

    /**
     * Gets operationTo.
     *
     * @return Value of operationTo.
     */
    public Operation getOperationTo() {
        return operationTo;
    }

    /**
     * Sets new operationTo.
     *
     * @param operationTo
     *         New value of operationTo.
     */
    public void setOperationTo(Operation operationTo) {
        this.operationTo = operationTo;
    }

    /**
     * Gets processingTime.
     *
     * @return Value of processingTime.
     */
    public Integer getProcessingTime() {
        return processingTime;
    }

    /**
     * Sets new processingTime.
     *
     * @param processingTime
     *         New value of processingTime.
     */
    public void setProcessingTime(Integer processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Gets Job edge points from..
     *
     * @return Value of Job edge points from..
     */
    public Operation getOperationFrom() {
        return operationFrom;
    }

    /**
     * Sets new Job edge points from..
     *
     * @param operationFrom
     *         New value of Job edge points from..
     */
    public void setOperationFrom(Operation operationFrom) {
        this.operationFrom = operationFrom;
    }

    /**
     * Gets Max dist for this edge..
     *
     * @return Value of Max dist for this edge..
     */
    public Integer getMaxDistanceToMe() {
        return maxDistanceToMe;
    }

    /**
     * Sets new Max dist for this edge..
     *
     * @param maxDistanceToMe
     *         New value of Max dist for this edge..
     */
    public void setMaxDistanceToMe(Integer maxDistanceToMe) {
        this.maxDistanceToMe = maxDistanceToMe;
    }

    /**
     * Checks if has job to.
     *
     * @return true/false
     */
    public boolean hasJobTo() {
        return operationTo != null;
    }

    /**
     * Checks if machine path.
     *
     * @return true if is MP.
     */
    public boolean isMachinePath() {

        return operationTo.getMachine() == operationFrom.getMachine();
    }

    /**
     * Flips disjunctive edges
     */
    public void flipDisjunctive() {

        final Operation isJobTo = operationTo;
        final Operation isJobFrom = operationFrom;

        operationTo = isJobFrom;
        operationFrom = isJobTo;

        operationTo.setDisjunctiveParent(this);
        operationFrom.setDisjunctiveEdge(this);
        setProcessingTime(operationFrom.getProcessingTime());
    }

    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof Edge)) {
            return false;
        }
        final Edge compareEdge = (Edge) obj;

        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(getOperationFrom(), compareEdge.getOperationFrom());
        equalsBuilder.append(getOperationTo(), compareEdge.getOperationTo());
        equalsBuilder.append(getProcessingTime(), compareEdge.getProcessingTime());

        return equalsBuilder.isEquals();
    }

    /**
     * Hash code builder.
     *
     * @return Hash of edge.
     */
    @Override
    public int hashCode() {

        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(getOperationFrom());
        hashCodeBuilder.append(getOperationTo());
        hashCodeBuilder.append(getProcessingTime());
        return hashCodeBuilder.toHashCode();
    }

    /**
     * toString method.
     *
     * @return {@link String}
     */
    @Override
    public String toString() {

        final StringBuilder stringBuilder = new StringBuilder();

        if (operationFrom != null) {

            stringBuilder.append("PT: ").append(processingTime).append(" JFrom: ").append(operationFrom.getJob())
                    .append(" MFrom: ").append(operationFrom
                                                       .getMachine());
        }
        if (operationTo != null) {

            stringBuilder.append(" | JTo:").append(operationTo.getJob()).append(" MTo: ").append(operationTo
                                                                                                         .getMachine())
                    .append(",\n");
        }
        return stringBuilder.toString();
    }
}
