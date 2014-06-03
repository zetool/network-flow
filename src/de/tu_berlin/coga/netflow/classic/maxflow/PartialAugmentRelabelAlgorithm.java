/**
 * PartialAugmentRelabelAlgorithm.java
 * Created: Oct 5, 2010,5:17:40 PM
 */
package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.flow.MaximumFlow;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class PartialAugmentRelabelAlgorithm extends PushRelabel {

	@Override
	protected MaximumFlow runAlgorithm( MaximumFlowProblem problem ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected int push( Edge e ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected int relabel( Node v ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected void computeMaxFlow() {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected void makeFeasible() {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

}
