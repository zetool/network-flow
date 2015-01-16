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
package de.tu_berlin.coga.netflow.classic.mincost;

import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import org.zetool.container.mapping.IdentifiableObjectMapping;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.mapping.Mappings;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.structure.Path;
import de.tu_berlin.coga.graph.structure.StaticPath;
import java.text.NumberFormat;

/**
 * This class provides a method fpr detecting minimum mean cycles in arbitrary
 * weighted graphs.
 *
 * @author Timon Kelter
 */
public abstract class MinimumMeanCycleDetector {
  private final static boolean TIMON = false;
  private final static boolean MARTIN = false;

  /**
   * Detects a minimum mean cost cycle. Running time is O(n*(n+m)).
   *
   * @param g the graph in which the cycles shall be found.
   * @param cost The edge costs for graph {@code n}
   * @return The mean value value of a minimum mean cost cycle. If no cycle is
   * found, 0 is returned.
   */
  public static StaticPath detect( DefaultDirectedGraph g, IdentifiableIntegerMapping<Edge> cost ) {
		// To determine whether such a circuit exists we use the Minmum Mean Cycle Algorithm
    // from the book "Combinatorial Optimization" by "Bernhard Korte, Jens Vygen"
    // Chapter 7.3

		// TODO: Find a more efficient way of reversing the changes to the graph.
    // Cloning and deleting the cloned instance at the end is not too smart.
    // Clone the graph because we will have to modify it
    if( TIMON ) {
      System.out.println( "Graph old: " + g );
    }
    g = g.clone();
		// Reserve enough space in the graph structure for our operations
    // MG: int nodes = g.nodeCount() + 1;
    // MG: int edges = g.edgeCount() + g.nodeCount();
    int nodes = g.getNodeCapacity() + 1;
    int edges = g.getEdgeCapacity() + g.nodeCount();
    g.setNodeCapacity( nodes );
    g.setEdgeCapacity( edges );

		// Add a node that can reach every other node with cost 0
    // MG: Node newNode = g.getNode(g.nodeCount() - 1);
    Node newNode = g.getNode( g.getNodeCapacity() - 1 );

    for( Node n : g.nodes() ) {
      if( newNode != n ) {
        g.createAndSetEdge( newNode, n );
      }
    }
    // Copy the costs / Add the 0 costs for edges leaving node "newNode"
    IdentifiableIntegerMapping<Edge> res_cost
            = new IdentifiableIntegerMapping<>( g.edgeCount() );
    for( Edge e : g.edges() ) {
      if( e.start() == newNode ) {
        res_cost.set( e, 0 );
      } else {
        res_cost.set( e, cost.get( e ) );
      }
    }

    //Beginning of the algorithm - Initialization work
    // TODO detetor, new or delete and make bug free
    int n = g.nodeCount();
		// F is the minimum weight of an edge progression of length [k] (first index) from
    // node s to node [x] (second index)
    IdentifiableIntegerMapping<Node> F[] = new IdentifiableIntegerMapping[n + 1];
		// In the algorithm F[k][x] can be infinite. As int variables cannot store 
    // "infinite" values we introduce this bitflag array to remember an infinite value in F
    IdentifiableObjectMapping<Node, Boolean> isInfinite[] = new IdentifiableObjectMapping[n + 1];

    // Main algorithm works here - dynamic programming where F stores the results
    if( MARTIN ) {
      System.out.println( "Graph here: " + g );
    }
    for( int k = 0; k <= n; k++ ) {
      F[k] = new IdentifiableIntegerMapping<>( g.nodeCount() );
      isInfinite[k] = new IdentifiableObjectMapping<>( g.nodeCount() );

      for( Node x : g.nodes() ) {
        if( MARTIN ) {
          System.out.println( "Node = " + x );
        }
        if( MARTIN ) {
          System.out.println( "Node, incoming = " + g.predecessorNodes( x ) );
        }
        if( MARTIN ) {
          System.out.println( "Node, outgoing = " + g.successorNodes( x ) );
        }
        // Set up initialization values
        if( (k == 0) && (x == newNode) ) {
          F[k].set( x, 0 );
          isInfinite[k].set( x, false );
        } else {
          isInfinite[k].set( x, true );
        }

        // Compute the values for the k-th round
        if( k >= 1 ) {
          if( MARTIN ) {
            System.out.println( String.format( "isInfinite[%1$s] = %2$s", k - 1,
                    Mappings.toString( g.nodes(), isInfinite[k - 1] ) ) );
          }
          for( Edge e : g.incomingEdges( x ) ) {
            if( MARTIN ) {
              System.out.println( String.format( "isInfinite[%1$s](%2$s) = %3$s", k - 1, e.start(),
                      isInfinite[k - 1].get( e.start() ) ) );
            }
            if( !isInfinite[k - 1].get( e.start() ) ) {
              int new_way_cost = F[k - 1].get( e.start() ) + res_cost.get( e );
              if( MARTIN ) {
                System.out.println( String.format( "new_way_cost: %1$s, F[%2$s](%3$s) = %4$s, isInfinite[%2$s](%3$s) = %5$s",
                        new_way_cost, k, x, F[k].get( x ), isInfinite[k].get( x ) ) );
              }
              if( (new_way_cost < F[k].get( x )) || isInfinite[k].get( x ) ) {
                F[k].set( x, new_way_cost );
                if( MARTIN ) {
                  System.out.println( String.format( "Set isInfinite[%1$s](%2$s) = false", k, x ) );
                }
                isInfinite[k].set( x, false );
              }
            }
          }
        }
      }
    }

    if( TIMON ) {
      // Print dynamic programming table
      NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMaximumFractionDigits( 2 );
      nf.setMinimumFractionDigits( 2 );
      nf.setMaximumIntegerDigits( 2 );
      nf.setMinimumIntegerDigits( 2 );
      for( int k = 0; k <= n; k++ ) {
        for( Node node : g.nodes() ) {
          System.out.print( isInfinite[k].get( node ) ? " XX,XX" : ((F[k].get( node ) >= 0) ? "+" : "") + nf.format( F[k].get( node ) ) + " " );
        }
        System.out.println();
      }
    }

    // Helper variables for extracting the minimum mean weight out of F
    double minimum_mean_weight = Double.MAX_VALUE;
    double mean_weight;
    boolean mean_weight_valid;

    // Helper variables for extracting the cycle itself
    Node minimum_node = null;

    // Get the minimum mean weight and the associated cycle
    if( MARTIN ) {
      System.out.println( "isInfinite[" + n + "]: " + Mappings.toString( g.nodes(), isInfinite[n] ) );
    }
    for( Node x : g.nodes() ) {
      if( !isInfinite[n].get( x ) ) {
				// Discovered a path from s to x with length n ( --> has circuit on it )

        // Get the maximum mean weight for node x
        Node temp_node = null;
        mean_weight_valid = false;
        mean_weight = Integer.MIN_VALUE;

        for( int k = 0; k < n; k++ ) {
          if( !isInfinite[k].get( x ) ) {
            mean_weight_valid = true;
            mean_weight = Math.max( mean_weight,
                    (double) (F[n].get( x ) - F[k].get( x ))
                    / (double) (n - k) );

            temp_node = x;
          }
        }
        // Compute the minimum mean weight for all nodes yet seen
        if( mean_weight_valid ) {
          if( minimum_mean_weight > mean_weight ) {
            minimum_mean_weight = mean_weight;
            minimum_node = temp_node;
          }
        }
      }
    }

    // Test whether we found any cycle and reconstruct the minimum mean cycle eventually
    if( minimum_node == null ) {
      // No circuit found at all
      return null;
    } else {
      // Reconstruct the cycle by following the dynamic programming table F
      StaticPath p = new StaticPath();
      Node end = minimum_node;

			// Edges run from F[k-1] to F[k]. We will track the development of the
      // dynamic programming table F in reverse direction, thus starting in
      // level F[n] and working backwards until the cycle length is reached.
      int level = n;

			// Our task here is to extract any cycle that is contained in the construction
      // path of the optimal solution in the dynamic programing table. To detect a
      // circle we use this array of markers. Each of the boolean flags indicates 
      // whether a given node was already visited.
      boolean[] seenNodes = new boolean[g.nodeCount()];
      for( int i = 0; i < seenNodes.length; i++ ) {
        seenNodes[i] = false;
      }
      seenNodes[end.id()] = true;

      do {
        assert (level >= 0);

        // Get the next node on the cycle
        Edge nextEdge = null;
        for( Edge e : g.incomingEdges( end ) ) {
          if( !isInfinite[level - 1].get( e.start() ) ) {
            int way_cost = F[level - 1].get( e.start() ) + res_cost.get( e );
            if( way_cost == F[level].get( end ) ) {
							// If this is true then we chose node "x" as the
              // predecessor of "end" on the minimum-weight way of 
              // length "level" to "end"
              // --> x is the end node for the next level

							// We must take the first edge that matches the condition just like
              // we did when creating the dynamic programming table. So we do break
              // the for-loop here
              nextEdge = e;
              break;
            }
          }
        }

        // Process the edge that was found
        p.addFirstEdge( nextEdge );
        end = nextEdge.start();
        if( seenNodes[end.id()] ) {
					// Seen second time -> cycle found

					// Remove an eventually existing front part of our path 
          // This front part exists if and only if the cycle which 
          // we find does not start with the first node on our path
          while( p.last().end() != end ) {
            p.removeLastEdge();
          }
          break;
        } else {
          seenNodes[end.id()] = true;
        }

        // Shift to the next edge
        level--;
      } while( end != minimum_node );

      return p;
    }
  }

  /**
   * A small testing routine for the algo.
   */
  public static void main( String args[] ) {
    // Small network with one circle, and all edges with weight -2
    DefaultDirectedGraph n1 = new DefaultDirectedGraph( 2, 2 );
    n1.createAndSetEdge( n1.nodes().get( 0 ), n1.nodes().get( 1 ) );
    n1.createAndSetEdge( n1.nodes().get( 1 ), n1.nodes().get( 0 ) );
    IdentifiableIntegerMapping<Edge> cost1
            = new IdentifiableIntegerMapping<>( 2 );
    cost1.set( n1.edges().get( 0 ), -2 );
    cost1.set( n1.edges().get( 1 ), -2 );
    Path mm_cycle1 = detect( n1, cost1 );

    // Small network with one circle, two adjacent edges, and all edges with weight -2
    DefaultDirectedGraph n2 = new DefaultDirectedGraph( 6, 5 );
    n2.createAndSetEdge( n2.nodes().get( 0 ), n2.nodes().get( 1 ) );
    n2.createAndSetEdge( n2.nodes().get( 1 ), n2.nodes().get( 2 ) );
    n2.createAndSetEdge( n2.nodes().get( 2 ), n2.nodes().get( 4 ) );
    n2.createAndSetEdge( n2.nodes().get( 4 ), n2.nodes().get( 5 ) );
    n2.createAndSetEdge( n2.nodes().get( 4 ), n2.nodes().get( 1 ) );
    IdentifiableIntegerMapping<Edge> cost2
            = new IdentifiableIntegerMapping<>( 5 );
    cost2.set( n2.edges().get( 0 ), -2 );
    cost2.set( n2.edges().get( 1 ), -2 );
    cost2.set( n2.edges().get( 2 ), -2 );
    cost2.set( n2.edges().get( 3 ), -2 );
    cost2.set( n2.edges().get( 4 ), -2 );
    Path mm_cycle2 = detect( n2, cost2 );

		// Small network with two intersecting circles and two adjacent edges
    // One circle has weight -2, the second one -4
    DefaultDirectedGraph n3 = new DefaultDirectedGraph( 6, 7 );
    n3.createAndSetEdge( n3.nodes().get( 0 ), n3.nodes().get( 1 ) );
    n3.createAndSetEdge( n3.nodes().get( 1 ), n3.nodes().get( 2 ) );
    n3.createAndSetEdge( n3.nodes().get( 1 ), n3.nodes().get( 3 ) );
    n3.createAndSetEdge( n3.nodes().get( 2 ), n3.nodes().get( 4 ) );
    n3.createAndSetEdge( n3.nodes().get( 3 ), n3.nodes().get( 4 ) );
    n3.createAndSetEdge( n3.nodes().get( 4 ), n3.nodes().get( 5 ) );
    n3.createAndSetEdge( n3.nodes().get( 4 ), n3.nodes().get( 1 ) );
    IdentifiableIntegerMapping<Edge> cost3
            = new IdentifiableIntegerMapping<>( 7 );
    cost3.set( n3.edges().get( 0 ), -2 );
    cost3.set( n3.edges().get( 1 ), -2 );
    cost3.set( n3.edges().get( 2 ), -5 );
    cost3.set( n3.edges().get( 3 ), -2 );
    cost3.set( n3.edges().get( 4 ), -5 );
    cost3.set( n3.edges().get( 5 ), -2 );
    cost3.set( n3.edges().get( 6 ), -2 );
    Path mm_cycle3 = detect( n3, cost3 );

    // Example from "easy_room.xml" with minimum mean circle cost (-14)/3
    DefaultDirectedGraph n4 = new DefaultDirectedGraph( 3, 4 ) {
    };
    n4.createAndSetEdge( n4.nodes().get( 1 ), n4.nodes().get( 2 ) );
    n4.createAndSetEdge( n4.nodes().get( 2 ), n4.nodes().get( 0 ) );
    n4.createAndSetEdge( n4.nodes().get( 2 ), n4.nodes().get( 1 ) );
    n4.createAndSetEdge( n4.nodes().get( 0 ), n4.nodes().get( 1 ) );
    IdentifiableIntegerMapping<Edge> cost4
            = new IdentifiableIntegerMapping<>( 4 );
    cost4.set( n4.edges().get( 0 ), 2 );
    cost4.set( n4.edges().get( 1 ), 0 );
    cost4.set( n4.edges().get( 2 ), 2 );
    cost4.set( n4.edges().get( 3 ), -16 );
    Path mm_cycle4 = detect( n4, cost4 );
  }
}
