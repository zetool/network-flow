/*
 * DynamicFlowProblem.java
 *
 */

package de.tu_berlin.coga.netflow.dynamic.problems;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;

/**
 *
 * @author Martin Groß
 */
public class DynamicFlowProblem {

    protected IdentifiableIntegerMapping<Edge> edgeCapacities;
    private DirectedGraph network;
    protected IdentifiableIntegerMapping<Edge> transitTimes;

	public DynamicFlowProblem( IdentifiableIntegerMapping<Edge> edgeCapacities, DirectedGraph graph, IdentifiableIntegerMapping<Edge> transitTimes ) {
		this.edgeCapacities = edgeCapacities;
		this.network = graph;
		this.transitTimes = transitTimes;
	}
		
		
		public IdentifiableIntegerMapping<Edge> getEdgeCapacities() {
        return edgeCapacities;
    }

//    public void setCapacities(IdentifiableIntegerMapping<Edge> capacities) {
//        this.edgeCapacities = capacities;
//    }

    public DirectedGraph getNetwork() {
        return network;
    }

//    public void setNetwork(AbstractNetwork network) {
//        this.network = network;
//    }

    public IdentifiableIntegerMapping<Edge> getTransitTimes() {
        return transitTimes;
    }

//    public void setTransitTimes(IdentifiableIntegerMapping<Edge> transitTimes) {
//        this.transitTimes = transitTimes;
//    }

}
