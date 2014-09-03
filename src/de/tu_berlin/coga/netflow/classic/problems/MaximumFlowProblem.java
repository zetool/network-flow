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

package de.tu_berlin.coga.netflow.classic.problems;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Graph;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.network.ExtendedGraph;
import de.tu_berlin.coga.netflow.ds.network.Network;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Groß
 */
public class MaximumFlowProblem {
	private Graph graph;
	private IdentifiableIntegerMapping<Edge> capacities;
	private List<Node> sources;
	private List<Node> sinks;

	public MaximumFlowProblem( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
		this.graph = graph;
		this.capacities = capacities;
		this.sources = sources;
		this.sinks = sinks;
	}

	public MaximumFlowProblem( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
		this( graph, capacities, Collections.singletonList( source ) , sinks );
	}

	public MaximumFlowProblem( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
		this( graph, capacities, sources, Collections.singletonList( sink ) );
	}

	public MaximumFlowProblem( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
		this( graph, capacities, Collections.singletonList( source ), Collections.singletonList( sink ) );
	}

  public MaximumFlowProblem( Network network ) {
    if( network.sinkCount() == 0 || network.sourceCount() == 0 ) {
      throw new IllegalArgumentException( "At least one sink and source must be specified." );
    }
		this.graph = network.getGraph();
		this.capacities = network.getCapacities();
		this.sources = new LinkedList<>( network.sources() );
		this.sinks = new LinkedList<>( network.sinks() );
  }

	public IdentifiableIntegerMapping<Edge> getCapacities() {
		return capacities;
	}

	public Graph getNetwork() {
		return graph;
	}

	public Node getSink() {
		if( sinks.size() == 1 )
			return sinks.get( 0 );
		throw new IllegalStateException( "There are multiple sinks." );
	}

	public List<Node> getSinks() {
		return sinks;
	}

	public Node getSource() {
		if( sources.size() == 1 )
			return sources.get( 0 );
		throw new IllegalStateException( "There are multiple sources." );
	}

	public List<Node> getSources() {
		return sources;
	}

  public MaximumFlowProblem asSingleSourceProblem() {
    if( !(graph instanceof DirectedGraph) ) {
      throw new UnsupportedOperationException( "Only works for directed grpahs!" );
    }
    ExtendedGraph extended = new ExtendedGraph( (DirectedGraph)graph, 2, sources.size() + sinks.size() );

    Node newSource = extended.getFirstNewNode();
    Node newSink = extended.getNode( newSource.id() + 1 );

    IdentifiableIntegerMapping<Edge> newCapacities = new IdentifiableIntegerMapping<>( capacities, extended.edgeCount() );
    for( Node s : sources ) {
      Edge e = extended.createAndSetEdge( newSource, s );
      newCapacities.set( e, Integer.MAX_VALUE );
    }
    for( Node t : sinks ) {
      Edge e = extended.createAndSetEdge( t, newSink );
      newCapacities.set( e, Integer.MAX_VALUE );
    }
    return new MaximumFlowProblem( extended, newCapacities, newSource, newSink );
  }

  public final boolean isSingleSourceSink() {
    return getSources().size() == 1 && getSinks().size() == 1;
  }
}
