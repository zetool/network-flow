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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.zetool.netflow.dynamic.eatapprox;

import org.zetool.common.datastructure.Tuple;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import java.util.Set;
import org.zetool.common.datastructure.SimpleTuple;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class NetworkFlowAlgorithmGlobalRelabelling extends NetworkFlowAlgorithm {


	protected boolean useBugHeuristic = false;
	protected int globalRelabels;
	protected int nm;
	protected int globalRelabelThreshold;
	protected int relabelsSinceLastGlobalRelabel;
	boolean performGlobalRelabel = true;

	private static enum States {
		inactiveFirst,
		inactiveNext,
		activeFirst,
		activeNext;
	}

	// nm muss neu gesetzt werden!

	@Override
	protected void init() {
		super.init();
		m = residualGraph.getCurrentVisibleEdgeCount();
		System.out.println( "Visible Edges: " + m );
		nm = 6 * n + m;
		globalRelabelThreshold = (int) 0.5 * nm;
	}

	@Override
	protected void computeMaxFlow() {
		while( activeBuckets.getMaxIndex() >= activeBuckets.getMinIndex() ) {
			final Node v = activeBuckets.max();
			if( v == null ) {
				if( activeBuckets.getMaxIndex() < activeBuckets.getMinIndex() && useBugHeuristic )
					globalUpdate();
			} else {
				activeBuckets.removeActive( activeBuckets.getMaxIndex(), v );
				//gotActive.remove( v );
//				if( v.id() == 3457 ) {
//					System.out.println( "Deactivate 3457 with distance " + activeBuckets.getMaxIndex() + " with excess " + excess.get( v ) );
//				}

				discharge( v );
//				if( v.id() == 3457 ) {
//					System.out.println( "After discharge left with excess " + excess.get( v ) );
//				}


				if( activeBuckets.getMaxIndex() < activeBuckets.getMinIndex() )
					break;

				/* is it time for global update? */
				if( relabelsSinceLastGlobalRelabel > nm )
					globalUpdate();
			}
		}
		// we have a max flowValue
		flowValue = excess.get( sink );
	}

	@Override
	protected int relabel( Node v ) {
		relabelsSinceLastGlobalRelabel += 12;
		return super.relabel( v );
	}

	@Override
	protected Tuple<Integer,Edge> searchForMinDistance( Node v ) {
		int minDistance = n;
		Edge minEdge = null;
		// search for the minimum distance value
		for( int i = useBugHeuristic ? current.get( v ) : residualGraph.getFirst( v ); i < residualGraph.getLast( v ); ++i ) {
			relabelsSinceLastGlobalRelabel++;
			final Edge e = residualGraph.getEdge( i );

			if( residualGraph.getResidualCapacity( e ) > 0 ) {
				if( minEdge == null && distanceLabels.get( e.end() ) <= minDistance ) {
					minEdge = e;
					minDistance = distanceLabels.get( e.end() );
				} else if ( distanceLabels.get( e.end() ) < minDistance ) {
					minDistance = distanceLabels.get( e.end() );
					minEdge = e;
				}
			}

//			if( residualGraph.getResidualCapacity( e ) > 0 && distanceLabels.get( e.end() ) < minDistance ) {
//				minDistance = distanceLabels.get( e.end() );
//				minEdge = e;
//			}

		}
		return new SimpleTuple<>( minDistance+1, minEdge );
	}


	private void initGlobalUpdate() {
		inactiveBuckets.reset( activeBuckets.getdMax() );
		activeBuckets.reset();

		// all node distances to n
		for( Node node : getProblem().getNetwork().nodes() )
			distanceLabels.set( node, n );
		// Visible nodes
		//for( int i = 0; i < residualGraph.getCurrentVisibleNodeCount(); ++i ) {
		//	distanceLabels.set( residualGraph.nodes.get( i ), n );
		//}
		// super sinks:
		for( int i = residualGraph.BASE_SINK; i < residualGraph.SUPER_SINK; ++i ) {
			distanceLabels.set( residualGraph.nodes.get( i ), n );
		}
		distanceLabels.set( sink, 0 );

		inactiveBuckets.addInactive( 0, sink );
	}

	//LinkedList<Node> toAdd = new LinkedList<>();
	//HashSet<Node> toAdd = new HashSet<>();

	protected void globalUpdate() {
		//System.out.println( "\n\nGlobal Update" );
		relabelsSinceLastGlobalRelabel = 0;
		globalRelabels++;

		boolean[] formerActive = new boolean[n];
		boolean[] newActive = new boolean[n];

		for( int i = 0; i < n; ++i ) {
			if( activeBuckets.active[i] )
				formerActive[i] = true;
		}

		initGlobalUpdate();

		for( int curDist = 0; true; curDist++ ) {
			final int curDistPlusOne = curDist+1;
			if( activeBuckets.get( curDist ) == null && inactiveBuckets.get( curDist ) == null )
				break;

			Node node = null;
			States nextNodeState = States.inactiveFirst; // which type is the next node.
			while( true ) {
				switch( nextNodeState ) {
					case inactiveFirst:
						node = inactiveBuckets.get( curDist );
						nextNodeState = States.inactiveNext;
						break;
					case inactiveNext:
						node = inactiveBuckets.next( node );
						break;
					case activeFirst:
						node = activeBuckets.get( curDist );
						nextNodeState = States.activeNext;
						break;
					case activeNext:
						node = activeBuckets.next( node );
						break;
					default:
						throw new AssertionError( nextNodeState );
				}

				if( node == null )
					if( nextNodeState == States.inactiveNext ) {
						nextNodeState = States.activeFirst;
						continue;
					} else {
						assert nextNodeState == States.activeNext : nextNodeState;
						break;
					}

				// scanning arcs incoming to a node (these are reverse arcs from outgoing arcs)
				for( int i = residualGraph.getFirst( node ); i < residualGraph.getLast( node ); ++i ) {
					final Edge a = residualGraph.getEdge( i );
					if( residualGraph.getResidualCapacity( residualGraph.getReverseEdge( a ) ) > 0 ) {
						final Node j = a.end();
						//System.out.println( "Node update for " + j );
						if( distanceLabels.get( j ) == n ) {
							if( !canReachSink.get( j ) ) {
								// let this node to be at max level because it is useless
								inactiveBuckets.addInactive( n, j );
								continue;
							}

							distanceLabels.set( j, curDistPlusOne );
							//System.out.println( "Update Distance for " + j + " to " + curDistPlusOne );
							current.set( j, residualGraph.getFirst( j ) );
							if( curDistPlusOne > activeBuckets.getdMax() )
								activeBuckets.setdMax( curDistPlusOne );
							if( excess.get( j ) > 0 ) { // put into active list {
								//System.out.println( "Adding " + j + " to active" );
								activeBuckets.addActive( curDistPlusOne, j );
								//gotActive.remove( j );
								newActive[j.id()] = true;
							} else { // put into inactive list
								//System.out.println( "Adding " + j + " to in_active" );
								inactiveBuckets.addInactive( curDistPlusOne, j);
							}
						}
					}
				}
			}
		}
		for( int i = 0; i < n; ++i ) {
			if( formerActive[i] && !newActive[i] ) {
				//if( i == 3457 )
				//	System.out.println( "----- Node " + i + " was active but is not any more." );
				//toAdd.add( residualGraph.nodes.get( i) );
			}
		}
		//for( Node n : gotActive ) {
			//toAdd.add( n );
		//}
		//gotActive.clear();

		int i = 3;
		i++;
	}

	public int getGlobalRelabels() {
		return globalRelabels;
	}

	@Override
	void updateDistances( Set<Edge> newEdges ) {
		super.updateDistances( newEdges ); //To change body of generated methods, choose Tools | Templates.
		nm = 6 * n + m;
		globalRelabelThreshold = (int) 0.5 * nm;
		// set distances correct!
		globalUpdate(); // this is necessary for correctness!

//		for( Node n : toAdd ) {
//			if( !activeBuckets.active[n.id()] && excess.get( n ) > 0 ) {
//				activeBuckets.addActive( distanceLabels.get( n ), n );
//			}
//		}
//
//		toAdd.clear();

	}
}
