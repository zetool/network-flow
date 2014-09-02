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

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;

/**
 * Additional graph methods that are used to insert new edges and nodes to
 * graphs.
 */
public interface ModifiableGraph {

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
}
