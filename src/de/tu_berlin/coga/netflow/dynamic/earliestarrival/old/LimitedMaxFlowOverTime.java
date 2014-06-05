/**
 * LimitedMaxFlowOverTime.java
 * Created: 02.12.2011, 14:36:51
 */
package de.tu_berlin.coga.netflow.dynamic.earliestarrival.old;

import de.tu_berlin.coga.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
import de.tu_berlin.coga.netflow.classic.maxflow.PushRelabel;
import de.tu_berlin.coga.netflow.classic.maxflow.PushRelabelHighestLabel;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.network.TimeExpandedNetwork;
import de.tu_berlin.coga.netflow.ds.flow.MaximumFlow;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import java.util.ArrayList;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class LimitedMaxFlowOverTime {
	
	MaximumFlowOverTimeProblem problem;
	IdentifiableIntegerMapping<Node> supplies;
			long hiprf;

	public LimitedMaxFlowOverTime( MaximumFlowOverTimeProblem problem, IdentifiableIntegerMapping<Node> supplies ) {
		this.problem = problem;
		this.supplies = supplies;
	}
	
	protected void runAlgorithm() {
		if( problem.getSources().isEmpty() || problem.getSinks().isEmpty() ) {
			System.out.println( "TimeExpandedMaximumFlowOverTime: The problem is invalid - this should not happen!" );
			//return new PathBasedFlowOverTime();
			throw new IllegalArgumentException( "empty problem" );
		}
		
		TimeExpandedNetwork ten = new TimeExpandedNetwork( problem.getNetwork(), problem.getCapacities(), problem.getTransitTimes(), problem.getTimeHorizon(), supplies, false );

		//System.out.println( ten.toString() );

		MaximumFlowProblem maximumFlowProblem = new MaximumFlowProblem( ten, ten.capacities(), ten.singleSource(), ten.singleSink() );

		//PushRelabel hipr = new PushRelabelHighestLabelGlobalRelabelling();
		PushRelabel hipr = new PushRelabelHighestLabel();
		//PushRelabel hipr = new PushRelabelHighestLabel();
		
//		EdmondsKarp ek = new EdmondsKarp();
//		ek.setProblem( maximumFlowProblem );
//		ek.run();
//		System.out.println( ek.getSolution().getFlowValue() );
		
		hipr.setProblem( maximumFlowProblem );
		long start = System.nanoTime();
		hipr.run();
		long end = System.nanoTime();
		MaximumFlow mf = hipr.getSolution();

		System.out.println( "Flow value: " + mf.getFlowValue() );
		System.out.println( "Sink-Arrival value: " + hipr.getFlowValue() );
		hiprf = hipr.getFlowValue();
		
		//System.out.println( "Pushes: " + hipr.getPushes() + ", Relabels: " + hipr.getRelabels() );
		//System.out.println( "Global relabels: " + hipr.getGlobalRelabels() );
		//System.out.println( "Gaps : " + hipr.getGaps() + " Gap nodes: " + hipr.getGapNodes() );
		//System.out.println( Formatter.formatTimeUnit( end-start, TimeUnits.NanoSeconds ) );
	
		
		//System.out.println( "Fluss auf 12: " + mf.get( ten.getEdge( 12 ) ) );
		//System.out.println( "Fluss auf 13: " + mf.get( ten.getEdge( 13 ) ) );
		
	}
	
	public static void main( String[] args ) {
		
		DefaultDirectedGraph n = new DefaultDirectedGraph(4, 3);
		
		int timeHorizon = 3;
		
		n.createAndSetEdge( n.getNode( 0 ), n.getNode( 2 ) );
		n.createAndSetEdge( n.getNode( 0 ), n.getNode( 3 ) );
		n.createAndSetEdge( n.getNode( 1 ), n.getNode( 3 ) );
		
		IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 3 );
		capacities.set( n.getEdge( 0 ), 1 );
		capacities.set( n.getEdge( 1 ), 1 );
		capacities.set( n.getEdge( 2 ), 1 );
		
		IdentifiableIntegerMapping<Edge> transitTime = new IdentifiableIntegerMapping<>( 3 );
		transitTime.set( n.getEdge( 0 ), 2 );
		transitTime.set( n.getEdge( 1 ), 1 );
		transitTime.set( n.getEdge( 2 ), 2 );
		
		IdentifiableIntegerMapping<Node> supplies = new IdentifiableIntegerMapping<>( 4 );
		supplies.set( n.getNode( 0 ), 4 );
		supplies.set( n.getNode( 1 ), 2 );
		supplies.set( n.getNode( 2 ), -4 );
		supplies.set( n.getNode( 3 ), -2 );
		
		
		System.out.println( n.toString() );
		
		ArrayList<Node> sources = new ArrayList<>(2);
		sources.add( n.getNode( 0 ) );
		sources.add( n.getNode( 1 ) );
		
		ArrayList<Node> sinks = new ArrayList<>(2);
		sinks.add( n.getNode( 2 ) );
		sinks.add( n.getNode( 3 ) );

		MaximumFlowOverTimeProblem p = new MaximumFlowOverTimeProblem(n, capacities, transitTime, sources, sinks, timeHorizon );
		
		LimitedMaxFlowOverTime lmfot = new LimitedMaxFlowOverTime( p, supplies );
		
		lmfot.runAlgorithm();
		
	}

	long getFlow() {
		return hiprf;
	}

}
