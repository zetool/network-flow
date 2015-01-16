
package org.zetool.netflow.classic.maxflow;

import org.zetool.graph.traversal.BreadthFirstSearch;
import org.zetool.graph.structure.StaticPath;


/**
 * An implementation of the algorithm of Edmonds and Karp. Successively flow is
 * augmented along shortest s-t-paths.
 *
 * Warning: there is a bug in here, on some instances, the result is not correct.
 * @author Jan-Philipp Kappmeier
 */
public class EdmondsKarp extends FordFulkerson {

	/**
	 * Finds a shortest path such that the Ford and Fulkerson algorithm runs in
	 * polynomial time.
	 * @return a shortest path in the residual network
	 */
	@Override
	protected StaticPath findPath() {
    BreadthFirstSearch bfs = new BreadthFirstSearch();
    bfs.setProblem( residualNetwork );
    bfs.setStart( source );
    bfs.setStop( sink );
    bfs.run();
		return new StaticPath( bfs );
	}
}