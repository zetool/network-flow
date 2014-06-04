/* zet evacuation tool copyright (c) 2007-10 zet evacuation team
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

import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.container.collection.ListSequence;
import de.tu_berlin.coga.graph.Edge;
import ds.graph.GraphLocalization;
import de.tu_berlin.coga.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.util.OppositeNodeCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The term {@code DynamicNetwork} refers to the fact, that the network
 * structure can change over time.
 */
public class DynamicNetwork implements DirectedGraph {

	protected ListSequence<Node> nodes;
	protected ListSequence<Edge> edges;
	protected transient Map<Node, ListSequence<Edge>> incomingEdges;
	protected transient Map<Node, ListSequence<Edge>> outgoingEdges;
  protected ListSequence<Edge> temp_removed_edges = new ListSequence<>();
  protected ListSequence<Node> temp_removed_nodes = new ListSequence<>();

	public DynamicNetwork() {
		nodes = new ListSequence<>();
		edges = new ListSequence<>();
		incomingEdges = new HashMap<>();
		outgoingEdges = new HashMap<>();
	}

	public DynamicNetwork( DynamicNetwork graph ) {
		this();
		setNodes( graph.nodes() );
		setEdges( graph.edges() );
	}

	public DynamicNetwork( Iterable<Node> nodes, Iterable<Edge> edges ) {
		this();
		setNodes( nodes );
		setEdges( edges );
	}

	public DynamicNetwork( DirectedGraph graph ) {
		this();
		setNodes( graph.nodes() );
		setEdges( graph.edges() );
	}

	public boolean directed() {
		return true;
	}

	public void addEdge( Edge edge ) {
		edges.add( edge );
		incomingEdges.get( edge.end() ).add( edge );
		outgoingEdges.get( edge.start() ).add( edge );
	}

	public void addEdges( Iterable<Edge> edges ) {
		for( Edge edge : edges )
			addEdge( edge );
	}

	public void addNode( Node node ) {
		nodes.add( node );
		incomingEdges.put( node, new ListSequence<>() );
		outgoingEdges.put( node, new ListSequence<>() );
	}

	public void addNodes( Iterable<Node> nodes ) {
		for( Node node : nodes )
			addNode( node );
	}

	@Override
	public boolean contains( Node node ) {
		return incomingEdges.containsKey( node );
	}

	@Override
	public boolean contains( Edge edge ) {
		return edges.contains( edge );
	}

	public List<Edge> extractEdges() {
		List<Edge> edgesCopy = this.edges;
		setEdges( new ArrayList<>() );
		return edgesCopy;
	}

	@Override
	public Edge getEdge( int index ) {
		return edges.get( index );
	}

	@Override
	public Edge getEdge( Node source, Node target ) {
		for( Edge edge : outgoingEdges.get( source ) )
			if( edge.start() == source && edge.end() == target )
				return edge;
		return null;
	}

	@Override
	public Node getNode( int index ) {
		return nodes.get( index );
	}

	@Override
	public ListSequence<Edge> edges() {
		return edges;
	}

	@Override
	public ListSequence<Edge> incomingEdges( Node node ) {
		return incomingEdges.get( node );
	}

	public ListSequence<Edge> incidentEdges( Node node ) {
		ListSequence<Edge> incidentEdges = new ListSequence<>();
		incidentEdges.addAll( incomingEdges( node ) );
		incidentEdges.addAll( outgoingEdges( node ) );
		return incidentEdges;
	}

	public ListSequence<Edge> outgoingEdges( Node node ) {
		return outgoingEdges.get( node );
	}

	public ListSequence<Node> nodes() {
		return nodes;
	}

	@Override
	public int degree( Node node ) {
		return inDegree( node ) + outDegree( node );
	}

	public int inDegree( Node node ) {
		return incomingEdges( node ).size();
	}

	public int outDegree( Node node ) {
		return outgoingEdges( node ).size();
	}

	public int edgeCount() {
		return edges.size();
	}

	public int nodeCount() {
		return nodes.size();
	}

	public Node opposite( Edge edge, Node node ) {
		if( node == edge.start() )
			return edge.end();
		else if( node == edge.end() )
			return edge.start();
		else
			throw new IllegalArgumentException( GraphLocalization.loc.getString( "ds.graph.NotIncidentException" + node + ", " + edge ) );
	}

	public Node opposite( Node node, Edge edge ) {
		return opposite( edge, node );
	}

	public void removeAllEdges() {
		for( Node node : nodes ) {
			incomingEdges.get( node ).clear();
			outgoingEdges.get( node ).clear();
		}
		edges.clear();
	}

	public void removeAllNodes() {
		edges.clear();
		nodes.clear();
		incomingEdges.clear();
		outgoingEdges.clear();
	}

	public void removeEdge( Edge edge ) {
		edges.remove( edge );
		incomingEdges.get( edge.end() ).remove( edge );
		outgoingEdges.get( edge.start() ).remove( edge );
	}

	public void removeEdges( Iterable<Edge> edges ) {
		for( Edge edge : edges )
			removeEdge( edge );
	}

	public void removeLoops() {
		ListSequence<Edge> loops = new ListSequence<>();
		for( Edge edge : edges )
			if( edge.start() == edge.end() )
				loops.add( edge );
		removeEdges( loops );
	}

	public void removeNode( Node node ) {
		removeEdges( incidentEdges( node ) );
		nodes.remove( node );
	}

	public void removeNodes( Iterable<Node> nodes ) {
		for( Node node : nodes )
			removeNode( node );
	}

	public void retainEdges( Collection<Edge> edges ) {
		ListSequence<Edge> copy = edges().clone();
		for( Edge edge : edges )
			copy.remove( edge );
		removeEdges( copy );
	}

	public void retainNodes( Iterable<Node> nodes ) {
		ListSequence<Edge> edgesCopy = edges().clone();
		removeAllNodes();
		addNodes( nodes );
		for( Edge edge : edgesCopy )
			if( contains( edge.start() ) && contains( edge.end() ) )
				addEdge( edge );
	}

	public void setEdges( Iterable<Edge> edges ) {
		removeAllEdges();
		addEdges( edges );
	}

	public void setNodes( Iterable<Node> nodes ) {
		removeAllNodes();
		addNodes( nodes );
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "V = {" );
		for( Node node : nodes )
			builder.append( node ).append( "," );
		if( !nodes.isEmpty() )
			builder.deleteCharAt( builder.length() - 1 );
		builder.append( "}\n" );
		builder.append( "E = {" );
		int counter = 0;
		for( Edge edge : edges ) {
			if( counter == 10 ) {
				counter = 0;
				builder.append( "\n" );
			}
			builder.append( edge ).append( "," );
			counter++;
		}
		if( !edges.isEmpty() )
			builder.deleteCharAt( builder.length() - 1 );
		builder.append( "}" );
		return builder.toString();
	}

	public String deepToString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "V = {" );
		for( Node node : nodes )
			builder.append( node ).append( "," );
		if( !nodes.isEmpty() )
			builder.deleteCharAt( builder.length() - 1 );
		builder.append( "}\n" );
		builder.append( "E = {" );
		int counter = 0;
		for( Edge edge : edges ) {
			if( counter == 10 ) {
				counter = 0;
				builder.append( "\n" );
			}
			builder.append( edge.nodesToString() ).append( "," );
			counter++;
		}
		if( !edges.isEmpty() )
			builder.deleteCharAt( builder.length() - 1 );
		builder.append( "}" );
		return builder.toString();
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public IdentifiableCollection<Node> adjacentNodes( Node node ) {
		return new OppositeNodeCollection( node, incidentEdges( node ) );
	}

	@Override
	public IdentifiableCollection<Node> predecessorNodes( Node node ) {
		return new OppositeNodeCollection( node, incomingEdges.get( node ) );
	}

	@Override
	public IdentifiableCollection<Node> successorNodes( Node node ) {
		return new OppositeNodeCollection( node, outgoingEdges.get( node ) );
	}

	public void setNode( Node node ) {
		addNode( node );
	}

	public void setEdge( Edge edge ) {
		addEdge( edge );
	}

	@Override
	public IdentifiableCollection<Edge> getEdges( Node start, Node end ) {
		throw new UnsupportedOperationException( GraphLocalization.loc.getString( "ds.graph.NotSupportedException" ) );
	}

	public DirectedGraph getAsStaticNetwork() {
		DefaultDirectedGraph network = new DefaultDirectedGraph( nodeCount(), edgeCount() );
		network.setNodes( nodes );
		network.setEdges( edges );
		return network;
	}

        public void add_edge_temp(Edge e)
        {
            temp_removed_edges.remove(e);
        }

        public void add_node_temp(Node n)
        {
            temp_removed_nodes.remove(n);
        }

        public void remove_edge_temp(Edge e)
        {
            temp_removed_edges.add(e);
        }

        public void remove_node_temp(Node n)
        {
            temp_removed_nodes.add(n);
        }

        public void recover_temp_removed_edges()
        {
            temp_removed_edges.clear();
        }

        public void recover_temp_removed_nodes()
        {
            temp_removed_nodes.clear();
        }

        public IdentifiableCollection<Node> temp_adjacentNodes( Node node ) {

            IdentifiableCollection<Node> adjacent_nodes = new ListSequence<>();

                if (!temp_removed_nodes.contains(node))
                {
                     IdentifiableCollection<Edge> adj = outgoingEdges(node);
                     for (Edge e: adj)
                     {
                         if (temp_removed_nodes.contains(e.opposite(node)) || temp_removed_edges.contains(e))
                         {
                             continue;
                         }
                         adjacent_nodes.add(e.opposite(node));
                     }
                }

		return adjacent_nodes;
	}

	public IdentifiableCollection<Node> temp_predNodes( Node node ) {
		IdentifiableCollection<Node> adjacent_nodes = new ListSequence<>();

		if( !temp_removed_nodes.contains( node ) ) {
			IdentifiableCollection<Edge> adj = incomingEdges( node );
			for( Edge e : adj ) {
				if( temp_removed_nodes.contains( e.opposite( node ) ) || temp_removed_edges.contains( e ) )
					continue;
				adjacent_nodes.add( e.opposite( node ) );
			}
		}

		return adjacent_nodes;
	}

	@Override
	public Iterator<Node> iterator() {
		return nodes.iterator();
	}
}

