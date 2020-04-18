/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
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

package org.zetool.netflow.dynamic.eatapprox;

import org.zetool.common.datastructure.Tuple;
import org.zetool.container.bucket.BucketPriorityQueue;
import org.zetool.container.bucket.BucketSet;
import org.zetool.netflow.classic.maxflow.PushRelabel;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.MaximumFlow;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.container.mapping.IdentifiableBooleanMapping;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Edge;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import org.zetool.common.datastructure.SimpleTuple;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class NetworkFlowAlgorithm extends PushRelabel {
	HidingResidualGraph residualGraph;
	/** The index of the current edge for a given node in the edges array. */
	protected IdentifiableIntegerMapping<Node> current;
	/** The buckets for active nodes. */
	protected BucketPriorityQueue<Node> activeBuckets;
	/** The buckets for inactive nodes. */
	protected BucketSet<Node> inactiveBuckets;
	protected HashSet<Node> nonActiveExcessNodes = new HashSet<>();

	protected IdentifiableBooleanMapping<Node> canReachSink;

	private final boolean verbose = false;

	/**
	 * Allocates the memory for the data structures. These contain information
	 * for the nodes and edges.
	 */
	private void alloc() {
		// nodecount hier muss der tatsächliche nodecount hin!
		distanceLabels = new IdentifiableIntegerMapping<>( n );	// distance
		excess = new IdentifiableIntegerMapping<>( n );	// excess
		current = new IdentifiableIntegerMapping<>( n );	// current edge datastructure (index)
		activeBuckets = new BucketPriorityQueue<>( n + 1, Node.class );
		activeBuckets.setDistanceLabels( distanceLabels );
		inactiveBuckets = new BucketSet<>( n + 2, Node.class );
		residualGraph = null;//new HidingResidualGraph( n, m, 0 );
	}

	@Override
	protected MaximumFlow runAlgorithm( MaximumFlowProblem problem ) {
		source = problem.getSource();
		sink = problem.getSink();

		FakeMaximumFlowProblem myProblem = (FakeMaximumFlowProblem)problem;
		n = myProblem.getNetwork().nodeCount();
		m = myProblem.getNetwork().edgeCount();

		long start = System.nanoTime();
		alloc();
		// reset node count!
		init();
		long end = System.nanoTime();
		initTime = end-start;
		start = System.nanoTime();
		computeMaxFlow();
		end = System.nanoTime();
		phase1Time = end-start;

		if( true )
			return null;

		start = System.nanoTime();
		makeFeasible();
		end = System.nanoTime();
		phase2Time = end-start;

		// compute outgoing flow
		int s = 0;
		for( int i = residualGraph.getFirst( source ); i < residualGraph.getLast( source ); ++i ) {// TODO edge iterator
			Edge e = residualGraph.getReverseEdge( residualGraph.getEdge( i ) );
			s += residualGraph.getResidualCapacity( e );
		}
		//System.out.println( "Fluss: " + s );

		IdentifiableIntegerMapping<Edge> flow = new IdentifiableIntegerMapping<>( m );
		for( Edge e : residualGraph.edges ) {
			if( residualGraph.isReverseEdge( e ) )
				continue;
			flow.set( residualGraph.originalResidualEdgeMapping.get( e ), residualGraph.getReverseResidualCapacity( e ) );
		}
		MaximumFlow f = new MaximumFlow( problem, flow );

		f.check();

		return f;
	}

	/**
	 * Run the algorithm again, but now with the new edges available.
	 */
	void run2() {
		computeMaxFlow();
	}

	private void searchForNewReachNodes( Node start ) {
		Queue<Node> queue = new LinkedList<>();
		queue.add( start );

		while(!queue.isEmpty()){
			Node node = queue.remove();
			if( verbose )
				System.out.println( "Found new reach-node: " + node );
			canReachSink.set( node, true );
			if( excess.get( node ) == 0 ) {
				if( inactiveBuckets.inactive[node.id()] == false ) {
					//System.out.println( "Node should be inactive!" );
					//inactiveBuckets.addInactive( distanceLabels.get( node ), node );
				}

			} else {
				if( activeBuckets.active[node.id()] == false ) {
					System.out.println( "Should be __active!" );
					//activeBuckets.addActive( distanceLabels.get( node ), node );
				}
			}
			// iterate over incoming edges
			IdentifiableCollection<Edge> in = residualGraph.incomingEdges( node );
			for( Edge e : in ) {
				if( !canReachSink.get( e.start() ) ) {
					queue.add( e.start() );
					canReachSink.set( e.start(), true );
				}
			}
		}
	}

	/**
	 * Sets up the data structures. At first, creates reverse edges and orders the
	 * edges according to their tail nodes into the array. Thus, the incident
	 * edges to a vertex can be searched by a run through the array.
	 */
	protected void init() {
		//residualGraph.init( getProblem().getNetwork(), getProblem().getCapacities(), current );
		FakeMaximumFlowProblem p = (FakeMaximumFlowProblem)getProblem();
		residualGraph = p.getResidualGraph();
		canReachSink = new IdentifiableBooleanMapping<>( n );
		n = residualGraph.getCurrentVisibleNodeCount(); // update node count to the actual number of visible nodes!

		current = residualGraph.current;

		// set reachable using only backward real arcs
		searchForNewReachNodes( sink );


		// later: avoid putting unreachable nodes to the active-list, still set excess! (necessary for overriden algorithm, not for "normal")

		// initialize excesses
		excess.set( source, 0 );

		for( int i = residualGraph.getFirst( source ); i < residualGraph.getLast( source ) ; ++i ) {
			Edge e = residualGraph.getEdge( i );
			if( e.end().id() != source.id() ) { // loops?
				final int delta = residualGraph.getResidualCapacity( e );
				residualGraph.augment( e, delta );
				excess.increase( e.end(), delta );
			}
		}
		pushes += residualGraph.getLast( source );

		for( Node v : getProblem().getNetwork() ) {
			final int id = v.id();
			if( id == sink.id() ) {
				distanceLabels.set( v, 0 );
        //System.out.println( v + " active" );
				inactiveBuckets.addInactive( 0, v );
				continue;
			}
			distanceLabels.set( v, id == source.id() ? n : 1 ); // nodecount hier muss der aktuelle node-count hin.
			if( excess.get( v ) > 0 ) {
        //System.out.println( v + " active" );
				activeBuckets.addActive( 1, v );
				//gotActive.add( v );
			} else if( distanceLabels.get( v ) < n )  {
        //System.out.println( v + " inactive" );
				inactiveBuckets.addInactive( 1, v );        
      } else  {
        //System.out.println( v + " nothing" );
      }
		}
		activeBuckets.setdMax( 1 );
	}

	@Override
	protected void computeMaxFlow() {
		while( activeBuckets.getMaxIndex() >= activeBuckets.getMinIndex() ) {
			final Node v = activeBuckets.max();
			if( v != null ) {
				if( v.id() == 3457 ) {
					//System.out.println( "Deactivate 3457 with distance " + activeBuckets.getMaxIndex() + " with excess " + excess.get( v ) );
				}
				activeBuckets.removeActive( activeBuckets.getMaxIndex(), v );
				//gotActive.remove( v );

				int pushesBefore = pushes;
				discharge( v );
				int pushesAfter = pushes;
				//if( pushesBefore == pushesAfter )
					//System.out.println( "NO PUSHES HAVE BEEN PERFORMED" );

				if( activeBuckets.getMaxIndex() < activeBuckets.getMinIndex() )
					break;
			}
		}
		flowValue = excess.get( sink ); // we have a max flowValue
	}

	protected void discharge( Node v ) {
		if( verbose )
			System.out.println( "Discharge for node " + v + " with excess " + excess.get( v ) );

		assert excess.get( v ) > 0;
		assert v.id() != sink.id();
		assert inactiveBuckets.inactive[v.id()] == false;
		//assert activeBuckets.active[v.id()] == true; vor dem aufruf von discharge wird active auf false gesetzt.
		do {
			int nodeDistance = distanceLabels.get( v );	// current node distance. -1 is applicable distance
			int i; // for all outarcs
			for( i = current.get( v ); i < residualGraph.getLast( v ); ++i ) {
				final Edge e = residualGraph.getEdge( i );
				if( residualGraph.getResidualCapacity( e ) > 0 && distanceLabels.get( e.end() ) == nodeDistance - 1 && push( e ) == 0 )
					break;
			}

			// all outgoing arcs are scanned now.
			if( i == residualGraph.getLast( v ) ) {
				// relabel, ended due to pointer at the last arc
				relabel( v );

				if( distanceLabels.get( v ) == n ) { // nodecount hier soll der aktuelle-node-count hin
					assert excess.get( v ) > 0;
					nonActiveExcessNodes.add( v );
					break;
				}
			} else {
				// node is no longer active
				//System.out.println( "Set current for node " + v + " to " + i + " THE ONE LINE ");
				current.set( v, i );
				// put the vertex on the inactive list
				inactiveBuckets.addInactive( nodeDistance, v );
				break;
			}
		} while( true );
	}

	/**
	 *
	 * @param e
	 * @return the rest excess at the start node of the edge
	 */
	@Override
	protected int push( Edge e ) {
		pushes++;
		final int delta = residualGraph.getResidualCapacity( e ) < excess.get( e.start() ) ? residualGraph.getResidualCapacity( e ) : excess.get( e.start() );
		if( verbose )
			System.out.println( "Push " + delta + " from " + e.start() + " to " + e.end() );
		residualGraph.augment( e, delta );

		if( !e.end().equals( sink ) && excess.get( e.end() ) == 0 ) {
			// excess of a.end will be positive after the push!
			// remove j from the inactive list and put to the active list
			final int dist = distanceLabels.get( e.start() )-1;

			try {
				Objects.requireNonNull( inactiveBuckets.buckets[dist], "Buckets distance" );


			} catch( NullPointerException ex ) {
				if( inactiveBuckets.inactive[e.end().id()]  == true ) {
					System.out.println( "Bucket is inactive, but probably has wrong distance." );
				} else {
					System.out.println( "Node is not inactive wtf." );
				}
				System.out.println( "Push " + delta + " from " + e.start() + " to " + e.end() );
			}


			inactiveBuckets.deleteInactive( dist, e.end() );
			activeBuckets.addActive( dist, e.end() );
			//gotActive.add( e.end() );
		}

		excess.decrease( e.start(), delta );
		excess.increase( e.end(), delta );

		return excess.get( e.start() );
	}

	@Override
	protected int relabel( Node v ) {
		assert excess.get( v ) > 0;

		relabels++;
		if( verbose )
			System.out.println( "Relabel. Old distance for " + v + " was " + distanceLabels.get( v ) );

		distanceLabels.set( v, n ); // nodecount hier soll der aktuelle nodecount hin

		final Tuple<Integer,Edge> minEdge = searchForMinDistance( v );
		if( minEdge.getU() < n ) {
			distanceLabels.set( v, minEdge.getU() );

			//System.out.println( "Set current for node " + v + " to " + minEdge.getV().id() );

			current.set( v, minEdge.getV().id() );
			if( activeBuckets.getdMax() < minEdge.getU() )
				activeBuckets.setdMax( minEdge.getU() );
		}
		return minEdge.getU();
	}

	protected Tuple<Integer,Edge> searchForMinDistance( Node v ) {
		int minDistance = n; // nodecount hier soll der aktuelle nodecount hin
		Edge minEdge = null;
		// search for the minimum distance value
		for( int i = residualGraph.getFirst( v ); i < residualGraph.getLast( v ); ++i ) {
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
		//System.out.println( "searchForMinDistance für " + v );

		return new SimpleTuple<>( minDistance+1, minEdge );
	}

	//HashSet<Node> gotActive = new HashSet<>();

	/**
	 * Set new (correct) distance label for the newly reachable nodes via new
	 * edges.
	 * @param newEdges
	 */
	void updateDistances( Set<Edge> newEdges ) {
		//gotActive.clear();
		int oldN = n;
		n = residualGraph.getCurrentVisibleNodeCount();

		for( Edge e : newEdges ) {
			if( canReachSink.get( e.end() ) && !canReachSink.get( e.start() ) ) {
				searchForNewReachNodes( e.start() );
			}
		}


		for( Edge e: newEdges ) {
			Node u = e.start();
			Node v = e.end(); // a new node
			int oldDist = distanceLabels.get( v ); // we set to max d(u) - 1
			int uDist = distanceLabels.get( u ) - 1;
			if( uDist > oldDist ) {
				if( verbose )
					 System.out.println( "Update distance label for node " + v + " from " + oldDist + " to " + uDist );
				// TODO: evtl. mit breitensuche nur die knoten die erreichbar sind auf inactive setzen!
				distanceLabels.set( v, uDist );
				try {
					if( !inactiveBuckets.inactive[v.id()] ) {
						//System.out.println( "Try to remove " + v + " from list! --> failed!" );
					} else {
						//System.out.println( "Remove inactive " + v + " from list!" );
						inactiveBuckets.deleteInactive( oldDist, v );
					}

				} catch( Exception ex ) {
					System.out.println( "EXCEPTION" );
					//throw ex;
				}
				if( excess.get( v ) > 0 )
					throw new IllegalArgumentException();
				inactiveBuckets.addInactive( uDist, v );

			} else {
				if( excess.get( v ) > 0 )
					throw new IllegalArgumentException();
				inactiveBuckets.addInactive( oldDist, v );

			}
			if( excess.get( u ) > 0 ) {
				if( !nonActiveExcessNodes.contains( u ) ) {
						//System.out.println( "We found a node with positive excess that is not active!" );
			//		throw new IllegalStateException( " a node with positive excess was found that is not in the set!" );
				}
				if( inactiveBuckets.inactive[u.id()] ) {
					//throw new IllegalArgumentException();
					inactiveBuckets.deleteInactive( uDist+1, u );
				}
				activeBuckets.addActive( uDist+1, u ); // udist+1? or at least, update distance!
				//gotActive.add( v );
				//System.out.println( "Adding " + u + " with distance " + (uDist+1) + " to active." );
			}
		}

		Node v = source;
		distanceLabels.set( v, n );

		for( Node n : activeBuckets.activeHash ) {
				if( inactiveBuckets.inactive[n.id()] ) {
					//throw new IllegalArgumentException();
					inactiveBuckets.deleteInactive( distanceLabels.get( n ), n );
				}
			activeBuckets.addActive( distanceLabels.get( n ), n);
		}


		if( 1 == 1 )
			return;

		for( Node v2 : nonActiveExcessNodes ) {
			if( v2.id() == 3457 ) {
				int i = 0;
				i++;
			}
			if( excess.get( v2 ) > 0 ) {
				activeBuckets.addActive( distanceLabels.get( v2 ), v2 );
				//gotActive.add( v );
				if( verbose )
					System.out.println( "Adding excess node " + v2 + " with distance " + distanceLabels.get( v2 ) + " to active." );
			} else {
				//inactiveBuckets.addInactive( distanceLabels.get( v ), v );
			}
		}
		nonActiveExcessNodes.clear();

		//inactiveBuckets.addInactive( n, v ); // the source is neither active nor inactive

		for( int i = 0; i < residualGraph.getCurrentVisibleNodeCount(); ++i ) {
			if( distanceLabels.get( residualGraph.nodes.get( i ) ) == oldN && excess.get( residualGraph.nodes.get( i ) ) == 0 )  {
				//System.out.println( "Add an inactive node: " + i );
				// todo better? some kind of bfs should be enough...
				inactiveBuckets.addInactive( oldN, residualGraph.nodes.get( i ) );
			}
		}

		// check all nodes, that they are either active or inactive
		for( int i = 0; i < residualGraph.getCurrentVisibleNodeCount(); ++i ) {
			int distance= distanceLabels.get( residualGraph.nodes.get( i ) );
			boolean active = activeBuckets.active[i];
			boolean inactive = inactiveBuckets.inactive[i];



			Node node = residualGraph.getNode( i );
			if( inactive && excess.get( residualGraph.getNode( i ) ) > 0 ) {
				System.out.println( "insert " + node );
				//if( inactiveBuckets.inactive[node.id()] ) {
					//inactiveBuckets.deleteInactive( distance, node );
				System.out.println( "BFS FAIL: " + node + " was also inactive!" );
					//activeBuckets.addActive( distance, node );
				//}
			}

			if( !(active || inactive ) ) {
				if( excess.get( node ) > 0 && !inactive ) {
					//System.out.println( "--- Time Layer: " + residualGraph.lastLayer + " - Node: " + i + " is neither active nor inactive and has excess!!!!." );
					if( !canReachSink.get( node ) ) {

						//throw new IllegalStateException( "A node with excess " + excess.get( node ) );
					} else {
						activeBuckets.addActive( distance, node );
						if( inactiveBuckets.inactive[node.id()] ) {
						}
					}

					activeBuckets.addActive( distance, residualGraph.getNode( i ) );
				} //else
					//System.out.println( "--- Time Layer: " + residualGraph.lastLayer + " - Node: " + i + " is neither active nor inactive." );
			}
		}

		// perform a BFS, only use nodes that are visible right now. start backwards
//		// from the super sink
//		Queue<Node> queue = new LinkedList<>();
//
//		queue.add( residualGraph.nodes.get( residualGraph.SUPER_SINK ) );
//
//		boolean[] bfs = new boolean[residualGraph.SUPER_SINK+1];
//		bfs[ residualGraph.SUPER_SINK] = true;
//
//		//System.out.println( "Visible nodes: " + residualGraph.getCurrentVisibleNodeCount() );
//
//		//System.out.println( "Super sink index: " + residualGraph.SUPER_SINK );
//		while( !queue.isEmpty() ) {
//			Node n = queue.poll();
//			//System.out.println( "BFS " + n );
//
//			if( excess.get( residualGraph.getNode( n.id() ) ) > 0 && n.id() != residualGraph.SUPER_SINK ) {
//				//System.out.println( "Time Layer: " + residualGraph.lastLayer + " - Node: " + n.id() + " is neither active nor inactive and has excess!!!!." );
//				int distance= distanceLabels.get( n );
//				activeBuckets.addActive( distance, n );
//				System.out.println( "BFS insert " + n );
//				if( inactiveBuckets.inactive[n.id()] ) {
//					inactiveBuckets.deleteInactive( distance, n );
//					System.out.println( "BFS FAIL: " + n + " was also inactive!" );
//				}
//			} //else
//				//System.out.println( "Time Layer: " + residualGraph.lastLayer + " - Node: " + n.id() + " is neither active nor inactive." );
//
//
//			for( int i = residualGraph.first.get( n ); i < residualGraph.last.get( n ); ++i ) {
//				Edge out = residualGraph.getEdge( i );
//				if( out.end().id() > residualGraph.getCurrentVisibleNodeCount() && out.end().id() < residualGraph.BASE_SINK )
//					//throw new IllegalStateException( "Something with edge numbers is wrong: " + out.end().id() );
//					continue;
//				Edge in = residualGraph.getReverseEdge( out );
//				if( residualGraph.getResidualCapacity( in ) > 0 && bfs[in.start().id()] != true ) {
//					//System.out.println( "BFS insert " + in.start() );
//					queue.add( in.start() );
//					bfs[in.start().id()] = true;
//				}
//			}
//		}

		//int[] potNodes = new int[]{2530, 3047, 3457, 3515, 4047, 5799, 6834, 14172, 15686, 21381, 25474, 32344, 40656, 40717, 45654, 55909, 57262, 64817, 65985};
		//int[] potNodes = new int[]{  3457,             57262};
//		int[] potNodes = new int[]{};
//
//		for( int nodeIndex : potNodes ) {
//			if( excess.get( residualGraph.getNode( nodeIndex ) ) > 0 ) {
//				activeBuckets.addActive( distanceLabels.get( residualGraph.getNode( nodeIndex ) ), residualGraph.getNode( nodeIndex ) );
//				if( inactiveBuckets.inactive[nodeIndex]) {
//					inactiveBuckets.deleteInactive( distanceLabels.get( residualGraph.getNode( nodeIndex ) ), residualGraph.getNode(  nodeIndex ) );
//				}
//			}
//
//		}
//


	}

	private static enum FeasibleState {
		Unused( 0 ),
		Active( 1 ),
		Finished( 2 );
		int val;
		FeasibleState( int value ) {
			this.val = value;
		}
	}

	@Override
	protected void makeFeasible() {
/*
   do dsf in the reverse flow graph from nodes with excess
   cancel cycles if found
   return excess flow in topological order
*/

/*
   i->d is used for dfs labels
   i->bNext is used for topological order list
   buckets[i-nodes]->firstActive is used for DSF tree
*/


	Node j,tos,bos,restart,r;
	Edge a;
  double delta;

		Node[] buckets = new Node[n];
		Node[] next = new Node[n]; // nodecount wird nur am ende aufgerufen, hier kann der tatsächliche nodecount hin?

	/* deal with self-loops */
//  forAllNodes(i) {
//    forAllArcs(i,a)
//      if ( a -> head == i ) {
//	a -> resCap = cap[a - arcs];
//      }
//  }

  /* initialize */
  tos = null;
	bos = null;
	for( Node i : getProblem().getNetwork() ) {
		distanceLabels.set( i, FeasibleState.Unused.val);
		buckets[i.id()] = null;
		if( true )
			throw new IllegalStateException( "This should not be executed!" );
		current.set( i, residualGraph.getFirst( i ) );
  }

  /* eliminate flow cycles, topologicaly order vertices */
	for( Node i : getProblem().getNetwork() ) {
    if( ( distanceLabels.get( i ) == FeasibleState.Unused.val ) && ( excess.get( i ) > 0 ) && ( i.id() != source.id() ) && ( i.id() != sink.id() ) ) {
      r = i;
			distanceLabels.set( r, FeasibleState.Active.val );
			do {
				for ( ; current.get( i ) != residualGraph.getLast( i ); current.increase( i, 1 ) ) {
					a = residualGraph.getEdge( current.get( i ) );
					if( residualGraph.isReverseEdge( a ) && residualGraph.getResidualCapacity( a ) > 0 ) {
						j = a.end();
						if( distanceLabels.get( j ) == FeasibleState.Unused.val ) {
							/* start scanning j */
							distanceLabels.set( j, FeasibleState.Active.val );
							buckets[j.id()] = i;
				      i = j;
				      break;
						}
						else
							if( distanceLabels.get( j ) == FeasibleState.Active.val ) {
								/* find minimum flow on the cycle */
								delta = residualGraph.getResidualCapacity( a );
								while ( true ) {
									delta = Math.min( delta, residualGraph.getResidualCapacity( current.get( j ) ) );
									if ( j.equals( i ) )
								    break;
									else
										j = residualGraph.getEdge( current.get( j ) ).end();
								}

								/* remove delta flow units */
								j = i;
								while ( true ) {
									a = residualGraph.getEdge( current.get( j ) );
									residualGraph.augment( a, (int)delta );
									j = a.end();
									if ( j.equals(i) )
										break;
								}

								/* backup DFS to the first saturated arc */
								restart = i;
								for( j = residualGraph.getEdge( current.get( i ) ).end(); !j.equals( i ); j = a.end() ) {
									a = residualGraph.getEdge( current.get( j ) );
									if( distanceLabels.get( j ) == FeasibleState.Unused.val || residualGraph.getResidualCapacity( a ) == 0 ) {
										distanceLabels.set( residualGraph.getEdge( current.get( j ) ).end(), FeasibleState.Unused.val );
										if( distanceLabels.get( j ) != FeasibleState.Unused.val )
											restart = j;
									}
								}

								if( !restart.equals( i ) ) {
									i = restart;
									current.increase( i, 1 );
									break;
								}
							}
					}
				}

				if( current.get( i ) == residualGraph.getLast( i ) ) {
					/* scan of i complete */
					distanceLabels.set( i, FeasibleState.Finished.val );
					if( !i.equals( source ) )
						if( bos == null ) {
							bos = i;
							tos = i;
						} else {
							next[i.id()] = tos;
							tos = i;
						}

					if( !i.equals( r ) ) {
						i = buckets[i.id()];
						current.increase( i, 1 );
					} else
						break;
				}
			} while ( true );
    }
	}


  /* return excesses */
  /* note that sink is not on the stack */
	if ( bos != null ) {
    for ( Node i = tos; !i.equals( bos ) ; i = next[i.id()] ) {
			a = residualGraph.getEdge( residualGraph.getFirst( i ) );
      while ( excess.get( i ) > 0 ) {
				if( residualGraph.isReverseEdge( a ) && residualGraph.getResidualCapacity( a ) > 0 ) {
					if( residualGraph.getResidualCapacity( a ) < excess.get( i ) )
						delta = residualGraph.getResidualCapacity( a );
					else
						delta = excess.get( i );
					residualGraph.augment( a, (int)delta );
					excess.decrease( i, (int)delta );
					excess.increase( a.end(), (int)delta );
				}
				a = residualGraph.getEdge( a.id()+1 );
      }
    }
    /* now do the bottom */
    Node i = bos;
		a = residualGraph.getEdge( residualGraph.getFirst( i ) );
		while( excess.get( i ) > 0 ) {
			if( residualGraph.isReverseEdge( a ) && residualGraph.getResidualCapacity( a ) > 0 ) {
					if( residualGraph.getResidualCapacity( a ) < excess.get( i ) )
						delta = residualGraph.getResidualCapacity( a );
					else
						delta = excess.get( i );
					residualGraph.augment( a, (int)delta );
					excess.decrease( i, (int)delta );
					excess.increase( a.end(), (int) delta );
				}
				a = residualGraph.getEdge( a.id()+1 );
    }
  }


	}

	private boolean isAdmissible( Edge e ) {
		return residualGraph.getResidualCapacity( e ) > 0 && distanceLabels.get( e.start() ) == distanceLabels.get( e.end() ) + 1;
	}
}