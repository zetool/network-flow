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

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.structure.Path;

/**
 * The {@code AbstractNetwork} provides an implementation of a directed graph
 * optimized for use by flow algorithms. Examples of these optimizations
 * include use of array based data structures for edges and nodes in order to
 * provide fast access, as well as the possiblity to hide edges and nodes (which
 * is useful for residual networks, for instance).
 */
public abstract class AbstractNetwork implements NetworkInterface, Cloneable {



	/**
	 * Returns the number of edges that can be contained in the graph.
	 * Runtime O(1).
	 * @return the number of edges that can be contained in the graph.
	 */
	public abstract int getEdgeCapacity();

	/**
	 * Allocates space for edges to be contained in the graph.
	 * Runtime O(newCapacity).
	 * @param newCapacity the number of edges that can be contained by the
	 * graph.
	 */
	public abstract void setEdgeCapacity( int newCapacity );

	/**
	 * Returns the number of nodes that can be contained in the graph.
	 * Runtime O(1).
	 * @return the number of nodes that can be contained in the graph.
	 */
	public abstract int getNodeCapacity();

	/**
	 * Allocates space for nodes to be contained in the graph.
	 * Runtime O(newCapacity).
	 * @param newCapacity the number of nodes that can be contained by the
	 * graph.
	 */
	public abstract void setNodeCapacity( int newCapacity );

	/**
	 * Checks whether the specified edge is hidden. Runtime O(1).
	 * @param edge the edge to be tested.
	 * @return {@code true} if the specified edge is hidden, {@code false
	 * } otherwise.
	 */
	public abstract boolean isHidden( Edge edge );

	/**
	 * Sets the hidden state of the specified edge to the specified value. A
	 * hidden edge is treated as if it did not belong to the graph - the only
	 * difference to it being actually deleted is that it can be restored very
	 * efficiently. This can be useful for residual networks amongst other
	 * things. Runtime O(1).
	 * @param edge the edge for which the hidden state is to be set.
	 * @param value the new value of the edge's hidden state.
	 */
	public abstract void setHidden( Edge edge, boolean value );

	/**
	 * Checks whether the specified node is hidden. Runtime O(1).
	 * @param node the node to be tested.
	 * @return {@code true} if the specified node is hidden, {@code false
	 * } otherwise.
	 */
	public abstract boolean isHidden( Node node );

	/**
	 * Sets the hidden state of the specified node to the specified value. A
	 * hidden node is treated as if it did not belong to the graph - the only
	 * difference is to it being actually deleted is that it can be restored
	 * very efficiently. Hiding a node
	 * causes all edges incident to it to be hidden as well.
	 * Runtime O(degree(node)).
	 * @param node the node for which the hidden state is to be set.
	 * @param value the new value of the node's hidden state.
	 */
	public abstract void setHidden( Node node, boolean value );

	public abstract void setHiddenOnlyNode( Node node, boolean value );

	public abstract void showAllEdges();

	/**
	 * Creates a new directed edge between the specified start and end nodes and
	 * adds it to the graph (provided the graph has enough space allocated for
	 * an additional edge). Runtime O(1).
	 * @param start the start node of the new edge.
	 * @param end the end node of the new edge.
	 * @return the new edge.
	 */
	public abstract Edge createAndSetEdge( Node start, Node end );

	/**
	 * Adds the specified edges to the graph by calling {@code setEdge} for
	 * each edge. Runtime O(number of edges).
	 * @param edges the edges to be added to the graph.
	 */
	public abstract void setEdges( Iterable<Edge> edges );

	/**
	 * Adds the specified nodes to the graph by calling {@code setNode} for
	 * each node. Runtime O(number of nodes).
	 * @param nodes the nodes to be added to the graph.
	 */
	public abstract void setNodes( Iterable<Node> nodes );

	public abstract Path getPath( Node start, Node end );

	/**
	 * Checks whether at least one edge between the specified start and end nodes
	 * exists.
	 * @param start the start node of the edge to be checked
	 * @param end the end node of the path to be checked
	 * @return {@code true} if the edge between the start node and the end node exists, {@code false} otherwise
	 */
	public abstract boolean existsEdge( Node start, Node end );

	/**
	 * Creates a network equal to the network but all edges between a pair of
	 * nodes are reversed.
	 * @return a reversed copy of the network
	 */
	public abstract AbstractNetwork createReverseNetwork();

	@Override
	public abstract AbstractNetwork clone();

	/**
	 * Returns the network as a {@code AbstractNetwork} object, i.e. as a static graph.
	 * @return the network as a {@code AbstractNetwork} object.
	 */
	public abstract AbstractNetwork getAsStaticNetwork();

}
