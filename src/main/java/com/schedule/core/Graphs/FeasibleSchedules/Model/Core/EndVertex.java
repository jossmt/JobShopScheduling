package com.schedule.core.Graphs.FeasibleSchedules.Model.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * End dummy vertex (SINK).
 */
public class EndVertex extends Operation implements Serializable {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(EndVertex.class);

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
    @Override
    public Set<Edge> getParentEdges() {
        return endParentEdges;
    }

    @Override
    public boolean hasParentEdges(){

        return !endParentEdges.isEmpty();
    }

    /**
     * Adds parent edge to end vertex.
     *
     * @param endParentEdge
     *         {@link Edge}
     */
    public void addEndParentEdge(final Edge endParentEdge) {
        endParentEdges.add(endParentEdge);
    }

    /**
     * Hash code means edges aren't removed once changed from the set
     * Manually removes edges from end vertex parent edges (hacky for now).
     */
    public void removeOverridenParentEdges() {

        final Set<Edge> genuineEdges = new HashSet<>();
        for (final Edge edge : endParentEdges) {
            if (edge.getOperationTo() instanceof EndVertex) {
                genuineEdges.add(edge);
            }
        }
        endParentEdges = genuineEdges;
    }
}
