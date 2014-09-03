
package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.container.collection.ShiftedArraySet;
import de.tu_berlin.coga.container.collection.CombinedCollection;
import de.tu_berlin.coga.container.collection.ArraySet;
import de.tu_berlin.coga.container.collection.DependingListSequence;
import de.tu_berlin.coga.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.container.collection.ListSequence;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.IteratorIterator;
import de.tu_berlin.coga.graph.Node;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A special {@code DefaultDirectedGraph} extending an static network, i.e. a network that
 * cannot be changed, such that it can be modified by adding some sources and
 * edges. Slow implementation!
 * @author Jan-Philipp Kappmeier
 */
public class ExtendedGraph implements DirectedGraph {
	private final int originalEdgeCount;
	private final int originalNodeCount;
  private final DirectedGraph graph;
  private final ArraySet<Node> newNodes;
  private final ArraySet<Edge> newEdges;

  // Node information
	/** Caches the edges incident to a node for all nodes in the graph. * Must not be null. */
	protected HashMap<Node, DependingListSequence<Edge>> incidentEdges;
	/** Caches the edges ending at a node for all nodes in the graph. Must not be null. */
	protected HashMap<Node, DependingListSequence<Edge>> incomingEdges;
	/** Caches the edges starting at a node for all nodes in the graph. Must not be null. */
	protected HashMap<Node, DependingListSequence<Edge>> outgoingEdges;
	/** Caches the number of edges incident to a node for all nodes in the graph. Must not be null. */
	protected HashMap<Node,Integer> degree;
	/** Caches the number of edges ending at a node for all nodes in the graph. Must not be null. */
	protected HashMap<Node,Integer> indegree;
	/** Caches the number of edges starting at a node for all nodes in the graph. Must not be null. */
	protected HashMap<Node,Integer> outdegree;

	public ExtendedGraph( DirectedGraph graph, int newNodes, int newEdges ) {
		originalNodeCount = graph.nodeCount();
		originalEdgeCount = graph.edgeCount();
    this.graph = graph;
    this.newNodes = new ShiftedArraySet<>( Node.class, newNodes, originalNodeCount );
    for( int i = 0; i < newNodes; ++i ) {
      this.newNodes.add( new Node( originalNodeCount + i ) );
    }
    this.newEdges = new ShiftedArraySet<>( Edge.class, newEdges, originalEdgeCount );
	}

	public Node getFirstNewNode() {
		return newNodes.get( 0 );
	}

	public int getFirstNewEdgeIndex() {
		return originalEdgeCount;
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
  public IdentifiableCollection<Node> predecessorNodes( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  @Override
  public IdentifiableCollection<Node> successorNodes( Node node ) {
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
  public boolean isDirected() {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  @Override
  public IdentifiableCollection<Edge> edges() {
    return new CombinedCollection<>( graph.edges(), this.newEdges );
  }

  @Override
  public IdentifiableCollection<Node> nodes() {
    return new CombinedCollection<>( graph.nodes(), this.newNodes );
  }

  @Override
  public int edgeCount() {
    return graph.edgeCount() + newEdges.size();
  }

  @Override
  public int nodeCount() {
    return graph.nodeCount() + newNodes.size();
  }

  @Override
  public IdentifiableCollection<Edge> incidentEdges( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  @Override
  public IdentifiableCollection<Node> adjacentNodes( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  @Override
  public int degree( Node node ) {
    int degree = 0;
    if( isOriginalNode( node.id() ) )
      degree += graph.degree( node );
    if( this.degree.containsKey( node ) )
      degree+= this.degree.get( node );
    return degree;
  }

  @Override
  public boolean contains( Edge edge ) {
    return graph.contains( edge ) || newEdges.contains( edge );
  }

  @Override
  public boolean contains( Node node ) {
    return graph.contains( node ) || newNodes.contains( node );
  }

  @Override
  public Edge getEdge( int id ) {
    if( isOriginalEdge( id ) )
      return graph.getEdge( id );
    else
      return newEdges.get( id - originalEdgeCount );
  }

  @Override
  public Edge getEdge( Node start, Node end ) {
    Edge e = null;
    if( isOriginalNode( start.id() ) && isOriginalNode( end.id() ) ) {
      e = graph.getEdge( start, end );
    }
    if( e != null )
      return e;
    if( outgoingEdges.containsKey( start ) ) {
      for( Edge edge : outgoingEdges.get( start ) )
        if( edge.end().equals( end ) )
          return e;
    }
    return null;
  }

  @Override
  public IdentifiableCollection<Edge> getEdges( Node start, Node end ) {
    IdentifiableCollection<Edge> result = new ListSequence<>() ;
    if( isOriginalNode( start.id() ) && isOriginalNode( end.id() ) ) {
      result = graph.getEdges( start, end );
    }
    if( outgoingEdges.containsKey( start ) ) {
      for( Edge edge : outgoingEdges.get( start ) )
        if( edge.end().equals( end ) )
          result.add( edge );
    }
		return result;
  }

  @Override
  public Node getNode( int id ) {
    if( isOriginalNode( id ) )
      return graph.getNode( id );
    else
      return newNodes.get( id - originalNodeCount );
  }

  private boolean isOriginalNode( int id ) {
    return id < originalNodeCount;
  }
  private boolean isOriginalEdge( int id ) {
    return id < originalEdgeCount;
  }

  @Override
  public Iterator<Node> iterator() {
    return new IteratorIterator<>( graph.iterator(), newNodes.iterator() );
  }

  // TEmporary:
  public static int getEdgeCapacity() {
    return -1;
  }

  public Edge createAndSetEdge( Node start, Node end ) {
    if( newEdges.size() >= newEdges.getCapacity() ) {
      throw new IllegalArgumentException( "Cannot add more edges to extended graph!" );
    }
    Edge edge = new Edge( originalEdgeCount + newEdges.size(), start, end );
    newEdges.add( edge );
    return edge;
  }

  @Override
  public String toString() {
    return DirectedGraph.stringRepresentation( this );
  }
}
