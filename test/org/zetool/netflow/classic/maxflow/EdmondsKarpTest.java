package org.zetool.netflow.classic.maxflow;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EdmondsKarpTest {

  @Test
  public void testHiddenEdges() {
    // TODO: enable edge hiding
    DefaultDirectedGraph n = new DefaultDirectedGraph( 4, 5 );

    int c = 0;
    Node A = n.getNode( c++ );
    Node B = n.getNode( c++ );
    Node C = n.getNode( c++ );
    Node D = n.getNode( c++ );

    Edge AB = n.createAndSetEdge( A, B );
    Edge AC = n.createAndSetEdge( A, C );
    Edge BC = n.createAndSetEdge( B, C );
    Edge BD = n.createAndSetEdge( B, D );
    Edge CD = n.createAndSetEdge( C, D );

    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( n.getEdgeCapacity() );
    capacities.set( AB, 10 );
    capacities.set( AC, 2 );
    capacities.set( BC, 2 );
    capacities.set( BD, 2 );
    capacities.set( CD, 10 );

    n.setHidden( BC, true );

    MaximumFlowProblem mfp = new MaximumFlowProblem( n, capacities, n.getNode( 0 ), n.getNode( n.getNodeCapacity() - 1 ) );

    EdmondsKarp ek = new EdmondsKarp();

    ek.setProblem( mfp );
    ek.run();

    assertThat( ek.getSolution().getFlowValue(), is( equalTo( 4L )) );
    System.out.println( ek.getSolution().toString() );
    assertThat( "Check the solution.", ek.getSolution().check(), is( true ) );

    n.setHidden( BC, false );
    //ek.residualNetwork.update();

//    ek.run();
//
//    assertEquals( 6, ek.getSolution().getFlowValue() );
//    System.out.println( ek.getSolution().toString() );
//    assertEquals( "Check the solution.", true, ek.getSolution().check() );
  }

  
  @Test
  public void testInstance() {
    MaximumFlowProblem mfp = new MaximumFlowProblem( FlowTestInstances.getDiamondExample() );
    EdmondsKarp ff = new EdmondsKarp();
    ff.setProblem( mfp );

    ff.run();

    assertThat( "Flow value", ff.getSolution().getFlowValue(), is( equalTo( 3L )) );
    assertThat( "Check the solution.", ff.getSolution().check(), is( true ) );
  }

  @Test
  public void testInstanceUndirected() {
    MaximumFlowProblem mfp = new MaximumFlowProblem( FlowTestInstances.getDiamondExampleUndirected() );
    EdmondsKarp ff = new EdmondsKarp();
    ff.setProblem( mfp );

    ff.run();

    assertThat( "Flow value", ff.getSolution().getFlowValue(), is( equalTo( 0L ) ) );
    assertThat( "Check the solution.", ff.getSolution().check(), is( true ) );
  }

  @Test
  public void testHarrisRoss() {
    MaximumFlowProblem mfp = new MaximumFlowProblem( FlowTestInstances.getHarrisRossOriginal() );
    EdmondsKarp ff = new EdmondsKarp();
    ff.setProblem( mfp );

    ff.run();
    
    assertEquals( "Flow value", 163, ff.getSolution().getFlowValue() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }
}
