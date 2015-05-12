
package org.zetool.netflow.classic.maxflow;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.junit.Test;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.util.GraphUtil;
import org.zetool.netflow.ds.flow.MaximumFlow;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class PushRelabelHighestLabelTest {

	@Test
	public void testInstance() {
    int[] edgeCapacities = {2, 1, 1, 1, 2};
    int[][] edges = {
      {0, 1},
      {0, 2},
      {1, 2},
      {1, 3},
      {2, 3}};
    DirectedGraph network = GraphUtil.generateDirected( 4, edges );
    IdentifiableIntegerMapping<Edge> capacities = getCapacities( network, edgeCapacities );
		MaximumFlowProblem mfp = new MaximumFlowProblem( network, capacities, network.getNode( 0 ), network.getNode( 3 ) );
    
    // Test algorithm
		PushRelabelHighestLabel hipr = new PushRelabelHighestLabel();
		hipr.setProblem( mfp );
		hipr.run();

    // Assert
    int[] expectedMaximumFlow = {2, 1, 1, 1, 2};
    assertThat( "Max flow not 3", hipr.getSolution().getFlowValue(), is( equalTo( 3L ) ) );
		assertThat( hipr.getSolution().check(), is( true ) );
    assertFlow( network, hipr.getSolution(), expectedMaximumFlow );
	}
  
  /**
   * Asserts the flow values on edges in a {@link MaximumFlow} for a graph.
   * @param network a network as directed graph
   * @param actualFlow a flow assigning flow values to edges
   * @param expectedFlow the expected flow values, ordered by {@link Edge#id()}
   */
  public static void assertFlow( DirectedGraph network, MaximumFlow actualFlow, int[] expectedFlow ) {
    for( Edge e : network.edges() ) {
      assertThat( actualFlow.get( e ), is( equalTo( expectedFlow[e.id() ] ) ) );
    }
  }
  
  public static IdentifiableIntegerMapping<Edge> getCapacities( DirectedGraph g, int[] capacities ) {
    if( g.edgeCount() != capacities.length ) {
      throw new IllegalArgumentException( "Edges: " + g.edgeCount() + ", capacities: " + capacities.length );
    }
    
		IdentifiableIntegerMapping<Edge> edgeCapacities = new IdentifiableIntegerMapping<>( g.edgeCount() );
    for( Edge e : g.edges() ) {
      edgeCapacities.set( e, capacities[e.id()] );
    }
    return edgeCapacities;
  }
}
