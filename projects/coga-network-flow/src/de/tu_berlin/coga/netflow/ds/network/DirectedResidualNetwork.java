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
public class DirectedResidualNetwork extends DirectedNetwork implements ResidualNetwork {
  /** The residual graph. */
  private DefaultResidualGraph residualGraph;
  
  public DirectedResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    super( getResidualGraph( graph, capacities ), capacities, sources, sinks );
    init();
  }

  public DirectedResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    super( getResidualGraph( graph, capacities ), capacities, source, sinks );
    init();
  }

  public DirectedResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    super( getResidualGraph( graph, capacities ), capacities, sources, sink );
    init();
  }

  public DirectedResidualNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
    super( getResidualGraph( graph, capacities ), capacities, source, sink );
    init();
  }

  private static DefaultResidualGraph getResidualGraph( DirectedGraph digraph, IdentifiableIntegerMapping<Edge> capacities ) {
    return new DefaultResidualGraph( digraph, capacities );
  }

  /**
   * Initializes the instance by setting up the residual graph.
   */
  private void init() {
    this.residualGraph = (DefaultResidualGraph)super.getNetwork();

  }

  /****************************************************************************
   * Delegated methods of the graph of the network such that the network can be
   * accessed as a graph itself
   */

  @Override
  public Edge reverseEdge( Edge edge ) {
    return residualGraph.reverseEdge( edge );
  }

  @Override
  public boolean isReverseEdge( Edge edge ) {
    return residualGraph.isReverseEdge( edge );
  }

  
  /****************************************************************************
   * Delegated methods of the residual graph
   */
  
  @Override
  public void augmentFlow( Edge edge, int amount ) {
    residualGraph.augmentFlow( edge, amount );
  }

  @Override
  public int residualCapacity( Edge edge ) {
    return residualGraph.residualCapacity( edge );
  }

  @Override
  public IdentifiableIntegerMapping<Edge> flow() {
    return residualGraph.flow();
  }
}