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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of the {@link de.tu_berlin.coga.ds.network.Network} interface based on the
 * {@link de.tu_berlin.coga.graph.DefaultDirectedGraph}.
 * @author Jan-Philipp Kappmeier
 */
public class DefaultNetwork implements Network {
  private DirectedGraph graph;
  private IdentifiableIntegerMapping<Edge> capacities;
  private List<Node> sources;
  private List<Node> sinks;

  // TODO: replace constructors and change to a builder that maybe also automatically builds the network!

  public DefaultNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    this.graph = graph;
    this.capacities = capacities;
    this.sources = sources;
    this.sinks = sinks;
  }

  public DefaultNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    this( graph, capacities, new LinkedList<>(), sinks );
    sources.add( source );
  }

  public DefaultNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    this( graph, capacities, sources, new LinkedList<>() );
    sinks.add( sink );
  }

  public DefaultNetwork( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
    this( graph, capacities, new LinkedList<>(), new LinkedList<>() );
    sources.add( source );
    sinks.add( sink );
  }

  @Override
  public IdentifiableIntegerMapping<Edge> getCapacities() {
    return capacities;
  }

  public DirectedGraph getNetwork() {
    return graph;
  }

  public Node getSink() {
    if( sinks.size() == 1 ) {
      return sinks.get( 0 );
    }
    throw new IllegalStateException( "There are multiple sinks." );
  }

  public List<Node> getSinks() {
    return sinks;
  }

  public Node getSource() {
    if( isSingleSource() ) {
      return sources.get( 0 );
    }
    throw new IllegalStateException( "There are multiple sources." );
  }

  public List<Node> getSources() {
    return sources;
  }

  public final boolean isSingleSource() {
    return sourceCount() == 1;
  }

  public final boolean isSingleSink() {
    return sinkCount() == 1;
  }

  @Override
  public Collection<Node> sinks() {
    return sinks;
  }

  @Override
  public Collection<Node> sources() {
    return sources;
  }

  @Override
  public final int sinkCount() {
    return sinks.size();
  }

  @Override
  public final int sourceCount() {
    return sources.size();
  }

  @Override
  public int getCapacity( Edge e ) {
    return capacities.get( e );
  }

  @Override
  public DirectedGraph getGraph() {
    return graph;
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

  @Override
  public boolean isDirected() {
    return graph.isDirected();
  }

  @Override
  public IdentifiableCollection<Edge> edges() {
    return graph.edges();
  }

  @Override
  public IdentifiableCollection<Node> nodes() {
    return graph.nodes();
  }

  @Override
  public int edgeCount() {
    return graph.edgeCount();
  }

  @Override
  public int nodeCount() {
    return graph.nodeCount();
  }

  @Override
  public IdentifiableCollection<Edge> incidentEdges( Node node ) {
    return graph.incidentEdges( node );
  }

  @Override
  public IdentifiableCollection<Node> adjacentNodes( Node node ) {
    return graph.adjacentNodes( node );
  }

  @Override
  public int degree( Node node ) {
    return graph.degree( node );
  }

  @Override
  public boolean contains( Edge edge ) {
    return graph.contains( edge );
  }

  @Override
  public boolean contains( Node node ) {
    return graph.contains( node );
  }

  @Override
  public Edge getEdge( int id ) {
    return graph.getEdge( id );
  }

  @Override
  public Edge getEdge( Node start, Node end ) {
    return graph.getEdge( start, end );
  }

  @Override
  public IdentifiableCollection<Edge> getEdges( Node start, Node end ) {
    return graph.getEdges( start, end );
  }

  @Override
  public Node getNode( int id ) {
    return graph.getNode( id );
  }

  @Override
  public Iterator<Node> iterator() {
    return graph.iterator();
  }
}
