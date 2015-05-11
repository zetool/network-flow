/**
 * FordFulkersonTest.java Created: 06.06.2014, 15:59:15
 */
package org.zetool.netflow.classic.maxflow;

import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.netflow.ds.network.DirectedNetwork;
import org.zetool.netflow.ds.network.Network;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FordFulkersonTest {
  @Test
  public void testMultipleSources() {
    DefaultDirectedGraph graph = new DefaultDirectedGraph( 3, 2 );
    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 2 );

    Edge e1 = graph.createAndSetEdge( graph.getNode( 0 ), graph.getNode( 2 ) );
    Edge e2 = graph.createAndSetEdge( graph.getNode( 1 ), graph.getNode( 2 ) );

    capacities.set( e1, 1 );
    capacities.set( e2, 2 );
    
    List<Node> sources = new LinkedList<>();
    sources.add( graph.getNode( 0 ) );
    sources.add( graph.getNode( 1 ) );
    List<Node> sinks = Collections.singletonList( graph.getNode( 2 ) );
    
    Network network = new DirectedNetwork( graph, capacities, sources, sinks );
    MaximumFlowProblem mfp = new MaximumFlowProblem( network );

    FordFulkerson ff = new FordFulkerson();

    ff.setProblem( mfp );
    ff.run();

    assertEquals( "Flow value", 3, ff.getSolution().getFlowValue() );
    System.out.println( ff.getSolution().toString() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }
  
  @Test
  public void testInstance() {
    Network network = FlowTestInstances.getDiamondExample();

    MaximumFlowProblem mfp = new MaximumFlowProblem( network );

    FordFulkerson ff = new FordFulkerson();

    ff.setProblem( mfp );
    ff.run();

    assertEquals( "Flow value", 3, ff.getSolution().getFlowValue() );
    System.out.println( ff.getSolution().toString() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }

  @Test
  public void testInstanceUndirected() {
    Network network = FlowTestInstances.getDiamondExampleUndirected();

    MaximumFlowProblem mfp = new MaximumFlowProblem( network );

    FordFulkerson ff = new FordFulkerson();

    ff.setProblem( mfp );
    ff.run();

    //TODO: calculation of flow value does not work.
    assertEquals( "Flow value", 0, ff.getSolution().getFlowValue() );
    System.out.println( ff.getSolution().toString() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }

  @Test
  public void testHarrisRoss() {
    Network network = FlowTestInstances.getHarrisRossOriginal();

    MaximumFlowProblem mfp = new MaximumFlowProblem( network );

    FordFulkerson ff = new FordFulkerson();

    ff.setProblem( mfp );
    ff.run();
    
    System.out.println( ff.getSolution() );

    assertEquals( "Flow value", 163, ff.getSolution().getFlowValue() );
    System.out.println( ff.getSolution().toString() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }
}
