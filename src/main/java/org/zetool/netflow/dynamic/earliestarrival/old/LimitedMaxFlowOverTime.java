/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zetool.netflow.dynamic.earliestarrival.old;

import org.zetool.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
import org.zetool.netflow.classic.maxflow.PushRelabel;
import org.zetool.netflow.classic.maxflow.PushRelabelHighestLabel;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.network.TimeExpandedNetwork;
import org.zetool.netflow.ds.flow.MaximumFlow;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
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
	
	public void runAlgorithm() {
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

  public long getHiprf() {
    return hiprf;
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

	public long getFlow() {
		return hiprf;
	}

}
