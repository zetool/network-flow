package org.zetool.netflow.dynamic.eatapprox;

import org.zetool.common.util.Formatter;
import org.zetool.common.util.units.TimeUnits;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.structure.StaticPath;
import org.zetool.graph.traversal.BreadthFirstSearch;
import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.zetool.common.algorithm.AbstractAlgorithm;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EatPatternBFS extends AbstractAlgorithm<EarliestArrivalFlowProblem, EarliestArrivalFlowPattern> {

    EarliestArrivalFlowPatternBuilder builder = new EarliestArrivalFlowPatternBuilder();

    @Override
    protected EarliestArrivalFlowPattern runAlgorithm(EarliestArrivalFlowProblem problem) {
        EarliestArrivalFlowProblem mfot = problem;
        List<Node> sinks = new LinkedList<>();
        sinks.add(mfot.getSink());

    //mfot.setTimeHorizon( 5 );
        performTest(mfot.getNetwork(), mfot.getEdgeCapacities(), mfot.getTransitTimes(), mfot.getTimeHorizon(), mfot.getSources(), sinks, mfot.getSupplies());

        return builder.build();
    }

    public void performTest(DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, int timeHorizon, List<Node> sources, List<Node> sinks, IdentifiableIntegerMapping<Node> supplies) {
		// Create Hiding residual graph
        //1676

        System.out.println("Time horizon: " + timeHorizon);

        builder = new EarliestArrivalFlowPatternBuilder(timeHorizon + 1);

        long counterUnhide = 0;
        long counterDistances = 0;
        long counterFlow = 0;
        long counterBuild = 0;

        long start;
        long end;

        HidingResidualGraph g = new HidingResidualGraph(graph, capacities, transitTimes, timeHorizon, sources, sinks, supplies);
        start = System.nanoTime();
        g.build();
        end = System.nanoTime();
        counterBuild += (end - start);
        System.out.println(g);

//		System.out.println( "Nodes: " + g.nodes() );
//		System.out.println( "Edges: " + g.edges() );
        System.out.println("Super-Source-Index: " + g.SUPER_SOURCE);
        System.out.println("Super-Sink-Index: " + g.SUPER_SINK);
        System.out.println("Base-Source-Index: " + g.BASE_SOURCE);
        System.out.println("Base-Sink-Index: " + g.BASE_SINK);
        System.out.println("First-Node-Index: " + g.NODES);

//		for( int i = 0; i < g.nodes().size(); ++i ) {
//			if( i >= g.NODES && i < g.BASE_SINK )
//				System.out.println( "Node with ID " + i + " is on layer " + g.getLayer( i ) + " for original Node " + g.getOriginalNode( i ) );
//			else
//				System.out.println( "Node with ID " + i + " is on layer " + g.getLayer( i ) + " and is special node without corresponding original node." );
//		}
		//System.out.println( g.first );
        //System.out.println( g.last );
        int flowValue = 0;
        int totalCapacity = 0;

        for (Node source : sources) {
            totalCapacity += supplies.get(source);
        }

        int theta = 0;
//   	System.out.println( "\n\n\n-----------------------------------------------" );
// 		System.out.println( "Iteration " + theta );
        System.out.println("Starting BFS-based SSSP in iterative time expanded network");
        while (theta < timeHorizon && flowValue != totalCapacity) {
            BreadthFirstSearch bfs = new BreadthFirstSearch();
            bfs.setProblem(g);
            bfs.setStart(g.getNode(g.SUPER_SOURCE));
            bfs.setStop(g.getNode(g.SUPER_SINK));
            bfs.run();
            StaticPath path = new StaticPath(g.getNode(g.SUPER_SINK), bfs);
      //System.out.println( "Reachable: " + bfs.getReachableNodes() );
            //System.out.println( "Found path: " + path );
            int delta = Integer.MAX_VALUE;
            if (path.length() > 0) {
                for (Edge e : path) {
                    delta = Math.min(delta, g.residualCapacity.get(e));
                }
            } else {
//        System.out.println( "Flow arrived so far: " + g.residualCapacity.get( g.edges.get( g.edges.size()-1 ) ) );
//        System.out.println( 0 + " " + g.residualCapacity.get( g.edges.get( g.edges.size()-1 ) ) );
//      	System.out.println( "\n\n\n-----------------------------------------------" );
//    		System.out.println( "Iteration " + theta );
                builder.addFlowValue(g.residualCapacity.get(g.edges.get(g.edges.size() - 1)));
                theta++;
                g.activateTimeLayer(theta);
                continue;
            }
            //System.out.println( "Augmenting by " + delta );
//      System.out.println( "Augmenting " + path + " by " + delta );

            if (delta > 0) {
                for (Edge e : path) {
                    g.augment(e, delta);
                }
            }
            flowValue += delta;
        }
//  	System.out.println( "\n\n\n" );
        // maybe we have one time horizon too mauch here!
        builder.addFlowValue(g.residualCapacity.get(g.edges.get(g.edges.size() - 1)));

		// Set up instance
        //FakeMaximumFlowProblem fmfp = new FakeMaximumFlowProblem( g, g );
        //NetworkFlowAlgorithm nf = new NetworkFlowAlgorithm();
        //NetworkFlowAlgorithm nf = new NetworkFlowAlgorithmGlobalRelabelling();
        //nf.setProblem( fmfp );
//		// Run first
//		try {
//			System.out.println();
//			//printEdgeList( g );
//			System.out.println();
        //start = System.nanoTime();
        //nf.run();
        //end = System.nanoTime();
        //counterFlow += (end - start);
//		} catch( Exception e ) {
//			throw e;
//		} finally {
//			//printEdgeList( this );
//		}
//		//System.out.println( "Flow arrived so far: " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
        //System.out.println( 0 + " " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
        //builder.addFlowValue( nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
//		//if( true )
//		//	throw new IllegalStateException( "We are here where we have ap roblem" );
//
//		for( int currentTimeLayer = 1; currentTimeLayer <= timeHorizon; ++currentTimeLayer ) {
//			System.out.println( "\n\n\n-----------------------------------------------" );
//			System.out.println( "Iteration " + currentTimeLayer );
//			start = System.nanoTime();
//			Set<Edge> newEdges = g.activateTimeLayer( currentTimeLayer );
//			end = System.nanoTime();
//			counterUnhide += (end - start);
//			for( Edge e : newEdges ) {
//				//System.out.println( "New edge visible: " + e );
//			}
//
//      //nf = new NetworkFlowAlgorithm();
//      nf = new NetworkFlowAlgorithmGlobalRelabelling();
//      nf.setProblem( fmfp );
//      
//      fmfp.getResidualGraph().resetFlow();
//
//			start = System.nanoTime();
//			nf.run();
//			end = System.nanoTime();
//			counterFlow += (end - start);
//
//      System.out.println( 0 + " " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
//  		builder.addFlowValue( nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
//
//      
//			//start= System.nanoTime();
//			//nf.updateDistances( newEdges );
//			//end = System.nanoTime();
//			//counterDistances += (end-start);
////			System.out.println( Formatter.formatUnit( end-start, TimeUnits.NanoSeconds, 2 ) );
////
////			start = System.nanoTime();
////			nf.run2();
////			end = System.nanoTime();
////			counterFlow += (end - start);
////			//nf.run2();
////			//System.out.println( "Flow arrived so far: " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
////			System.out.println( currentTimeLayer + " " + nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
////			builder.addFlowValue( nf.residualGraph.residualCapacity.get( nf.residualGraph.edges.get( nf.residualGraph.edges.size()-1 ) ) );
////			//System.out.println( "\nThe distnaces are: " );
////			//for( Node n : g.nodes() )
////			//	System.out.println( n.id() + ": " + nf.distanceLabels.get( n ) );
////
////		//printEdgeList( this );
////
////			//System.out.println( "-----------------------------------------------" );
//		}
        // Manually get flow values on arcs to the sink:
        int c = 0;
        int flow = 0;
		//for( int i = g.first.get( g.nodes.get( g.BASE_SINK ) ); i < g.last.get( g.nodes.get( g.BASE_SINK ) ); ++i ) {
        //	flow += g.residualCapacity.get( g.edges.get( i ) );
        //	System.out.println( "Sink-Edges, time " + c + ": " + flow );
        //	c++;
        //}

        long total = counterBuild + counterDistances + counterFlow + counterUnhide;

        System.out.println("Residualnetz aufbauen: " + Formatter.formatUnit(counterBuild, TimeUnits.NanoSeconds, 2) + " = " + Formatter.formatPercent((double) counterBuild / total));
        System.out.println("Schichten sichtbar machen: " + Formatter.formatUnit(counterUnhide, TimeUnits.NanoSeconds, 2) + " = " + Formatter.formatPercent(((double) counterUnhide / total)));
        System.out.println("Distanzen korrigieren: " + Formatter.formatUnit(counterDistances, TimeUnits.NanoSeconds, 2) + " = " + Formatter.formatPercent(((double) counterDistances / total)));
        System.out.println("Fluss-Berechnung: " + Formatter.formatUnit(counterFlow, TimeUnits.NanoSeconds, 2) + " = " + Formatter.formatPercent(((double) counterFlow / total)));
    }

    public static void main(String... args) throws IOException {

		//EarliestArrivalFlowProblem mfot = AlgorithmTest.readFromDatFile( "../../input/flow/swiss_1_10s.dat" );
        //EarliestArrivalFlowProblem mfot = readFromDatFile( "../../input/flow/siouxfalls_5_10s-original.dat" );
        EarliestArrivalFlowProblem mfot = AlgorithmTest.testInstance();

        //EarliestArrivalApproximationAlgorithm algo = new EarliestArrivalApproximationAlgorithm();
        EatPatternBFS algo = new EatPatternBFS();

        mfot.setTimeHorizon(1677);
        algo.setProblem(mfot);
        algo.run();

        System.out.println(algo.getSolution());
        System.out.println("Runtime: " + algo.getRuntimeAsString());
    }

}
