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

import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Graph;
import org.zetool.graph.UndirectedGraph;
import org.zetool.graph.util.GraphUtil;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class NetworkUtil {
  
  /** Private constructor for utility class. */
  private NetworkUtil() {
  }
  
  /**
   * Generates a directed graph out of edges in an array. The edges are defined as start and end id in the first two
   * array indices, the third array index defines the edge's capacity. Two dimensional arrays where the second
   * dimension is larger than three are also accepted, the additional entries are ignored.
   *
   * @param nodes the number of n odes
   * @param edges the edges as two dimensional array.
   * @param source the id of the source node
   * @param sink the id of the sink node
   * @return a directed graph
   */
  public static Network generateDirected( int nodes, int[][] edges, int source, int sink ) {
    if( edges[0].length < 3 ) {
      throw new IllegalArgumentException( "Illegal Format!" );
    }
    
    DirectedGraph graph = GraphUtil.generateDirected( nodes, edges );
    return new DirectedNetwork( graph, getCapacities(graph, edges), graph.getNode( source ), graph.getNode( sink ) );
  }

  /**
   * Generates an undirected instance of {@link Network} containing the given edges. The edges
   * are specified as two dimensional array of ids start and end nodes which can take allowed
   * values from {@literal 0} to {@code nodes-1}, similar for the source and sink id.
   * @param nodes the umber of sources to generate, with ids starting from 0
   * @param edges the edges
   * @param source the id of the source node
   * @param sink the id of the sink node
   * @return an undirected {@link Network} according to the given data
   */
  public static Network generateUndirected( int nodes, int[][] edges, int source, int sink ) {
    if( edges[0].length < 3 ) {
      throw new IllegalArgumentException( "Illegal Format!" );
    }
    
    UndirectedGraph graph = GraphUtil.generateUndirected( nodes, edges );
    return new UndirectedNetwork( graph, getCapacities(graph, edges), graph.getNode( source ), graph.getNode( sink ) );
  }
  
  private static IdentifiableIntegerMapping<Edge> getCapacities( Graph graph, int[][] edges ) {
    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( edges.length );

    for( int i = 0; i < edges.length; ++i ) {
      capacities.add( graph.getEdge( i ), edges[i][2] );
    }
    return capacities;
  }
}
