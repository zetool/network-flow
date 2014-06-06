/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.graph.DefaultGraph;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Edge;

/**
 * A residual graph is a graph that has the special property, that for each edge there is also parallel edge in reversed
 * order that is called "reverse edge" of the original edge.
 *
 * The original network is not changed. If the original graph contains hidden edges and/or the edges are not enumerated
 * 1, ..., n this creates problems.
 * @author Jan-Philipp Kappmeier
 */
class DefaultResidualGraph extends DefaultGraph implements ResidualGraph {
  // TODO: maybe clone method for default graph that generates copy with larger capacity
  private final int maxNodeId;
  private final int originalNumberOfEdges;
  private final DirectedGraph graph;

  public DefaultResidualGraph( DirectedGraph graph ) {
    super( graph.nodeCount(), graph.edgeCount() * 2 );
    this.maxNodeId = graph.nodeCount();
    this.originalNumberOfEdges = graph.edgeCount();
    this.graph = graph;

    setUp();

//		flow = new IdentifiableDoubleMapping<>( graph.edgeCount() );
//TODO move to network
    //residualCapacities = new IdentifiableDoubleMapping<>( graph.edgeCount() * 2 );
//    for( Edge edge : edges ) {

//			if( isReverseEdge( edge ) ) {
//				residualCapacities.set( edge, 0 );
//				changeVisibility( edge, false );
//
//			} else {
//				changeVisibility( edge, true );
//				flow.set( edge, 0.0 );
//				residualCapacities.set( edge, capacities.get( edge ) );
//    }
//		}
  }

  /**
   * Initializes the reverse arcs copies of the graph.
   */
  private void setUp() {
    // create original edges
    setNodes( graph.nodes() );
    //setEdges( graph.edges() );

    int maxID = -1;
    for( Edge edge : graph.edges() ) {
      //createEdge( edge.end(), edge.start(), edge.id() + (maxEdgeId + 1) );
      setEdge( edge );
      maxID = Math.max( edge.id(), maxID );
    }
    if( maxID >= originalNumberOfEdges ) {
      throw new IllegalArgumentException( "Default residual Graph only works with graphs enumerated 1, ..., m for the edges." );
    }

    // copy reverse edges
    for( Edge edge : graph.edges() ) {
      createAndSetEdge( edge.end(), edge.start() );
    }
  }

  /**
   * Returns the reverse edge of the specified edge. Runtime O(1). The reverse edge of an reverse edge is
   * the original edge again.
   * @param edge the edge for which the reverse edge is to be returned.
   * @return the reverse edge of the specified edge.
   */
  public Edge reverseEdge( Edge edge ) {
    if( edge.id() < originalNumberOfEdges ) {
      return edges.getEvenIfHidden( edge.id() + originalNumberOfEdges );
    } else {
      return edges.getEvenIfHidden( edge.id() - originalNumberOfEdges );
    }
  }

  /**
   * Checks is whether the specified edge is a reverse edge. An edge is called reverse if it does not exist in the
   * original graph. Runtime O(1).
   * @param edge the edge to be tested.
   * @return {@code true} if the specified edge is a reverse edge, {@code false} otherwise.
   */
  public boolean isReverseEdge( Edge edge ) {
    return edge.id() >= originalNumberOfEdges;
  }
}
