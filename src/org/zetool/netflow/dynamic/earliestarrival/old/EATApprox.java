/**
 * EATApprox.java
 * Created: 02.12.2011, 15:35:02
 */
package org.zetool.netflow.dynamic.earliestarrival.old;

import org.zetool.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
import org.zetool.netflow.classic.maxflow.PushRelabel;
import org.zetool.netflow.classic.maxflow.PushRelabelHighestLabel;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.MaximumFlow;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import java.util.ArrayList;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EATApprox {
		MaximumFlowOverTimeProblem problem;
	IdentifiableIntegerMapping<Node> supplies;
	ArrayList<Integer> flowCurve = new ArrayList<>();

	public EATApprox( MaximumFlowOverTimeProblem problem, IdentifiableIntegerMapping<Node> supplies ) {
		this.problem = problem;
		this.supplies = supplies;
	}

  public ArrayList<Integer> getFlowCurve() {
    return flowCurve;
  }
  
  

	public void runAlgorithm() {
		if( problem.getSources().isEmpty() || problem.getSinks().isEmpty() ) {
			System.out.println( "TimeExpandedMaximumFlowOverTime: The problem is invalid - this should not happen!" );
			//return new PathBasedFlowOverTime();
			throw new IllegalArgumentException( "empty problem" );
		}

		// check for zero transit times
		if( problem.getTransitTimes().maximum() > 0 )
			throw new IllegalArgumentException( "Not-Zero transit times!" );

		// run the algorithm
		int sinkCount = problem.getSinks().size();
		int sourceCount = problem.getSources().size();

		int sumOfSupplies = 0;

		int lastFlow = -1;
		int totalFlow = 0;

		do {
			DefaultDirectedGraph n = new DefaultDirectedGraph( problem.getNetwork().nodeCount() + 2, problem.getNetwork().edgeCount() + sinkCount + sourceCount );
			IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( n.getEdgeCapacity() );

			int superSource = problem.getNetwork().nodeCount();
			int superSink = problem.getNetwork().nodeCount()+1;

			System.out.println( supplies.toString() );

			for( Edge e : problem.getNetwork().edges() ) {
				Edge newEdge = n.createAndSetEdge( e.start(), e.end() );
				//n.setEdgeCapacity( problem.getCapacities().get( e ) );
				capacities.set( newEdge, problem.getCapacities().get( e ) );
			}

			for( Node s : problem.getSources() ) {
				Edge newEdge = n.createAndSetEdge( n.getNode( superSource ), n.getNode( s.id() ) );
				capacities.set( newEdge, supplies.get( s ) );
			}

			for( Node s : problem.getSinks() ) {
				Edge newEdge = n.createAndSetEdge( n.getNode( s.id() ), n.getNode( superSink ) );
				capacities.set( newEdge, supplies.get( s ) * -1 );
			}

			//System.out.println( n.toString() );
			//System.out.println( capacities.toString() );

			MaximumFlowProblem maximumFlowProblem = new MaximumFlowProblem(n, capacities, n.getNode( superSource ), n.getNode( superSink ) );

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

			//System.out.println( "Flow value: " + mf.getFlowValue() );
			//System.out.println( "Sink-Arrival value: " + hipr.getFlowValue() );
			long hiprf = hipr.getFlowValue();
			lastFlow = mf.getFlowValue();

			//System.out.println( "Pushes: " + hipr.getPushes() + ", Relabels: " + hipr.getRelabels() );
			//System.out.println( "Global relabels: " + hipr.getGlobalRelabels() );
			//System.out.println( "Gaps : " + hipr.getGaps() + " Gap nodes: " + hipr.getGapNodes() );
			//System.out.println( Formatter.formatTimeUnit( end-start, TimeUnits.NanoSeconds ) );


			// reduce capacities
			for( Edge e : n.outgoingEdges( n.getNode( superSource ) ) ) {
				supplies.decrease( e.end(), mf.get( e ) );
			}

			for( Edge e : n.incomingEdges( n.getNode( superSink ) ) ) {
				supplies.increase( e.start(), mf.get( e ) );
			}

			sumOfSupplies = 0;
			for( Node s : problem.getSources() ) {
				sumOfSupplies += supplies.get( s );
			}

			//System.out.println( supplies.toString() );
			System.out.println( "Left over supplies: " + sumOfSupplies );
			//System.out.println( n.toString() );
			totalFlow += lastFlow;
			if( lastFlow != 0 )
				flowCurve.add( totalFlow );
		} while( sumOfSupplies != 0 && lastFlow != 0 );


	}

	public static void main( String... args ) {

		//EATApprox eata = new EATApprox( p, supplies );
	}
}
