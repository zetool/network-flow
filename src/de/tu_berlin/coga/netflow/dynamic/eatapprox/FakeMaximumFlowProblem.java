/**
 * FakeMaximumFlowProblem.java
 * Created: 14.01.2014, 15:40:49
 */
package de.tu_berlin.coga.netflow.dynamic.eatapprox;

import de.tu_berlin.coga.netflow.ds.network.NetworkInterface;
import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FakeMaximumFlowProblem extends MaximumFlowProblem {
	private HidingResidualGraph hidingResidualGraph;

	public FakeMaximumFlowProblem( NetworkInterface network, HidingResidualGraph residual ) {
		super( network, null, residual.getNode( residual.SUPER_SOURCE ), residual.getNode( residual.SUPER_SINK ) );
		this.hidingResidualGraph = residual;
	}

	HidingResidualGraph getResidualGraph() {
		return hidingResidualGraph;
	}

}
