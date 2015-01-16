
package org.zetool.netflow.dynamic;

import org.zetool.common.algorithm.Algorithm;
import org.zetool.container.priority.MinHeap;
import org.zetool.graph.Edge;
import org.zetool.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.EdgeBasedFlowOverTime;
import org.zetool.netflow.ds.structure.FlowOverTimeEdge;
import org.zetool.netflow.ds.structure.FlowOverTimePath;
import org.zetool.netflow.ds.flow.PathBasedFlowOverTime;
import org.zetool.container.mapping.Identifiable;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.container.mapping.IdentifiableObjectMapping;
import org.zetool.container.mapping.TimeIntegerMapping;
import org.zetool.container.mapping.TimeObjectMapping;

/**
 * This class decomposes a flow over time given by an
 * {@code ImplicitTimeExpandedResidualNetwork} and decomposes it into a
 * {@code PathBasedFlowOverTime}.
 *
 * @author Martin Groß
 */
public class FlowOverTimePathDecomposition extends Algorithm<ImplicitTimeExpandedResidualNetwork,
				PathBasedFlowOverTime> {

	/**
	 * Stores the flow this algorithm works on. Only used internally during the
	 * computation.
	 */
	private transient EdgeBasedFlowOverTime flow;
	/**
	 * The input for algorithm. Only used internally during the computation.
	 */
	private transient ImplicitTimeExpandedResidualNetwork network;
	/**
	 * Stores the flow this algorithm works on. Only used internally during the
	 * computation.
	 */
	private transient IdentifiableIntegerMapping<Edge> superSourceFlow;
	/**
	 * Stores the flow this algorithm works on. Only used internally during the
	 * computation.
	 */
	private transient IdentifiableObjectMapping<Node, TimeIntegerMapping> waitingFlow;

	@Override
	protected PathBasedFlowOverTime runAlgorithm( ImplicitTimeExpandedResidualNetwork network ) {
		// Store the reference to the network for other methods
		this.network = network;
		// Clone the input because we are going to modify it
		flow = new EdgeBasedFlowOverTime( network.flow().clone() );
		superSourceFlow = network.superSourceFlow().clone();
		waitingFlow = new IdentifiableObjectMapping<>( network.waitingFlow().clone() );

		// Initialize the data structure for the result
		PathBasedFlowOverTime result = new PathBasedFlowOverTime();
		// Iteratively extract source-sink paths from the flow
		FlowOverTimePath path;
		do {
			// Find a source-sink-path
			IdentifiableIntegerMapping<Node> arrivalTimes = new IdentifiableIntegerMapping<>( network.nodeCount() );
			IdentifiableIntegerObjectMapping<Node> preceedingEdges = new IdentifiableIntegerObjectMapping<>( network.nodeCount() );
			initialize( preceedingEdges, arrivalTimes );
			// Trace back a path from the sink to the super source
			path = constructPath( preceedingEdges, arrivalTimes.get( network.getProblem().getSink() ) );
			// If a path has been found...
			//System.out.println("Supersource: " + superSourceFlow);
			//System.out.println("Flow: " + flow);
			//System.out.println(preceedingEdges);
			//System.out.println(arrivalTimes);
			if( path != null ) {
				// Subtract as much flow as possible from the path
				subtractPath( path );
				// The first edge might be artifical and should be removed for the
				// path decomposition, if it is.
				if( network.hasArtificialSuperSource() ) {
					path.removeFirst();
				}
				result.addPathFlow( path );
			}
		} while( path != null );
		// Return the path flow found
		return result;
	}

	/**
	 * Computes a shortest super-source to sink path and stores the preceeding
	 * edges and arrival times in the supplied data structures.
	 * @param preceedingEdges data structure to store the results.
	 * @param arrivalTimes data structure to store the results.
	 */
	private void initialize( IdentifiableIntegerObjectMapping<Node> preceedingEdges, IdentifiableIntegerMapping<Node> arrivalTimes ) {
		MinHeap<Node, Integer> queue = new MinHeap<>( network.nodeCount() );
		for( int v = 0; v < network.nodeCount(); v++ ) {
			queue.insert( network.getNode( v ), Integer.MAX_VALUE );
		}
		// Start at the super source
		arrivalTimes.set( network.superSource(), 0 );
		queue.decreasePriority( network.superSource(), 0 );
		// Process the next node
		while( !queue.isEmpty() ) {
			// Extract the nearest unprocessed node
			MinHeap<Node, Integer>.Element min = queue.extractMin();
			Node node = min.getObject();
			Integer distance = min.getPriority();
			arrivalTimes.set( node, distance );
			if( distance == Integer.MAX_VALUE ) {
				continue;
			}
			// Iterate over its outgoing edges
			for( Edge edge : network.outgoingEdges( node ) ) {
				// We are only interested in flow carrying edges
				if( network.isReverseEdge( edge ) ) {
					continue;
				}
				// We can only use edges with flow, so have to determine the
				// next point in time after our arrival at the node where flow
				// is using the edge.
				int time;
				if( node == network.superSource() && network.hasArtificialSuperSource() ) {
					time = (superSourceFlow.get( edge ) > 0) ? 0 : Integer.MAX_VALUE;
				} else {
					time = flow.get( edge ).nextPositiveValue( distance );
				}
				// If there is no time where flow is using this edge, we skip it
				if( time == Integer.MAX_VALUE ) {
					continue;
				}
				// If need to wait to able to use this edge, there must be flow
				// waiting in this edge.
				int minimumALT = distance == time ? Integer.MAX_VALUE : waitingFlow.get( node ).minimum( distance, time );
				if( time > distance && minimumALT == 0  && !isSource( node ) ) {
					continue;
				}
				// We update the distances in the queue and the bookkeeping.
				Node w = edge.opposite( node );
				if( queue.contains( w ) && (long)queue.priority( w ) > (long)time + (long)network.transitTime( edge ) ) {
					queue.decreasePriority( w, time + network.transitTime( edge ) );
					preceedingEdges.get( w ).set( time + network.transitTime( edge ), new FlowOverTimeEdge( edge, time - distance, time ) );
				}
			}
		}
	}

	protected boolean isSource( Node node ) {
		return (network.superSource().equals( node ));
	}

	/**
	 * Traces back a flow over time path from super source to sink and returns it.
	 *
	 * @param preceedingEdges an in-forest specified by entering edges.
	 * @param arrivalTime
	 * @return a flow over time path from super source to sink.
	 */
	protected FlowOverTimePath constructPath( IdentifiableIntegerObjectMapping<Node> preceedingEdges, int arrivalTime ) {
		// We start with an empty path
		FlowOverTimePath path = new FlowOverTimePath();
		// Trace the path back from the sink
		Node node = network.getProblem().getSink();
		int time = arrivalTime;
		// As long as we haven't reached the super source...
		while( node != network.superSource() ) {
			FlowOverTimeEdge edge = preceedingEdges.get( node ).get( time );
			// If the preceeding edge is null, there is no super source -> sink path
			if( edge == null ) {
				return null;
			}
			// Otherwise, we move to the start of this edge and continue
			path.addFirst( edge );
			time = edge.getTime() - edge.getDelay();
			node = edge.getEdge().opposite( node );
		}
		return path;
	}

	/**
	 * Determines the bottleneck capacity of the specified path and subtracts flow
	 * from this path equal to the bottleneck capacity.
	 *
	 * @param path the path from which flow is subtracted.
	 */
	protected void subtractPath( FlowOverTimePath path ) {
		// Determine the bottleneck capacity
		int capacity = Integer.MAX_VALUE;
		boolean first = true;
		for( FlowOverTimeEdge edge : path ) {
			if( !first ) {
				final int minimum = edge.getTime() - edge.getDelay() == edge.getTime()
								? Integer.MAX_VALUE
								: waitingFlow.get( edge.getEdge().start() ).minimum( edge.getTime() - edge.getDelay(), edge.getTime() );
				capacity = Math.min( minimum, capacity );
			} else {
				first = false;
			}
			if( edge.getEdge().start() == network.superSource() && network.hasArtificialSuperSource() ) {
				capacity = Math.min( superSourceFlow.get( edge.getEdge() ), capacity );
			} else if( edge.getEdge().isLoop() ) {
			} else {
				capacity = Math.min( flow.get( edge.getEdge() ).get( edge.getTime() ), capacity );
			}
		}
		// Subtract the bottleneck flow value from the path
		first = true;
		for( FlowOverTimeEdge edge : path ) {
			if( !first ) {
				//waitingFlow.decrease( edge.getEdge().start(), edge.getTime() - edge.getDelay(), edge.getTime(), capacity );

				if( edge.getTime() - edge.getDelay() != edge.getTime() )
					waitingFlow.get( edge.getEdge().start() ).decrease( edge.getTime() - edge.getDelay(), edge.getTime(), capacity);

				//if( waitingFlow.get( edge.getEdge().start() ) != waitingFlow.get( edge.getEdge().start() ).get( capacity )  )

			} else {
				first = false;
			}
			if( edge.getEdge().start() == network.superSource() && network.hasArtificialSuperSource() ) {
				superSourceFlow.decrease( edge.getEdge(), capacity );
			} else if( edge.getEdge().isLoop() ) {
			} else {
				flow.get( edge.getEdge() ).decrease( edge.getTime(), capacity );
			}
		}
		// Set the rate and amount of the path
		path.setAmount( capacity );
		path.setRate( capacity );
	}

	/**
	 * Private utility class needed for generating generic instances of {@link IdentifiableObjectMapping}.
	 */
	private static class TimeEdgeClass extends TimeObjectMapping<FlowOverTimeEdge> {}

	private static class IdentifiableIntegerObjectMapping<D extends Identifiable> extends IdentifiableObjectMapping<D, TimeEdgeClass> {

		private IdentifiableIntegerObjectMapping( int domainSize ) {
			super( domainSize );
			for( int i = 0; i < domainSize; ++i ) {
				mapping[i] = new TimeEdgeClass();
			}
		}
	}

}
