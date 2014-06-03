/**
 * EarliestArrivalApproximationAlgorithm.java
 * Created: 23.01.2014, 10:56:44
 */
package de.tu_berlin.coga.netflow.dynamic.eatapprox;

import de.tu_berlin.coga.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import de.tu_berlin.coga.common.algorithm.Algorithm;
import de.tu_berlin.coga.common.util.Formatter;
import de.tu_berlin.coga.common.util.units.TimeUnits;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.network.NetworkInterface;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EarliestArrivalApproximationAlgorithm extends Algorithm<EarliestArrivalFlowProblem, EarliestArrivalFlowPattern> {

	EarliestArrivalFlowPatternBuilder builder = new EarliestArrivalFlowPatternBuilder();

	@Override
	protected EarliestArrivalFlowPattern runAlgorithm( EarliestArrivalFlowProblem problem ) {

		EarliestArrivalFlowProblem mfot = problem;
		List<Node> sinks = new LinkedList<>();
		sinks.add( mfot.getSink() );

		performTest( mfot.getNetwork(), mfot.getEdgeCapacities(), mfot.getTransitTimes(), mfot.getTimeHorizon(), mfot.getSources(), sinks, mfot.getSupplies() );

		return builder.build();
	}

	public void performTest( NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, int timeHorizon,  List<Node> sources, List<Node> sinks, IdentifiableIntegerMapping<Node> supplies ) {
		// Create Hiding residual graph
		//1676

		builder = new EarliestArrivalFlowPatternBuilder( timeHorizon+1 );

		long counterUnhide = 0;
		long counterDistances = 0;
		long counterFlow = 0;
		long counterBuild = 0;

		long start;
		long end;

		HidingResidualGraph g = new HidingResidualGraph(network, capacities, transitTimes, timeHorizon, sources, sinks, supplies );
		start = System.nanoTime();
		g.build();
		end = System.nanoTime();
		counterBuild += (end - start);
		System.out.println( g );

//		System.out.println( "Nodes: " + g.nodes() );
//		System.out.println( "Edges: " + g.edges() );
		System.out.println( "Super-Source-Index: " + g.SUPER_SOURCE );
		System.out.println( "Super-Sink-Index: " + g.SUPER_SINK );
		System.out.println( "Base-Source-Index: " + g.BASE_SOURCE );
		System.out.println( "Base-Sink-Index: " + g.BASE_SINK );
		System.out.println( "First-Node-Index: " + g.NODES );

//		for( int i = 0; i < g.nodes().size(); ++i ) {
//			if( i >= g.NODES && i < g.BASE_SINK )
//				System.out.println( "Node with ID " + i + " is on layer " + g.getLayer( i ) + " for original Node " + g.getOriginalNode( i ) );
//			else
//				System.out.println( "Node with ID " + i + " is on layer " + g.getLayer( i ) + " and is special node without corresponding original node." );
//		}

		//System.out.println( g.first );
		//System.out.println( g.last );

		// Set up instance
		FakeMaximumFlowProblem fmfp = new FakeMaximumFlowProblem( g, g );
		//NetworkFlowAlgorithm nf = new NetworkFlowAlgorithm();
		NetworkFlowAlgorithm nf = new NetworkFlowAlgorithmGlobalRelabelling();
		nf.setProblem( fmfp );

		// Run first
		try {
			System.out.println();
			//printEdgeList( g );
			System.out.println();
			start = System.nanoTime();
			nf.run();
			end = System.nanoTime();
			counterFlow += (end - start);
		} catch( Exception e ) {
			throw e;
		} finally {
			//printEdgeList( this );
		}
		//System.out.println( "Flow arrived so far: " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
		System.out.println( 0 + " " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
		builder.addFlowValue( nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
		//if( true )
		//	throw new IllegalStateException( "We are here where we have ap roblem" );

		for( int currentTimeLayer = 1; currentTimeLayer <= timeHorizon; ++currentTimeLayer ) {
			//System.out.println( "\n\n\n-----------------------------------------------" );
			//System.out.println( "Iteration " + currentTimeLayer );
			start = System.nanoTime();
			Set<Edge> newEdges = g.activateTimeLayer( currentTimeLayer );
			end = System.nanoTime();
			counterUnhide += (end - start);
			for( Edge e : newEdges ) {
			//	System.out.println( "New edge visible: " + e );
			}


			start= System.nanoTime();
			nf.updateDistances( newEdges );
			end = System.nanoTime();
			counterDistances += (end-start);
//			System.out.println( Formatter.formatUnit( end-start, TimeUnits.NanoSeconds, 2 ) );

			start = System.nanoTime();
			nf.run2();
			end = System.nanoTime();
			counterFlow += (end - start);
			//nf.run2();
			//System.out.println( "Flow arrived so far: " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
			System.out.println( currentTimeLayer + " " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
			builder.addFlowValue( nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
			//System.out.println( "\nThe distnaces are: " );
			//for( Node n : g.nodes() )
			//	System.out.println( n.id() + ": " + nf.distanceLabels.get( n ) );

		//printEdgeList( this );

			//System.out.println( "-----------------------------------------------" );
		}

		// Manually get flow values on arcs to the sink:
		int c = 0;
		int flow = 0;
		//for( int i = g.first.get( g.nodes.get( g.BASE_SINK ) ); i < g.last.get( g.nodes.get( g.BASE_SINK ) ); ++i ) {
		//	flow += g.residualCapacity.get( g.edges.get( i ) );
		//	System.out.println( "Sink-Edges, time " + c + ": " + flow );
		//	c++;
		//}

		long total = counterBuild + counterDistances + counterFlow + counterUnhide;

		System.out.println( "Residualnetz aufbauen: " + Formatter.formatUnit( counterBuild, TimeUnits.NanoSeconds, 2 )  + " = " + Formatter.formatPercent((double)counterBuild/total) );
		System.out.println( "Schichten sichtbar machen: " + Formatter.formatUnit( counterUnhide, TimeUnits.NanoSeconds, 2 )  + " = " + Formatter.formatPercent(((double)counterUnhide/total)) );
		System.out.println( "Distanzen korrigieren: " + Formatter.formatUnit( counterDistances, TimeUnits.NanoSeconds, 2 )  + " = " + Formatter.formatPercent( ( (double)counterDistances/total) ) );
		System.out.println( "Fluss-Berechnung: " + Formatter.formatUnit( counterFlow, TimeUnits.NanoSeconds, 2 )  + " = " + Formatter.formatPercent(((double)counterFlow/total)) );
	}

	private static void printEdgeList( HidingResidualGraph g ) {
		for( int i = 0; i < g.edges().size(); ++i ) {
			StringBuilder outLine = new StringBuilder();
			//System.out.println( t.g.edges().get( i ) );
			Edge e = g.edges().get( i );

			System.out.println( "Edge " + i + " with id " + e.id() );

			assert e.id() == i;

			outLine.append( e );
			Node v = e.start();
			Node w = e.end();

			outLine.append( " Target Layer " ).append( g.getLayer( w.id() ) ).append( ". " );

			outLine.append( " Residual capacity: " + g.getResidualCapacity( i ) + " - " );

			if( g.first.get( v ) == e.id() )
				outLine.append( " first " );
			if( g.last.get( v ) == e.id() + 1 )
				outLine.append( " last " );

			System.out.println( outLine );
		}
	}
}
