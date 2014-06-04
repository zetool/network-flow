/* zet evacuation tool copyright (c) 2007-09 zet evacuation team
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

import de.tu_berlin.coga.netflow.classic.problems.MinimumCostFlowProblem;
import de.tu_berlin.math.coga.algorithm.shortestpath.MooreBellmanFord;
import de.tu_berlin.coga.graph.structure.Path;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.traversal.BreadthFirstSearch;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.network.ResidualNetwork;
import de.tu_berlin.coga.netflow.ds.network.TimeExpandedNetwork;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.graph.DirectedGraph;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Martin Groß
 */
public class SuccessiveShortestPath /*extends Algorithm<MinimumCostFlowProblem, IdentifiableIntegerMapping<Edge>>*/ {

  private static final Logger LOGGER = Logger.getLogger( "fv.model.algorithm.SuccessiveShortestPath" );
  private DirectedGraph graph;
  private IdentifiableIntegerMapping<Node> baseBalances;
  private IdentifiableIntegerMapping<Edge> capacities;
  private IdentifiableIntegerMapping<Edge> baseCosts;
  private boolean bFlowExists = false;
  private IdentifiableIntegerMapping<Edge> flow;
  private List<Path> paths;
  private boolean bounds;
  private transient IdentifiableIntegerMapping<Node> balances;
  private transient IdentifiableIntegerMapping<Edge> costs;
  private transient ResidualNetwork residualNetwork;

  public SuccessiveShortestPath( DirectedGraph graph, IdentifiableIntegerMapping<Node> balances, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> costs ) {
    this.graph = graph;
    this.baseBalances = balances;
    this.baseCosts = costs;
    this.capacities = capacities;
  }

  public SuccessiveShortestPath( DirectedGraph graph, IdentifiableIntegerMapping<Node> balances, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> costs, boolean bounds ) {
    this.graph = graph;
    this.baseBalances = balances;
    this.baseCosts = costs;
    this.capacities = capacities;
    this.bounds = bounds;
  }

  public int balance( Node node ) {
    return balances.get( node );
  }

  public int capacity( Edge edge ) {
    return residualNetwork.residualCapacities().get( edge );
  }

  public int cost( Edge edge ) {
    return costs.get( edge );
  }

  public IdentifiableIntegerMapping<Edge> getFlow() {
    return flow;
  }

  public boolean bFlowExists() {
    return bFlowExists;
  }

  public List<Path> getPaths() {
    return paths;
  }

  private boolean existsPathBetween( DirectedGraph network, Node start, Node end ) {
    BreadthFirstSearch bfs = new BreadthFirstSearch();
    bfs.setProblem( network );
    bfs.setStart( start );
    bfs.setStop( end );
    bfs.run();
    return bfs.getDistance( end ) < Integer.MAX_VALUE;
  }

  public void run() {
    // Create the residual graph
    residualNetwork = new ResidualNetwork( graph, capacities );
    // Create a copy of the balance map since we are going to modify it
    balances = new IdentifiableIntegerMapping<>( baseBalances );
    // Extend the costs to the residual graph
    costs = new IdentifiableIntegerMapping<>( graph.edges() );
    // Prepare the path lists
    paths = new LinkedList<>();
    for( Edge edge : graph.edges() ) {
      costs.set( edge, baseCosts.get( edge ) );
      costs.set( residualNetwork.reverseEdge( edge ), -baseCosts.get( edge ) );
      if( baseCosts.get( edge ) < 0 ) {
        int capacity = capacities.get( edge );
        residualNetwork.augmentFlow( edge, capacity );
        balances.decrease( edge.start(), capacity );
        balances.increase( edge.end(), capacity );
      }
    }
    // Guarantee conservative costs by saturating negative cost edges
    for( Edge edge : residualNetwork.edges() ) {
      if( cost( edge ) < 0 ) {
        residualNetwork.augmentFlow( edge, capacity( edge ) );
        balances.decrease( edge.start(), capacity( edge ) );
        balances.increase( edge.end(), capacity( edge ) );
      }
    }
    int total = 0;
    int totalcost = 0;
    while( true ) {
      // Pick a feasible source-sink pair
      Node source = null;
      Node sink = null;
      for( Node node : graph.nodes() ) {
        if( balance( node ) > 0 ) {
          source = node;
          break;
        }
      }
      if( source != null ) {
        for( Node node : graph.nodes() ) {
          if( balance( node ) < 0 && existsPathBetween( residualNetwork, source, node ) ) {
            sink = node;
            break;
          }
        }
      }
      // If there are no sources and sinks left, we are done
      if( source == null && (sink == null || bounds) ) {
        flow = residualNetwork.flow();
        bFlowExists = true;
        return;
        // If there are only sources or sinks left, there is no b-flow
      } else if( source == null && !bounds && sink != null || source != null && sink == null ) {
        bFlowExists = false;
        return;
      }
      // Find a cost minimal source-sink-path
      MooreBellmanFord mbf = new MooreBellmanFord( residualNetwork, costs, source );
      mbf.run();
      Path shortestPath = mbf.getShortestPath( sink );
      paths.add( shortestPath );
      LOGGER.finest( "Der kürzeste Pfad ist " + shortestPath );
      // Augment flow along this shortest path
      int amount = Math.min( balance( source ), -balance( sink ) );
      for( Edge edge : shortestPath ) {
        if( capacity( edge ) < amount ) {
          amount = capacity( edge );
        }
      }
      LOGGER.finest( "Augmentiere " + amount + " Flusseinheiten." );
      //System.out.println("Augmentiere " + amount + " Flusseinheiten.");
      int pathCost = 0;
      for( Edge edge : shortestPath ) {
        pathCost += costs.get( edge );
      }
      total += amount;
      totalcost += amount * pathCost;
      System.out.println( "Sent: " + total + " witch cost: " + totalcost );

      balances.decrease( source, amount );
      balances.increase( sink, amount );
      LOGGER.finest( "Es warten " + balances.get( source ) + " Flusseinheiten in der Quelle, während " + balances.get( sink ) + " Flusseinheiten in der Senke benötigt werden." );
      for( Edge edge : shortestPath ) {
        residualNetwork.augmentFlow( edge, amount );
      }
    }
  }

  public static void main( String[] args ) {
    DefaultDirectedGraph network = new DefaultDirectedGraph( 4, 5 );
    Node source = network.getNode( 0 );
    Node a = network.getNode( 1 );
    Node b = network.getNode( 2 );
    Node sink = network.getNode( 3 );
    Edge e1 = new Edge( 0, source, a );
    Edge e2 = new Edge( 1, source, b );
    Edge e3 = new Edge( 2, a, b );
    Edge e4 = new Edge( 3, a, sink );
    Edge e5 = new Edge( 4, b, sink );
    network.setEdge( e1 );
    network.setEdge( e2 );
    network.setEdge( e3 );
    network.setEdge( e4 );
    network.setEdge( e5 );
    IdentifiableIntegerMapping<Node> balances;
    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( network.edges() );
    capacities.set( e1, 1 );
    capacities.set( e2, 1 );
    capacities.set( e3, 1 );
    capacities.set( e4, 1 );
    capacities.set( e5, 1 );
    IdentifiableIntegerMapping<Edge> costs = new IdentifiableIntegerMapping<>( network.edges() );
    costs.set( e1, 1 );
    costs.set( e2, 3 );
    costs.set( e3, 1 );
    costs.set( e4, 3 );
    costs.set( e5, 1 );
    TimeExpandedNetwork teg = new TimeExpandedNetwork( network, capacities, costs, source, sink, 8, true );
    balances = new IdentifiableIntegerMapping<>( teg.nodes() );
    balances.set( teg.singleSource(), 10 );
    balances.set( teg.singleSink(), -10 );
    SuccessiveShortestPath algo = new SuccessiveShortestPath( teg, balances, teg.capacities(), teg.costs() );
    algo.run();
    //System.out.println(algo.getPaths());
    for( Path path : algo.getPaths() ) {
      //System.out.println(path.toString());
    }
  }

  //@Override
  protected IdentifiableIntegerMapping<Edge> runAlgorithm( MinimumCostFlowProblem problem ) {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
}
