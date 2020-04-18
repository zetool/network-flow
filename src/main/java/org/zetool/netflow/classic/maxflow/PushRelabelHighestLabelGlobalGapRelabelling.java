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
package org.zetool.netflow.classic.maxflow;

import org.zetool.graph.Edge;
import org.zetool.graph.Node;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class PushRelabelHighestLabelGlobalGapRelabelling extends PushRelabelHighestLabelGlobalRelabelling {
	int gaps;
	int gapNodes;

	@Override
	protected void discharge( Node v ) {
		assert excess.get( v ) > 0;
		assert v.id() != sink.id();
		do {
			final int nodeDistance = distanceLabels.get( v );	// current node distance. -1 is applicable distance

			int i;	// for all outarcs
			for( i = current.get( v ); i < residualGraph.getLast( v ); ++i ) {
				final Edge e = residualGraph.getEdge( i );
				// if is applicable, push. break if no excess is left over
				if( residualGraph.getResidualCapacity( e ) > 0 && distanceLabels.get( e.end() ) == nodeDistance - 1 && push( e ) == 0 )
					break;
			}

			// all outgoing arcs are scanned now.
			if( i == residualGraph.getLast( v ) ) {
				// relabel, ended due to pointer at the last arc
				relabel( v );

				if( distanceLabels.get( v ) == n )
					break;

				if( activeBuckets.get( nodeDistance ) == null && inactiveBuckets.get( nodeDistance ) == null )
					gap( nodeDistance );

				if( distanceLabels.get( v ) == n )
					throw new IllegalStateException( "here, somehow should be a break" );
			} else {
				// node is no longer active
				current.set( v, i );
				// put the vertex on the inactive list
				inactiveBuckets.addInactive( nodeDistance, v );
				break;
			}
		} while( true );
	}

	/**
	 * Gap relabeling (maybe move to bucket?)
	 * @param emptyBucket
	 * @return the gap value of an empty bucket for the gap heuristic
	 */
	protected int gap( int emptyBucket ) {
		gaps++;
		int r = emptyBucket - 1;
		int cc;

		/* set labels of nodes beyond the gap to "infinity" */
		for( int l = emptyBucket + 1; l <= activeBuckets.getdMax(); l++ ) {
			// TODO iterator
			for( Node node = inactiveBuckets.get( l ); node != null; node = inactiveBuckets.next( node ) ) {
				distanceLabels.set( node, n );
				gapNodes++;
			}
			// TODO change somehow...
			inactiveBuckets.set( l, null );
		}
		cc = (activeBuckets.getMinIndex() > r) ? 1 : 0;
		activeBuckets.setMaxIndex( r );
		return cc;
	}

	public int getGapNodes() {
		return gapNodes;
	}

	public int getGaps() {
		return gaps;
	}
}
