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
    public Set<Edge> endParentEdges;

    /**
     * Constructor.
     *
     * @param id
     *         Identifier.
     * @param job
     *         Job id.
     * @param machine
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
     * Sets new endEdges.
     *
     * @param endParentEdges
     *         New value of endParentEdges.
     */
    public void getEndParentEdges(Set<Edge> endParentEdges) {
        this.endParentEdges = endParentEdges;
    }

    public boolean hasEndParentEdges(){
        return !endParentEdges.isEmpty();
    }

    public void addEndParentEdge(final Edge endParentEdge) {

        endParentEdges.removeIf(edge -> edge.getOperationFrom().getJob() == endParentEdge.getOperationFrom().getJob());
        endParentEdges.add(endParentEdge);
    }
}
