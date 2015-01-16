
package org.zetool.netflow.dynamic.problems;

import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class DynamicTransshipmentProblem extends DynamicFlowProblem {
	protected IdentifiableIntegerMapping<Node> nodeCapacities;
	protected IdentifiableIntegerMapping<Node> supplies;
	private int timeHorizon;

	public DynamicTransshipmentProblem( IdentifiableIntegerMapping<Edge> edgeCapacities, DirectedGraph network, IdentifiableIntegerMapping<Node> nodeCapacities, int timeHorizon, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Node> supplies ) {
		super( edgeCapacities, network, transitTimes );
		this.nodeCapacities = nodeCapacities;
		this.supplies = supplies;
		this.timeHorizon = timeHorizon;
		//  this.transitTimes = transitTimes;
//        for (Node source : sources) {
//            totalSupplies += supplies.get(source);
//        }
	}

	public int getTimeHorizon() {
		return timeHorizon;
	}

	/**
	 * Sets a new time horizon for the instance. Use this if a time horizon
	 * has changed, for example if an estimator has been used.
	 * @param timeHorizon the new time horizon
	 */
	public void setTimeHorizon( int timeHorizon ) {
		this.timeHorizon = timeHorizon;
	}

	public IdentifiableIntegerMapping<Node> getNodeCapacities() {
		return nodeCapacities;
	}

	public IdentifiableIntegerMapping<Node> getSupplies() {
		return supplies;
	}
}
