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

package de.tu_berlin.coga.netflow.classic.transshipment;

import de.tu_berlin.coga.common.algorithm.Algorithm;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.classic.maxflow.PushRelabelHighestLabelGlobalGapRelabelling;
import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.netflow.ds.flow.MaximumFlow;
import de.tu_berlin.coga.netflow.ds.network.OldResidualNetwork;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Groß
 */
public class StaticTransshipment implements Runnable {

  private IdentifiableIntegerMapping<Node> balances;
  private IdentifiableIntegerMapping<Edge> capacities;
  private DefaultDirectedGraph network;
  private IdentifiableIntegerMapping<Edge> flow;
  private OldResidualNetwork residualNetwork;
  private boolean feasible;
  private int valueOfFlow;
  private Algorithm<MaximumFlowProblem, MaximumFlow> algorithm;

  public Algorithm<MaximumFlowProblem, MaximumFlow> getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm( Algorithm<MaximumFlowProblem, MaximumFlow> algorithm ) {
    this.algorithm = algorithm;
  }

  public StaticTransshipment( DefaultDirectedGraph network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Node> balances ) {
    this.balances = balances;
    this.capacities = capacities;
    this.network = network;
    algorithm = new PushRelabelHighestLabelGlobalGapRelabelling();
    //algorithm = new FordFulkerson();
  }

  protected int getBalanceOfNode( Node node ) {
    int result = 0;
    for( Edge edge : network.outgoingEdges( node ) ) {
      result += residualNetwork.flow().get( edge );
    }
    for( Edge edge : network.incomingEdges( node ) ) {
      result -= residualNetwork.flow().get( edge );
    }
    return result;
  }

  public void run() {
    feasible = false;
    List<Node> sources = new LinkedList<>();
    List<Node> sinks = new LinkedList<>();
    long total = 0;
    for( Node node : network.nodes() ) {
      total += balances.get( node );
      if( balances.get( node ) < 0 ) {
        sinks.add( node );
      }
      if( balances.get( node ) > 0 ) {
        sources.add( node );
      }
    }
    assert( total == 0 ) : "Balances sum up to " + total + ", but must be 0";

    // TODO: extended network!
    network.setNodeCapacity( network.getNodeCapacity() + 2 );
    Node supersource = network.getNode( network.getNodeCapacity() - 2 );
    Node supersink = network.getNode( network.getNodeCapacity() - 1 );
    network.setEdgeCapacity( network.getEdgeCapacity() + sources.size() + sinks.size() );
    capacities.setDomainSize( network.getEdgeCapacity() );
    for( Node source : sources ) {
      Edge edge = network.createAndSetEdge( supersource, source );
      capacities.set( edge, balances.get( source ) );
    }
    for( Node sink : sinks ) {
      Edge edge = network.createAndSetEdge( sink, supersink );
      capacities.set( edge, -balances.get( sink ) );
    }
    MaximumFlowProblem problem = new MaximumFlowProblem( network, capacities, supersource, supersink );
    algorithm.setProblem( problem );
    algorithm.run();
    flow = algorithm.getSolution();
    System.out.println( "Flow: " + flow );
    valueOfFlow = algorithm.getSolution().getFlowValue();
    System.out.println( "Value: " + valueOfFlow );

    //residualNetwork = algorithm.getResidualNetwork();
    residualNetwork = new OldResidualNetwork( network, capacities );
    for( Edge edge : network.edges() ) {
      residualNetwork.augmentFlow( edge, flow.get( edge ) );
    }

    network.setNodeCapacity( network.getNodeCapacity() - 2 );
    network.setEdgeCapacity( network.getEdgeCapacity() - sources.size() - sinks.size() );
    capacities.setDomainSize( network.getEdgeCapacity() );
    flow.setDomainSize( network.getEdgeCapacity() );
    residualNetwork = new OldResidualNetwork( network, capacities );
    for( Edge edge : network.edges() ) {
      residualNetwork.augmentFlow( edge, flow.get( edge ) );
    }
    feasible = true;
    for( Node node : network.nodes() ) {
      // same: feasible = feasible && balances.get(node) == getBalancesOfNode(node);
      if( balances.get( node ) != getBalanceOfNode( node ) ) {
        feasible = false;
      }
    }
  }

  public IdentifiableIntegerMapping<Edge> getFlow() {
    return feasible ? flow : null;
  }

  public IdentifiableIntegerMapping<Edge> getFlowEvenIfNotFeasible() {
    return flow;
  }

  public int getFlowValueEvenIfInfeasibleFlow() {
    return valueOfFlow;
  }

  public OldResidualNetwork getResidualNetwork() {
    return feasible ? residualNetwork : null;
  }

  public static void main( String[] args ) {
    DefaultDirectedGraph graph = new DefaultDirectedGraph(8, 6);
    
    Node sa = graph.getNode( 0 );
    Node sb = graph.getNode( 1 );
    Node sc = graph.getNode( 2 );
    Node sd = graph.getNode( 3 );
    
    Node ta = graph.getNode( 4 );
    Node tb = graph.getNode( 5 );
    Node tc = graph.getNode( 6 );
    Node td = graph.getNode( 7 );
    
    
    Edge e1 = graph.createAndSetEdge( sa, ta );
    Edge e2 = graph.createAndSetEdge( sb, tb );
    Edge e3 = graph.createAndSetEdge( sc, tb );
    Edge e4 = graph.createAndSetEdge( sc, td );
    Edge e5 = graph.createAndSetEdge( sd, tc );
    Edge e6 = graph.createAndSetEdge( sd, td );
    
    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 6 );
    capacities.set( e1, 100 );
    capacities.set( e2, 100 );
    capacities.set( e3, 100 );
    capacities.set( e4, 100 );
    capacities.set( e5, 100 );
    capacities.set( e6, 100 );
    
    IdentifiableIntegerMapping<Node> balances = new IdentifiableIntegerMapping<>( 8 );
    balances.set( sa, 1 );
    balances.set( sb, 1 );
    balances.set( sc, 1 );
    balances.set( sd, 2 );
    balances.set( ta, -1 );
    balances.set( tb, -1 );
    balances.set( tc, -1 );
    balances.set( td, -2 );

    
    
    StaticTransshipment trans = new StaticTransshipment( graph, capacities, balances );
    
    trans.run();
    System.out.println( trans.getFlow() );
  }
}
