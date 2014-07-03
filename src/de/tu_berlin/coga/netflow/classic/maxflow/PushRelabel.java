/**
 * PushRelabel.java
 * Created: Oct 21, 2010, 6:03:08 PM
 */
package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.common.algorithm.Algorithm;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.network.OldResidualNetwork;
import de.tu_berlin.coga.netflow.ds.flow.MaximumFlow;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public abstract class PushRelabel extends Algorithm<MaximumFlowProblem, MaximumFlow> {
	/** The number of nodes. */
	protected int n;
	/** The number of edges. */
	protected int m;
	/** The source node. */
	protected Node source;
	/** The sink node. */
	protected Node sink;
	/** The excess mapping for nodes. */
	public IdentifiableIntegerMapping<Node> excess;
	/** The distances for nodes. */
	public IdentifiableIntegerMapping<Node> distanceLabels;
	/** The maximum flow value. */
	protected long flowValue;
	/** The number of pushes performed. */
	protected int pushes;
	/** The number of relabels performed by the algorithm. */
	protected int relabels;
	/** */
	protected long initTime;
	protected long phase1Time;
	protected long phase2Time;

	/**
	 * Pushes flow through a certain edge.
	 * @param e the edge
	 * @return the amount of flow that is left over as excess at the start node of {@code e}
	 */
	protected abstract int push( Edge e );

	/**
	 * Relabels a node so that flow can be pushed further.
	 * @param v the node whose label is changed
	 * @return the new distance label of the node
	 */
	protected abstract int relabel( Node v );

	/**
	 * Performs the first phase of a maximum flow computation. Sends as much flow
	 * to the sink as possible. Afterwards, the value of a maximum flow is known,
	 * but the nodes may have a flow excess.
	 */
	protected abstract void computeMaxFlow();

	/**
	 * After the first part of the algorithm, that is computed by {@link #computeMaxFlow()},
	 * the flow is not feasible as several nodes may have a flow excess. This
	 * method reduces the excess by sending the flow back to the source. Thus, the
	 * result is a feasible maximum flow.
	 */
	protected abstract void makeFeasible();

	/**
	 * Returns the flow value computed by the algorithm.
	 * @return the flow value computed by the algorithm
	 */
	public long getFlowValue() {
		return flowValue;
	}

	/**
	 * Returns the number of pushes performed by the algorithm.
	 * @return the number of pushes performed by the algorithm
	 */
	public int getPushes() {
		return pushes;
	}

	/**
	 * Returns the number of relabel operations performed by the algorithm.
	 * @return the number of relabel operations performed by the algorithm
	 */
	public int getRelabels() {
		return relabels;
	}

	public long getInitTime() {
		return initTime;
	}

	
	public long getPhase1Time() {
		return phase1Time;
	}

	public long getPhase2Time() {
		return phase2Time;
	}

	/**
	 * Decides whether a push is applicable to a certain edge. That is, if the node
	 * is active and is admissible at the same time.
	 * @param e the edge which is tested
	 * @return {@code true}, if flow can be pushed on the edge, {@code false} otherwise
	 */
	private boolean pushApplicable( Edge e ) {
		return isActive( e.start() ) && isAdmissible( e );
	}

	/**
	 * Decides whether relabel is applicable to a certain node. That is, if the node
	 * is active (has positive excess) but for none of the outgoing edges push
	 * is applicable.
	 * @param v the node which is tested
	 * @return {@code true}, if the node can be relabeled, {@code false} otherwise
	 */
	protected boolean relabelApplicable( Node v ) {
//		if( !isActive( v ) )
//			return false;
//
//		//ArrayList<Edge> el = incidentEdges.get( v );
//		for( Edge e : el /*residualNetwork.outgoingEdges( v )*/ ) {
//			if( pushApplicable( e ) ) {
//				return false;
//			}
//		}
//
		return true;
	}

	/**
	 * Decides whether an edge is admissible. That is, if it has positive residual
	 * capacity and the label of the start vertex is by one higher than the label
	 * of the end vertex.
	 * @param e the edge which is tested
	 * @return {@code true} if the edge is admissible, {@code false} otherwise.
	 */
	private boolean isAdmissible( Edge e ) {
		return true;
	}

	/**
	 * Decides whether a node is active. That is, if it has positive excess. The
	 * source and the sink cannot be active.
	 * @param v the node which is tested
	 * @return {@code true} if the node is active, {@code false} otherwise
	 */
	protected boolean isActive( Node v ) {
		return /*!v.equals( source ) && */ !v.equals( sink ) && distanceLabels.get( v ) < n && excess.get( v ) > 0;
	}

	public OldResidualNetwork getResidualNetwork() {
		throw new UnsupportedOperationException( "Not supported yet." );
	}
}
