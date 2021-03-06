/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zetool.netflow.ds.network;

import org.zetool.container.collection.DependingListSequence;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.collection.ListSequence;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A special {@code DefaultDirectedGraph} extending an static network, i.e. a network that
 * cannot be changed, such that it can be modified by adding some sources and
 * edges. Slow implementation!
 * @author Jan-Philipp Kappmeier
 */
public class ExtendedNetwork implements DirectedGraph {
	//private int newNodes;
	//private int newEdges;
	private final int originalEdgeCount;
	private final int originalNodeCount;
  private DirectedGraph graph;
  private ArrayList<Node> newNodes;
  private ArrayList<Edge> newEdges;

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

	public ExtendedNetwork( DirectedGraph graph, int newNodes, int newEdges ) {
		//super( network );
		originalNodeCount = graph.nodeCount();
		//network.setNodeCapacity( graph.getNodeCapacity() + newNodes );
		originalEdgeCount = graph.nodeCount();
		//graph.setEdgeCapacity( graph.getEdgeCapacity() + newEdges );
    for( int i = 0; i < newNodes; ++i ) {
      this.newNodes.add( new Node( originalEdgeCount + i ) );
    }
	}

	public Node getFirstNewNode() {
		return newNodes.get( 0 ); //this.getNode( originalNodeCount );
	}

	public int getFirstNewEdgeIndex() {
		return originalEdgeCount;
	}

	public void undo() {
//		setNodeCapacity( originalNodeCount );
//		setEdgeCapacity( originalEdgeCount );
	}

  @Override
  public IdentifiableCollection<Edge> incomingEdges( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IdentifiableCollection<Edge> outgoingEdges( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IdentifiableCollection<Node> predecessorNodes( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IdentifiableCollection<Node> successorNodes( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int inDegree( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int outDegree( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean isDirected() {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IdentifiableCollection<Edge> edges() {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IdentifiableCollection<Node> nodes() {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int edgeCount() {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int nodeCount() {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IdentifiableCollection<Edge> incidentEdges( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IdentifiableCollection<Node> adjacentNodes( Node node ) {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
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
    return id <= originalNodeCount;
  }
  private boolean isOriginalEdge( int id ) {
    return id <= originalEdgeCount;
  }

  @Override
  public Iterator<Node> iterator() {
    return null;
    //return new IteratorIterator<>( graph.iterator(), newNodes.iterator() );
  }

  // TEmporary:
  public static int getEdgeCapacity() {
    return -1;
  }

  public Edge createAndSetEdge( Node start, Node end ) {
    return null;
  }
}
