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

import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import java.util.List;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class DefaultResidualNetwork extends DefaultNetwork implements ResidualGraph, ResidualNetwork {
  /** The residual graph. */
  private DefaultResidualGraph residualGraph;
  /** The underlying graph. */
  private DirectedGraph originalGraph;
	/** The flow associated with this residual graph. */
	protected IdentifiableIntegerMapping<Edge> flow;
	/** The residual capacities of this residual graph. */
	protected IdentifiableIntegerMapping<Edge> residualCapacities;

  public DefaultResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    super( getResidualGraph( graph ), capacities, sources, sinks );
    originalGraph = graph;
    init();
  }

  public DefaultResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    super( getResidualGraph( graph ), capacities, source, sinks );
    originalGraph = graph;
    init();
  }

  public DefaultResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    super( getResidualGraph( graph ), capacities, sources, sink );
    originalGraph = graph;
    init();
  }

  public DefaultResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
    super( getResidualGraph( graph ), capacities, source, sink );
    originalGraph = graph;
    init();
  }

  private static DefaultResidualGraph getResidualGraph( DirectedGraph digraph ) {
    return new DefaultResidualGraph( digraph );
  }

  /**
   * Initializes the instance by setting up the residual graph.
   */
  private void init() {
    this.residualGraph = (DefaultResidualGraph)super.getNetwork();

    // initialize flow and stuff
		flow = new IdentifiableIntegerMapping<>( originalGraph.edgeCount() ); // A flow is only valid in the original graph
		residualCapacities = new IdentifiableIntegerMapping<>( residualGraph.edgeCount() );
		for( Edge edge : edges() ) {
			if( isReverseEdge( edge ) ) {
				residualCapacities.set( edge, 0 );
				residualGraph.setHidden( edge, true );
			} else {
				flow.set( edge, 0 );
				residualCapacities.set( edge, getCapacity( edge ) );
			}
    }
  }

	/**
	 * Augments a specified amount of flow along the specified edge. The
   * residual capacities of the edge and its reverse edge are updated
   * automatically. The residual graph is updated as well, if neccessary.
   * Runtime O(1).
	 * @param edge the edge along which flow is to be augmented.
	 * @param amount the amount of flow to augment.
	 */
  @Override
	public void augmentFlow( Edge edge, int amount ) {
		Edge reverseEdge = reverseEdge( edge );
		if( isReverseEdge( edge ) ) {
			flow.decrease( reverseEdge, amount );
			//System.out.println( "FLOW on EDGE " + reverseEdge + " set to " + flow.get( reverseEdge ) );
		} else {
			flow.increase( edge, amount );
			//System.out.println( "FLOW on EDGE " + edge + " set to " + flow.get( edge ) );
		}
		residualCapacities.decrease( edge, amount );
		residualCapacities.increase( reverseEdge, amount );
		if( 0 == residualCapacities.get( edge ) )
			residualGraph.setHidden( edge, true );
		if( 0 < residualCapacities.get( reverseEdge ) )
			residualGraph.setHidden( reverseEdge, false );
	}


  @Override
  public int residualCapacity( Edge edge ) {
    return residualCapacities.get( edge );
  }

  @Override
  public IdentifiableIntegerMapping<Edge> flow() {
    return flow;
  }

  //******************************************************************************************************
  // Delegated methods of the graph of the network such that the network can be accessed as a graph itself

  @Override
  public Edge reverseEdge( Edge edge ) {
    return residualGraph.reverseEdge( edge );
  }

  @Override
  public boolean isReverseEdge( Edge edge ) {
    return residualGraph.isReverseEdge( edge );
  }
}
