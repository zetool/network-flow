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
package org.zetool.netflow.ds.network.rational;

import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableDoubleMapping;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.StaticGraph;

/**
 *
 * @author Martin Groß / Sebastian Schenker
 */
public class RationalResidualGraph extends StaticGraph {

	private DirectedGraph graph;

	private int maxNodeId; //maximal id of nodes in original graph
	private int maxEdgeId; //maximal id of edges in original graph

	private IdentifiableDoubleMapping<Edge> flow;
	private IdentifiableDoubleMapping<Edge> residualCapacities;
	private IdentifiableDoubleMapping<Edge> residualTransitTimes;

	public RationalResidualGraph( DirectedGraph graph, IdentifiableDoubleMapping<Edge> capacities, int maxNodeId, int maxEdgeId ) {
		super( true, (maxNodeId + 1), (maxEdgeId + 1) * 2 );
		this.maxNodeId = maxNodeId;
		this.maxEdgeId = maxEdgeId;
		this.graph = graph;

		setNodes( graph.nodes() );
		setEdges( graph.edges() );

		for( Edge edge : graph.edges() ) {
			createEdge( edge.end(), edge.start(), edge.id() + (maxEdgeId + 1) );
		}

		flow = new IdentifiableDoubleMapping<>( graph.edgeCount() );

		residualCapacities = new IdentifiableDoubleMapping<>( graph.edgeCount() * 2 );

		for( Edge edge : edges ) {

			if( isReverseEdge( edge ) ) {
				residualCapacities.set( edge, 0 );
				changeVisibility( edge, false );

			} else {
				changeVisibility( edge, true );
				flow.set( edge, 0.0 );
				residualCapacities.set( edge, capacities.get( edge ) );
			}

		}

	}

	public void augmentFlow( Edge edge, double amount ) {

		Edge reverseEdge = getReverseEdge( edge );

		if( isReverseEdge( edge ) ) {
			flow.decrease( reverseEdge, amount );
		} else {
			flow.increase( edge, amount );
		}
		residualCapacities.decrease( edge, amount );
		residualCapacities.increase( reverseEdge, amount );
		if( eq( residualCapacities.get( edge ), 0.0 ) ) {
			//setHidden(edge, true);
			changeVisibility( edge, false );
		}
		if( less( 0.0, residualCapacities.get( reverseEdge ) ) ) {
			//setHidden(reverseEdge, false);
			changeVisibility( reverseEdge, true );
		}
	}

	public IdentifiableDoubleMapping<Edge> getResidualCapacities() {
		return residualCapacities;
	}

	public IdentifiableDoubleMapping<Edge> getResidualTransitTimes() {
		return residualTransitTimes;
	}

	public IdentifiableDoubleMapping<Edge> getFlow() {
		return flow;
	}

	public DirectedGraph getGraph() {
		return graph;
	}

	public Edge getReverseEdge( Edge edge ) {
		if( edge.id() <= maxEdgeId ) {       //changed by Sebastian: edge.id() < graph.edgeCount()
			return edges.get( edge.id() + (maxEdgeId + 1) );
		} else {
			return edges.get( edge.id() - (maxEdgeId + 1) );
		}
	}

	public boolean isReverseEdge( Edge edge ) {

		return (edge.id() > maxEdgeId); //changed by Sebastian: edge.id() >= graph.edgeCount();
	}

	public static boolean eq( double x, double y ) {
		return Math.abs( x - y ) < 10E-9;
	}

	public static boolean less( double x, double y ) {
		return x + 10E-9 < y;
	}

}
