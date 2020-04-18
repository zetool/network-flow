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

import org.zetool.common.datastructure.SimpleTuple;
import org.zetool.common.datastructure.Tuple;
import org.zetool.container.bucket.BucketPriorityQueue;
import org.zetool.container.bucket.BucketSet;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.MaximumFlow;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class PushRelabelHighestLabel extends PushRelabel {
	SimpleResidualGraph residualGraph;
	/** The index of the current edge for a given node in the edges array. */
	protected IdentifiableIntegerMapping<Node> current;
	/** The buckets for active nodes. */
	protected BucketPriorityQueue<Node> activeBuckets;
	/** The buckets for inactive nodes. */
	protected BucketSet<Node> inactiveBuckets;

	public class ResidualEdge extends Edge {
		ResidualEdge reverse;
		int residualCapacity;
		boolean reverseEdge;
		Edge original;
		ResidualEdge( int id, Node start, Node end, int cap, boolean reverse ) {
			super( id, start, end );
			this.residualCapacity = cap;
			reverseEdge = reverse;
		}

	}
	/**
	 * Allocates the memory for the data structures. These contain information
	 * for the nodes and edges.
	 */
	private void alloc() {
		distanceLabels = new IdentifiableIntegerMapping<>( n );	// distance
		excess = new IdentifiableIntegerMapping<>( n );	// excess
		current = new IdentifiableIntegerMapping<>( n );	// current edge datastructure (index)
		activeBuckets = new BucketPriorityQueue<>( n + 1, Node.class );
		activeBuckets.setDistanceLabels( distanceLabels );
		inactiveBuckets = new BucketSet<>( n + 1, Node.class );
		residualGraph = new SimpleResidualGraph( n, m );
	}

	@Override
	protected MaximumFlow runAlgorithm( MaximumFlowProblem problem ) {
    if( !(problem.getNetwork() instanceof DirectedGraph ) ) {
      throw new IllegalArgumentException( "Push Relabel implemented for directed graphs." );
    }
		source = getProblem().getSource();
		sink = getProblem().getSink();
		n = getProblem().getNetwork().nodeCount();
		m = getProblem().getNetwork().edgeCount();
		long start = System.nanoTime();
		alloc();
		init();
		long end = System.nanoTime();
		initTime = end-start;
		start = System.nanoTime();
		computeMaxFlow();
		end = System.nanoTime();
		phase1Time = end-start;

		//if( true ) return null;

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
		MaximumFlow f = new MaximumFlow( getProblem(), flow );

		f.check();

		return f;
	}

	/**
	 * Sets up the data structures. At first, creates reverse edges and orders the
	 * edges according to their tail nodes into the array. Thus, the incident
	 * edges to a vertex can be searched by a run through the array.
	 */
	protected void init() {
		residualGraph.init( (DirectedGraph)getProblem().getNetwork(), getProblem().getCapacities(), current );


		// set some flow on the residual network
		//residualGraph.augment( residualGraph.getEdge( 0 ), 1 );
		//residualGraph.augment( residualGraph.getEdge( 1 ), 1 );
		//residualGraph.augment( residualGraph.getEdge( 3 ), 1 );
		//residualGraph.augment( residualGraph.getEdge( 5 ), 1 );

		//excess.increase( sink, 2 );

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
				inactiveBuckets.addInactive( 0, v );
				continue;
			}
			distanceLabels.set( v, id == source.id() ? n : 1 );
      if( excess.get( v ) > 0 ) {
        activeBuckets.addActive( 1, v );
      } else if( distanceLabels.get( v ) < n ) {
        inactiveBuckets.addInactive( 1, v );
      }
		}
		activeBuckets.setdMax( 1 );
	}

	@Override
	protected void computeMaxFlow() {
		while( activeBuckets.getMaxIndex() >= activeBuckets.getMinIndex() ) {
			final Node v = activeBuckets.max();
			if( v != null ) {
				activeBuckets.removeActive( activeBuckets.getMaxIndex(), v );
				discharge( v );

				if( activeBuckets.getMaxIndex() < activeBuckets.getMinIndex() )
					break;
			}
		}
		flowValue = excess.get( sink ); // we have a max flowValue
	}

	protected void discharge( Node v ) {
		assert excess.get( v ) > 0;
		assert v.id() != sink.id();
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

				if( distanceLabels.get( v ) == n )
					break;
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
	 *
	 * @param e
	 * @return the rest excess at the start node of the edge
	 */
	protected int push( Edge e ) {
		pushes++;
		final int delta = residualGraph.getResidualCapacity( e ) < excess.get( e.start() ) ? residualGraph.getResidualCapacity( e ) : excess.get( e.start() );
		residualGraph.augment( e, delta );

		if( !e.end().equals( sink ) && excess.get( e.end() ) == 0 ) {
			// excess of a.end will be positive after the push!
			// remove j from the inactive list and put to the active list
			final int dist = distanceLabels.get( e.start() )-1;
			inactiveBuckets.deleteInactive( dist, e.end() );
			activeBuckets.addActive( dist, e.end() );
		}

		excess.decrease( e.start(), delta );
		excess.increase( e.end(), delta );

		return excess.get( e.start() );
	}

	@Override
	protected int relabel( Node v ) {
		assert excess.get( v ) > 0;

		relabels++;

		distanceLabels.set( v, n );

		final Tuple<Integer,Edge> minEdge = searchForMinDistance( v );
		if( minEdge.getU() < n ) {
			distanceLabels.set( v, minEdge.getU() );
			current.set( v, minEdge.getV().id() );
			if( activeBuckets.getdMax() < minEdge.getU() )
				activeBuckets.setdMax( minEdge.getU() );
		}
		return minEdge.getU();
	}

	protected Tuple<Integer,Edge> searchForMinDistance( Node v ) {
		int minDistance = n;
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
		}
		return new SimpleTuple<>( minDistance+1, minEdge );
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
		Node[] next = new Node[n];

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
  
  public static void main( String args[] ) {
    System.out.println( "Ford Fulkerson kompilieren bitte trololo" );
  }
}
