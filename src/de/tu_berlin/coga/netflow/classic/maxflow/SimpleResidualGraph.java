/**
 *
 * ResGraph.java
 * Created: 25.02.2011, 18:33:02
 */
package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.graph.OutgoingStarGraph;
import org.zetool.container.collection.ArraySet;
import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.graph.Node;
import org.zetool.container.mapping.IdentifiableBooleanMapping;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.container.mapping.IdentifiableObjectMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import java.util.Iterator;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class SimpleResidualGraph implements DirectedGraph, OutgoingStarGraph {

	public ArraySet<Node> nodes;
	public ArraySet<Edge> edges;

	// new mapping replacing the old stuff
	protected IdentifiableBooleanMapping<Edge> isReverseEdge;	// indicates if a given edge is residual or was contained in the original graph
	public IdentifiableIntegerMapping<Edge> residualCapacity; // gives the residual capacity of a given edge
	public IdentifiableObjectMapping<Edge,Edge> reverseEdge; // gives the residual edge for a given edge

	public IdentifiableObjectMapping<Edge,Edge> originalResidualEdgeMapping;

	/** The index of the first outgoing edge of a node in the edges array. */
	public IdentifiableIntegerMapping<Node> first; // temporarily public for debug reasons
	/** The index of the last outgoing edge of a node in the edges array. */
	public IdentifiableIntegerMapping<Node> last;

	/**
	 * Initializes the graph with a given size
	 * @param n
	 * @param m
	 */
	private int m;

	public SimpleResidualGraph( int n, int m ) {
		edges = new ArraySet<>( Edge.class, 2*m );
		nodes = new ArraySet<>( Node.class, n );
		first = new IdentifiableIntegerMapping<>( n ); // first outgoing edge (index)
		last = new IdentifiableIntegerMapping<>( n ); // last outgoing edge (index)
		isReverseEdge = new IdentifiableBooleanMapping<>( 2*m );
		residualCapacity = new IdentifiableIntegerMapping<>( 2*m );
		reverseEdge = new IdentifiableObjectMapping<>( 2*m );
		originalResidualEdgeMapping = new IdentifiableObjectMapping<>( 2*m );
		this.m = m;
	}

	/**
	 * Sets up the data structures. At first, creates reverse edges and orders the
	 * edges according to their tail nodes into the array. Thus, the incident
	 * edges to a vertex can be searched by a run through the array.
	 *
	 * @param graph
	 * @param capacities
	 * @param current
	 */
	public void init( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Node> current ) {
		// set up residual edges
		int edgeCounter = 0;
		int[] temp = new int[edges.getCapacity()];
		for( Node v : graph ) {
			first.set( v, edgeCounter );
			current.set( v, edgeCounter );
			// add the outgoing edges to the arc list
			for( Edge e : graph.outgoingEdges( v ) ) {
				//residualEdges[edgeCounter] = new ResidualEdge( edgeCounter, e.start(), e.end(), getProblem().getCapacities().get( e ), false );
				Edge ne = new Edge(edgeCounter, e.start(), e.end() );
				originalResidualEdgeMapping.set( ne, e );
				edges.add( ne );
				residualCapacity.add( ne, capacities.get( e ) );
				isReverseEdge.add( ne, false );
				//reverseEdge.set( e, e )
				//residualEdges[edgeCounter].original = e;
				temp[e.id()] = edgeCounter++;
			}
			// add the reverse edge for incoming edges to the arc list (they are also outgoing!
			for( Edge e : graph.incomingEdges( v ) ) {
				//residualEdges[edgeCounter] = new ResidualEdge( edgeCounter, e.end(), e.start(), 0, true );
				Edge ne = new Edge( edgeCounter, e.end(), e.start() );

				edges.add( ne );
				residualCapacity.add( ne, 0 );
				isReverseEdge.add( ne, true );

				temp[e.id() + m] = edgeCounter++;
			}
			last.set( v, edgeCounter );
		}
		for( int i = 0; i < m; ++i ) {
			//residualEdges[temp[i]].reverse = residualEdges[temp[i + m]];
			reverseEdge.set( edges.get( temp[i] ), edges.get( temp[i+m]));
			//residualEdges[temp[i + m]].reverse = residualEdges[temp[i]];
			reverseEdge.set( edges.get( temp[i+m]), edges.get(temp[i]));
		}

//		// initialize excesses
//		excess.set( source, 0 );
//		for( int i = first.get( source ); i < last.get( source ); ++i ) {
//			ResidualEdge e = residualEdges[i];
//			if( e.end().id() != source.id() ) {
//				pushes++;
//				final int delta = e.residualCapacity;
//				e.residualCapacity -= delta;
//				e.reverse.residualCapacity += delta;
//				excess.increase( e.end(), delta );
//			}
//		}

//		for( Node v : getProblem().getNetwork() ) {
//			final int id = v.id();
//			if( id == sink.id() ) {
//				distanceLabels.set( v, 0 );
//				inactiveBuckets.addInactive( 0, v );
//				continue;
//			}
//			distanceLabels.set( v, id == source.id() ? n : 1 );
//			if( excess.get( v ) > 0 )
//				activeBuckets.addActive( 1, v );
//			else if( distanceLabels.get( v ) < n )
//				inactiveBuckets.addInactive( 1, v );
//		}
//		activeBuckets.setdMax( 1 );
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public IdentifiableCollection<Edge> edges() {
		return edges;
	}

	@Override
	public IdentifiableCollection<Node> nodes() {
		return nodes;
	}

	@Override
	public int edgeCount() {
		return edges.size();
	}

	@Override
	public int nodeCount() {
		return nodes.size();
	}

	@Override
	public IdentifiableCollection<Edge> incidentEdges( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public IdentifiableCollection<Edge> incomingEdges( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public IdentifiableCollection<Edge> outgoingEdges( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public IdentifiableCollection<Node> adjacentNodes( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public IdentifiableCollection<Node> predecessorNodes( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public IdentifiableCollection<Node> successorNodes( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public int degree( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public int inDegree( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public int outDegree( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public boolean contains( Edge edge ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public boolean contains( Node node ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Edge getEdge( int id ) {
		return edges.get( id );
	}

	@Override
	public Edge getEdge( Node start, Node end ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public IdentifiableCollection<Edge> getEdges( Node start, Node end ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Node getNode( int id ) {
		return nodes.get( id );
	}

	public int getResidualCapacity( int i ) {
		return residualCapacity.get( edges.get( i ) );
	}

	public int getResidualCapacity( Edge e ) {
		return residualCapacity.get( e );
	}

	public boolean isReverseEdge( Edge a ) {
		 return isReverseEdge.get( a );
	}

	public int getReverseResidualCapacity( Edge e ) {
		return residualCapacity.get( reverseEdge.get( e ) );
	}

	public int getReverseResidualCapacity( int i ) {
		return residualCapacity.get( reverseEdge.get( edges.get( i ) ) );
	}

	public void augment( Edge a, int delta ) {
		//System.out.println( "Augmenting " + a + " by " + delta );
		residualCapacity.decrease( a, delta );
		residualCapacity.increase( reverseEdge.get( a ), delta );
	}

	int augmentMax( Edge e, int get ) {
		final int delta = residualCapacity.get( e ) < get ? residualCapacity.get( e ) : get;
		residualCapacity.decrease( e, delta );
		residualCapacity.increase( reverseEdge.get( e ), delta );
		return delta;
	}

	public Edge getReverseEdge( Edge e ) {
		return reverseEdge.get( e );
	}

	public int getFirst( Node node ) {
		return first.get( node );
	}

	public int getLast( Node node ) {
		return last.get( node );
	}

	@Override
	public int next( int current ) {
		return current+1;
	}

	@Override
	public Iterator<Node> iterator() {
		return nodes.iterator();
	}

}
