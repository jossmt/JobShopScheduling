package com.schedule.core.Graphs.FeasibleSchedules.Model.Core;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * End dummy vertex.
 */
public class EndVertex extends Operation implements Serializable {

    /**
     * End Edges.
     */
    private Set<Edge> endParentEdges;

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
    public EndVertex(Integer id, Integer job, Integer machine) {
        super(id, job, machine);

        endParentEdges = new HashSet<>();
    }


    /**
     * Gets endEdges.
     *
     * @return Value of endEdges.
     */
    public Set<Edge> getEndParentEdges() {
        return endParentEdges;
    }

    /**
     * Adds parent edge to end vertex.
     *
     * @param endParentEdge
     *         {@link Edge}
     */
    public void addEndParentEdge(final Edge endParentEdge) {

        endParentEdges.removeIf(edge -> edge.getOperationFrom().getJob() == endParentEdge.getOperationFrom().getJob());
        endParentEdges.add(endParentEdge);
    }
}
