/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.zetool.netflow.classic.maxflow;

import org.zetool.netflow.classic.problems.RationalMaxFlowProblem;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.network.rational.RationalResidualGraph;
import org.zetool.netflow.ds.flow.RationalMaxFlow;
import org.zetool.graph.DynamicNetwork;
import org.zetool.container.mapping.IdentifiableDoubleMapping;
import java.util.Collections;
import java.util.List;
import org.zetool.common.algorithm.AbstractAlgorithm;

/**
 *
 * @author Sebastian Schenker
 */
public class Fujishige extends AbstractAlgorithm<RationalMaxFlowProblem, RationalMaxFlow> {

	private final static double EPSILON = 0.00001;

	private RationalResidualGraph resGraph;

	private double maxflowvalue;

	private void initializeDatastructures() {
		int mnodeid = 0, medgeid = 0;
		for( Node n : getProblem().getNetwork().nodes() ) {
			if( n.id() > mnodeid ) {
				mnodeid = n.id();
			}
		}
		for( Edge e : getProblem().getNetwork().edges() ) {
			if( e.id() > medgeid ) {
				medgeid = e.id();
			}
		}
		resGraph = new RationalResidualGraph( getProblem().getNetwork(), getProblem().getCapacities(), mnodeid, medgeid );
	}

	@Override
	public RationalMaxFlow runAlgorithm( RationalMaxFlowProblem problem ) {
		initializeDatastructures();
		runFujishige();
		return new RationalMaxFlow( getProblem(), getFlow() );
	}

	public RationalResidualGraph getResidualGraph() {
		return resGraph;
	}

	private void setMaxFlowValue( double val ) {
		maxflowvalue = val;
	}

	public double getMaxFlowValue() {
		return maxflowvalue;
	}

	public IdentifiableDoubleMapping<Edge> getFlow() {
		return resGraph.getFlow();
	}

	private IdentifiableDoubleMapping<Node> setBeta( Double d ) {
		IdentifiableDoubleMapping<Node> betamap = new IdentifiableDoubleMapping<Node>( getProblem().getNetwork().nodeCount() );
		for( Node n : getProblem().getNetwork().nodes() ) {
			if( n == getProblem().getSink() ) {
				betamap.set( n, d );
			} else {
				betamap.set( n, 0.0 );
			}
		}
		return betamap;
	}

	private void runFujishige() {
		double delta = 0;
		IdentifiableDoubleMapping<Node> betaMap;
		double y;
		while( true ) {
			MAordering MAord = new MAordering( getResidualGraph(), getProblem().getSource(), getProblem().getSink(), getResidualGraph().getResidualCapacities() );
			MAord.computeMAordering();

			delta = Double.MAX_VALUE;
			List<Node> ordering = MAord.getMAordering();
			ordering.remove( 0 );
			for( Node node : ordering ) {
				if( MAord.getDemand( node ) < delta ) {
					delta = MAord.getDemand( node );
				}
			}
			//System.out.println("DELTA = " + delta);
			if( delta < EPSILON ) {
				break;
			} else {
				betaMap = setBeta( delta );
			}

			Collections.reverse( ordering );
			for( Node node : ordering ) {
				for( Edge edge : MAord.getEdgeList( node ) ) {
					y = Math.min( betaMap.get( node ), resGraph.getResidualCapacities().get( edge ) );
					if( y > EPSILON ) {
						resGraph.augmentFlow( edge, y );
						betaMap.decrease( node, y );
						betaMap.increase( edge.start(), y );
					}
				}
			}

		}
		//System.out.println("FUJIOUT");
		double flowvalue = 0.0;
		for( Edge e : resGraph.getGraph().outgoingEdges( getProblem().getSource() ) ) {
			flowvalue += resGraph.getFlow().get( e );
		}
		for( Edge e : resGraph.getGraph().incomingEdges( getProblem().getSource() ) ) {
			flowvalue -= resGraph.getFlow().get( e );
		}
		setMaxFlowValue( flowvalue );

	}

	public static void main( String... args ) {
		DynamicNetwork dyn = new DynamicNetwork();

		Node source = new Node(0);
		Node sink = new Node(3);
		Node v = new Node(1);
		Node w = new Node(2);

		dyn.addNode( source );
		dyn.addNode( sink );
		dyn.addNode( v );
		dyn.addNode( w );

		Edge e1 = new Edge(0, source, v);
		Edge e2 = new Edge(1, v, sink);
		Edge e3 = new Edge(2, v, w);
		Edge e4 = new Edge(3, source, w);
		Edge e5 = new Edge(4, w, sink);

		dyn.addEdge( e1 );
		dyn.addEdge( e2 );
		dyn.addEdge( e3 );
		dyn.addEdge( e4 );
		dyn.addEdge( e5 );

		IdentifiableDoubleMapping<Edge> capacities = new IdentifiableDoubleMapping<>( 5 );
		capacities.set( e1, 1.23456 );
		capacities.set( e2, 1.002568 );
		capacities.set( e3, 1.48962 );
		capacities.set( e4, 1.135 );
		capacities.set( e5, 1.22 );


		RationalMaxFlowProblem rmfp = new RationalMaxFlowProblem(dyn, capacities, source, sink );

		Fujishige f = new Fujishige();
		f.setProblem( rmfp );

		f.run();

		RationalMaxFlow rmf = f.getSolution();

		System.out.println( f.getMaxFlowValue() );
	}

}
