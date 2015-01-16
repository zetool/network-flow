
package org.zetool.netflow.ds.network;

import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.container.mapping.IdentifiableObjectMapping;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.SimpleUndirectedGraph;
import org.zetool.graph.UndirectedGraph;
import org.zetool.graph.structure.Path;
import java.util.Iterator;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class UndirectedResidualGraph extends SimpleUndirectedGraph implements ResidualGraph{
  private enum Direction {
    Neutral,
    MinToMax,
    MaxToMin
  }
  private final int originalNumberOfEdges;
  private final UndirectedGraph graph;
	/** The flow associated with this residual graph. */
	protected IdentifiableIntegerMapping<Edge> flow;
	/** The residual capacities of this residual graph. */
	protected IdentifiableIntegerMapping<Edge> residualCapacities;
  
  private IdentifiableObjectMapping<Edge,Direction> direction;
  
  private final IdentifiableIntegerMapping<Edge> capacities;

  public UndirectedResidualGraph( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities  ) {
    super( graph );
    this.originalNumberOfEdges = graph.edgeCount();
    this.graph = graph;
    this.capacities = capacities;

    setUp();
  }

  /**
   * Initializes the reverse arcs copies of the graph.
   */
  private void setUp() {
    // create original edges
    //setNodes( graph.nodes() ); // already done in constructor

    int maxID = -1;
    for( Edge edge : graph.edges() ) {
      addEdge( edge );
      maxID = Math.max( edge.id(), maxID );
    }
    if( maxID >= originalNumberOfEdges ) {
      throw new IllegalArgumentException( "Default residual Graph only works with graphs enumerated 1, ..., m for the edges." );
    }

    // copy reverse edges
    for( Edge edge : graph.edges() ) {
      addEdge( edge.end(), edge.start() );
    }

    // initialize flow and stuff
		flow = new IdentifiableIntegerMapping<>( graph.edgeCount() ); // A flow is only valid in the original graph
    direction = new IdentifiableObjectMapping<>( graph.edgeCount() );
		residualCapacities = new IdentifiableIntegerMapping<>( edgeCount() );
		for( Edge edge : edges() ) {
      direction.set( edge, Direction.Neutral );
			if( isReverseEdge( edge ) ) {
				residualCapacities.set( edge, 0 );
			} else {
				flow.set( edge, 0 );
				residualCapacities.set( edge, capacities.get( edge ) );
			}
    }
  
  }
  
  @Override
  public Edge reverseEdge( Edge edge ) {
    if( edge.id() < originalNumberOfEdges ) {
      return edges.get( edge.id() + originalNumberOfEdges );
    } else {
      return edges.get( edge.id() - originalNumberOfEdges );
    }
  }

  @Override
  public boolean isReverseEdge( Edge edge ) {
    return edge.id() >= originalNumberOfEdges;
  }

  @Override
  public void augmentFlow( Edge edge, int amount ) {
		Edge reverseEdge = reverseEdge( edge );
		if( isReverseEdge( edge ) ) {
			flow.decrease( reverseEdge, amount );
		} else {
			flow.increase( edge, amount );
		}
		residualCapacities.decrease( edge, amount );
		residualCapacities.increase( reverseEdge, amount );
		//if( 0 == residualCapacities.get( edge ) ) {
		//	setHidden( edge, true );
    //}
		//if( 0 < residualCapacities.get( reverseEdge ) ) {
		//	setHidden( reverseEdge, false );
    //}
  }

  @Override
  public void augmentFlow( Path path, int amount ) {
    Node lastStart = path.start();
    
    for( Edge e : path ) {
      augmentFlow( e, amount );
      
      // set the direction
      // direction from lastStart to e.oppositeNode( lastStart )
      Node currentEnd = e.opposite( lastStart );
      
      if( flow.get( e ) == 0 ) {
        direction.set( e, Direction.Neutral );
        direction.set( reverseEdge( e ), Direction.Neutral );
        System.out.println( "Setting edge " + e + " back to neutral." );
      } else {        
        if( lastStart.id() < currentEnd.id() ) {
          direction.set( e, Direction.MinToMax );
          direction.set( reverseEdge( e ), Direction.MaxToMin );
          System.out.println( "Setting edge " + e + " to forward." );
        } else if( lastStart.id() > currentEnd.id() ) {
          direction.set( e, Direction.MaxToMin );
          direction.set( reverseEdge( e ), Direction.MaxToMin );
          System.out.println( "Setting edge " + e + " to backward." );
        } else {
          throw new AssertionError( "Node ids cannot be equal" );
        }
      }
      lastStart = currentEnd;
    }
  }

  @Override
  public int residualCapacity( Edge edge ) {
    return residualCapacities.get( edge );
  }

  @Override
  public IdentifiableIntegerMapping<Edge> flow() {
    return flow;
  }
  
  /**
   * Override iterators such that edges with no capacity are skipped
   */
  @Override
  public IdentifiableCollection<Edge> incidentEdges( Node node ) {
    //return incidentEdges.get( node );
    return new SkipSequence( super.incidentEdges( node ), node );
  }

  
  
  
  private class SkipSequence implements IdentifiableCollection<Edge> {

    private final IdentifiableCollection<Edge> baseCollection;
    private final Node node;

    public SkipSequence( IdentifiableCollection<Edge> baseCollection, Node node ) {
      this.baseCollection = baseCollection;
      this.node = node;
    }
    
    @Override
    public Iterator<Edge> iterator() {
      return new SkipIterator( baseCollection, flow, node );
    }

  /****************************************************************************
   * Delegated all other methods.
   */

    @Override
    public boolean add( Edge element ) {
      return baseCollection.add( element );
    }

    @Override
    public void remove( Edge element ) {
      baseCollection.remove( element );
    }

    @Override
    public Edge removeLast() {
      return baseCollection.removeLast();
    }

    @Override
    public boolean contains( Edge element ) {
      return baseCollection.contains( element );
    }

    @Override
    public boolean empty() {
      return baseCollection.empty();
    }

    @Override
    public int size() {
      return baseCollection.size();
    }

    @Override
    public Edge get( int id ) {
      return baseCollection.get( id );
    }

    @Override
    public Edge first() {
      return baseCollection.first();
    }

    @Override
    public Edge last() {
      return baseCollection.last();
    }

    @Override
    public Edge predecessor( Edge element ) {
      return baseCollection.predecessor( element );
    }

    @Override
    public Edge successor( Edge element ) {
      return baseCollection.successor( element );
    }
  }
  
  
  private class SkipIterator implements Iterator<Edge> {
    private IdentifiableCollection<Edge> collection;
    private IdentifiableIntegerMapping<Edge> myFlow;
    private Iterator<Edge> iterator;
    private Edge next;
    private Node node;
    
    private SkipIterator( IdentifiableCollection<Edge> baseCollection, IdentifiableIntegerMapping<Edge> flow_a, Node node ) {
      this.collection = baseCollection;
      this.myFlow = flow_a;
      iterator = baseCollection.iterator();
      this.node = node;
      iterate();
    }
    
    private void iterate() {
      while( iterator.hasNext() ) {
        next = iterator.next();
        if( residualCapacities.get( next ) > 0 ) {
          // Only take arcs if direction = neutral
          // or, if MaxToMin and start > end
          // or, if MinToMax and start < end
          if( (direction.get( next ) == Direction.Neutral) 
                  || ( direction.get( next ) == Direction.MaxToMin && node.id() > next.opposite( node ).id() )
                  || ( direction.get( next ) == Direction.MinToMax && node.id() < next.opposite( node ).id() ) ) {
            return;
          }
          System.out.println( "Skip edge " + next + " with residual capacity " + residualCapacities.get( next ) + " and direction " + direction.get( next ) );
        } else {
          System.out.println( "Skip edge " + next + " with residual capacity " + residualCapacities.get( next ) );
        }
      }
      next = null;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Edge next() {
      Edge ret = next;
      iterate();
      return ret;
    }
    
  }
  
}
