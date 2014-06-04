/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tu_berlin.coga.netflow.classic.maxflow;

import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class PushRelabelHighestLabelTest extends TestCase {

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

		PushRelabelHighestLabel hipr = new PushRelabelHighestLabel();
		hipr.setProblem( mfp );
		hipr.run();

		System.out.println( "Flow: " + hipr.getSolution().getFlowValue() );
		System.out.println( hipr.getSolution().toString() );
		hipr.getSolution().check();
	}
}
