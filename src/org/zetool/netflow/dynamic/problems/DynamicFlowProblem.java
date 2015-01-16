/*
 * DynamicFlowProblem.java
 *
 */

package org.zetool.netflow.dynamic.problems;

import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;

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
