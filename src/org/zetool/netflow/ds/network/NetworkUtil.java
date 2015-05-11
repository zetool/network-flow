
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
   * 
   * @param nodes
   * @param edges
   * @param source
   * @param sink
   * @return 
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

  /** Private constructor for utility class. */
  private NetworkUtil() {
  }
}
