/**
 * FlowOverTime.java
 * Created: 07.06.2012, 16:23:08
 */
package de.tu_berlin.coga.netflow.ds.flow;

import de.tu_berlin.coga.netflow.dynamic.problems.DynamicFlowProblem;
import de.tu_berlin.coga.netflow.classic.PathComposition;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FlowOverTime implements FlowOverTimeInterface {
	/** Flow based result flow. */
	protected PathBasedFlowOverTime resultFlowPathBased = null;
	/** Edge based result flow. */
	protected EdgeBasedFlowOverTime resultFlowEdgeBased = null;
	DynamicFlowProblem dfp;

	public FlowOverTime( PathBasedFlowOverTime resultFlowPathBased, DynamicFlowProblem dfp ) {
		this.resultFlowPathBased = resultFlowPathBased;
		this.dfp = dfp;
	}

	@Override
	public EdgeBasedFlowOverTime getEdgeBased() {
		if( resultFlowEdgeBased != null )
			return resultFlowEdgeBased;
		if( resultFlowPathBased != null ) {
			assert (dfp.getNetwork() != null);
			assert (dfp.getTransitTimes() != null);
			PathComposition pathComposition = new PathComposition( dfp.getNetwork(), dfp.getTransitTimes(), resultFlowPathBased );
			pathComposition.run();
			resultFlowEdgeBased = pathComposition.getEdgeFlows();
			return resultFlowEdgeBased;
		}
		return null;
	}

	@Override
	public PathBasedFlowOverTime getPathBased() {
		return resultFlowPathBased;
	}
}
