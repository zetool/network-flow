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

import de.tu_berlin.coga.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import java.util.List;

/**
 * An implementation of the {@link de.tu_berlin.coga.ds.network.Network} interface based on the
 * {@link de.tu_berlin.coga.graph.DefaultDirectedGraph}.
 * @author Jan-Philipp Kappmeier
 */
public class DirectedNetwork extends GeneralNetwork implements DirectedGraph {
  private DirectedGraph graph;

  // TODO: replace constructors and change to a builder that maybe also automatically builds the network!

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
    return (DirectedGraph)super.getGraph(); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public DirectedGraph getNetwork() {
    return (DirectedGraph)super.getNetwork(); //To change body of generated methods, choose Tools | Templates.
  }

  
  
  //******************************************************************************************************
  // Delegated methods of the graph of the network such that the network can be accessed as a graph itself

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
}
