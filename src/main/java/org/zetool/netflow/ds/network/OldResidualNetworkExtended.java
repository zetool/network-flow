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
package org.zetool.netflow.ds.network;

import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class OldResidualNetworkExtended extends OldResidualNetwork {
	IdentifiableIntegerMapping<Edge> upper;
	IdentifiableIntegerMapping<Edge> lower;

	/**
	 * A constructor for clone and overriding classes.
	 * @param initialNodeCapacity
	 * @param initialEdgeCapacity
	 */
	protected OldResidualNetworkExtended( int initialNodeCapacity, int initialEdgeCapacity ) {
		super( initialNodeCapacity, initialEdgeCapacity );
	}

	/**
	 * Creates a new residual graph, based on the specified graph, the
 zero flow and the specidied capacities.
	 * @param graph the base graph for the residual graph.
	 * @param capacities the base capacities for the residual graph.
	 */
	public OldResidualNetworkExtended( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities ) {
		super( graph, capacities );
		this.upper = capacities;
	}

	public void setLower( IdentifiableIntegerMapping<Edge> lower ) {
		this.lower = lower;
	}

	/**
	 * Updates hidden edges in the residual graph. Call this, if the graph
 has changed and another iteration of a flow algorithm should be called
	 */
	@Override
	public void update() {
		// Update residual capacities
		if( !(graph instanceof DefaultDirectedGraph) )
			return; // No hidden edges and no update!
		DefaultDirectedGraph n = (DefaultDirectedGraph)graph;
		for( Edge edge : n.allEdges() ) {
			Edge rev = allGetEdge( edge.end(), edge.start() );
			if( rev == null )
				throw new IllegalStateException( "Reverse edge is null! (Hidden?)" );
			if( n.isHidden( edge ) ) { // if an edge in original graph is hidden, hide both edges.
				setHidden( edge, true );
				setHidden( rev, true);
				//System.out.println( "Hidden edge found in update: " + edge );
			} else { // otherwise, set up residual capacities
				// Edge is normal edge!
					int cap = upper.get( edge )-flow.get( edge );
					if( cap < 0 )
						throw new IllegalStateException( "Upper capacities to small!" );
					if( upper.get( edge ) == Integer.MAX_VALUE )
						cap = Integer.MAX_VALUE;
					residualCapacities.set( edge, cap );
					if( cap == 0 ) {
						setHidden( edge, true );
				//		System.out.println( "UPDATE: Hiding edge " + edge );
					}
					else
						setHidden( edge, false );
//				if( isReverseEdge( edge ) ) {

					// reverse edge
					cap = flow.get( edge ) - (lower == null ? 0 : lower.get( edge ) );
					if( cap < 0 )
						throw new IllegalStateException( "Lower capacities to high!" );
					//flow.set( edge, cap );
					residualCapacities.set( rev, cap );
					if( cap == 0 ) {
						setHidden( rev, true );
						//System.out.println( "UPDATE: Hiding edge " + edge );
					} else
						setHidden( rev, false );

//				} else {
//				}
			}
		}

//		for( Edge edge : n.allEdges() ) {
//			Edge rev = allGetEdge( edge.end(), edge.start() );
//			if( rev == null )
//				throw new IllegalStateException( "Reverse edge is null! (Hidden?)" );
//			if( n.isHidden( edge ) ) {
//				setHidden( edge, true );
//				setHidden( rev, true);
//			} else {
//				if( residualCapacities.get( edge ) > 0 )
//					setHidden( edge, false );
//				else
//					setHidden( edge, true );
//				if( residualCapacities.get( rev ) > 0 )
//					setHidden( rev, false );
//				else
//					setHidden( rev, true );
//			}
//		}

	}

}
