package com.schedule.core.Graphs.FeasibleSchedules.Wrapper;

import com.schedule.core.Graphs.FeasibleSchedules.Model.Core.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SchedulePaths {

    private List<Set<Edge>> longestpaths;

    private Boolean isFeasible = true;

    private Edge nodeCausingCycle;

    public SchedulePaths() {

        this.longestpaths = new ArrayList<>();
    }


    /**
     * Gets isFeasible.
     *
     * @return Value of isFeasible.
     */
    public Boolean isFeasible() {
        return isFeasible;
    }

    /**
     * Gets longestpaths.
     *
     * @return Value of longestpaths.
     */
    public List<Set<Edge>> getLongestpaths() {
        return longestpaths;
    }

    /**
     * Sets new longestpaths.
     *
     * @param longestpaths
     *         New value of longestpaths.
     */
    public void setLongestpaths(List<Set<Edge>> longestpaths) {
        this.longestpaths = longestpaths;
    }

    public void addPath(final Set<Edge> path){
        longestpaths.add(path);
    }

    /**
     * Sets new isFeasible.
     *
     * @param isFeasible
     *         New value of isFeasible.
     */
    public void setIsFeasible(boolean isFeasible) {

        this.isFeasible = isFeasible;
    }

    @Override
    public String toString() {

        return "Feasible: " + isFeasible + " Number of Longest Paths: " + longestpaths.size();
    }

    /**
     * Gets nodeCausingCycle.
     *
     * @return Value of nodeCausingCycle.
     */
    public Edge getNodeCausingCycle() {
        return nodeCausingCycle;
    }

    /**
     * Sets new nodeCausingCycle.
     *
     * @param nodeCausingCycle
     *         New value of nodeCausingCycle.
     */
    public void setNodeCausingCycle(Edge nodeCausingCycle) {
        this.nodeCausingCycle = nodeCausingCycle;
    }
}
