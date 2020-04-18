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

import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import java.util.List;

/**
 * An implementation of the {@link Network} interface based on the
 * {@link GeneralNetwork}.
 * @author Jan-Philipp Kappmeier
 */
public class DirectedNetwork extends GeneralNetwork implements DirectedGraph {
  private final DirectedGraph graph;

  public DirectedNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    super( graph, capacities, sources, sinks );
    this.graph = graph;
  }

  public DirectedNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    super( graph, capacities, source, sinks );
    this.graph = graph;
  }

  public DirectedNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    super( graph, capacities, sources, sink );
    this.graph = graph;
  }

  public DirectedNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
    super( graph, capacities, source, sink );
    this.graph = graph;
  }

  @Override
  public DirectedGraph getGraph() {
    return (DirectedGraph)super.getGraph();
  }

  /****************************************************************************************************************
   * Delegated methods of the directed graph of the network such that the network can be accessed as a graph itself
   */

  @Override
  public IdentifiableCollection<Edge> incomingEdges( Node node ) {
    return graph.incomingEdges( node );
  }

  @Override
  public IdentifiableCollection<Edge> outgoingEdges( Node node ) {
    return graph.outgoingEdges( node );
  }

  @Override
  public IdentifiableCollection<Node> predecessorNodes( Node node ) {
    return graph.predecessorNodes( node );
  }

  @Override
  public IdentifiableCollection<Node> successorNodes( Node node ) {
    return graph.successorNodes( node );
  }

  @Override
  public int inDegree( Node node ) {
    return graph.inDegree( node );
  }

  @Override
  public int outDegree( Node node ) {
    return graph.outDegree( node );
  }

  /**
   * Returns an extended network with a single source and single sink for a
   * given graph.
   * @param graph
   * @param capacities
   * @param sources
   * @param sinks
   * @return an extended network with a single source and single sink for a given graph
   */
  public static Network getExtendedNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities,
          List<Node> sources, List<Node> sinks ) {
    final ExtendedGraph extended = new ExtendedGraph( graph, 2, sources.size() + sinks.size() );

    final Node newSource = extended.getFirstNewNode();
    final Node newSink = extended.getNode( newSource.id() + 1 );

    final IdentifiableIntegerMapping<Edge> newCapacities
            = new IdentifiableIntegerMapping<>( capacities, extended.edgeCount() );
    sources.stream().forEach( s -> newCapacities.set( extended.createAndSetEdge( newSource, s ), Integer.MAX_VALUE ) );
    sinks.stream().forEach( t -> newCapacities.set( extended.createAndSetEdge( t, newSink ), Integer.MAX_VALUE ) );

    return new DirectedNetwork( extended, newCapacities, newSource, newSink );
  }
}
