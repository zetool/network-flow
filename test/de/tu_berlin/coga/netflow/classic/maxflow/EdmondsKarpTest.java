package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EdmondsKarpTest extends TestCase {

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

    assertEquals( 4, ek.getSolution().getFlowValue() );
    System.out.println( ek.getSolution().toString() );
    assertEquals( "Check the solution.", true, ek.getSolution().check() );

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
    DefaultDirectedGraph network = new DefaultDirectedGraph( 4, 5 );
    network.createAndSetEdge( network.getNode( 0 ), network.getNode( 1 ) );
    network.createAndSetEdge( network.getNode( 0 ), network.getNode( 2 ) );
    network.createAndSetEdge( network.getNode( 1 ), network.getNode( 2 ) );
    network.createAndSetEdge( network.getNode( 1 ), network.getNode( 3 ) );
    network.createAndSetEdge( network.getNode( 2 ), network.getNode( 3 ) );

    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 5 );
    capacities.add( network.getEdge( 0 ), 2 );
    capacities.add( network.getEdge( 1 ), 1 );
    capacities.add( network.getEdge( 2 ), 1 );
    capacities.add( network.getEdge( 3 ), 1 );
    capacities.add( network.getEdge( 4 ), 2 );

    MaximumFlowProblem mfp = new MaximumFlowProblem( network, capacities, network.getNode( 0 ), network.getNode( 3 ) );

    EdmondsKarp ek = new EdmondsKarp();

    ek.setProblem( mfp );
    ek.run();

    assertEquals( "Flow value", 3, ek.getSolution().getFlowValue() );
    System.out.println( ek.getSolution().toString() );
    assertEquals( "Check the solution.", true, ek.getSolution().check() );
  }
}
