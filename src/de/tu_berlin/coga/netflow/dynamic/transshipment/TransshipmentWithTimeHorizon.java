/* zet evacuation tool copyright (c) 2007-09 zet evacuation team
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.tu_berlin.coga.netflow.dynamic.transshipment;

import de.tu_berlin.coga.netflow.dynamic.problems.DynamicTransshipmentProblem;
import algo.graph.Flags;
import de.tu_berlin.coga.netflow.dynamic.DynamicFlowAlgorithm;
import de.tu_berlin.coga.netflow.classic.maxflow.PathDecomposition;
import de.tu_berlin.coga.netflow.ds.structure.DynamicPath;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.structure.StaticPath;
import de.tu_berlin.coga.netflow.ds.network.TimeExpandedNetwork;
import de.tu_berlin.coga.netflow.ds.flow.FlowOverTime;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimePath;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlowOverTime;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlow;
import de.tu_berlin.coga.netflow.ds.structure.StaticFlowPath;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import java.util.logging.Logger;

/**
 * The class {@code TransshipmentWithTimeHorizon} provides a method to calculate
 * a dynamic transshipment with certain properties by using the time-expanded network
 * if the method to compute a adequate transshipment in the time-expanded network is overridden.
 * @param <U>
 */
public abstract class TransshipmentWithTimeHorizon<U extends DynamicTransshipmentProblem> extends DynamicFlowAlgorithm<U> {
  private static final Logger log = Logger.getGlobal();

	/**
	 * Creates a new instance of the transshipment algorithm with given network, transit times, capacities, supplies and a time horizon.
	 */
	public TransshipmentWithTimeHorizon() {
	}

	/**
	 * Abstract method that has to be overridden with the concrete transshipment algorithm.
	 * @param network The (time expanded) network the algorithm works on.
	 * @return An edge based flow.
	 */
	protected abstract IdentifiableIntegerMapping<Edge> transshipmentWithTimeHorizon( TimeExpandedNetwork network );

	@Override
	protected FlowOverTime runAlgorithm( U problem ) {
		runAlgorithm();
		return new FlowOverTime( resultFlowPathBased, getProblem() );
	}

	/**
	 * This static method computes a transshipment using the method {@link #transshipmentWithTimeHorizon}
	 * that has to be implemented by subclasses.
	 * The algorithm creates a time expanded network, calls {@link #transshipmentWithTimeHorizon} to compute
	 * a static flow and creates a dynamic flow from the result.
	 * If {@code runTransshipment} returns {@code null}
	 * this method also returns {@code null}.
	 */
	public void runAlgorithm() {
		/* Short debug output telling that a time expanded network is created. */
		if( Flags.TRANSSHIPMENT_SHORT ) {
			System.out.println( "The " + getName() + " algorithm creates a time expanded network." );
			fireEvent( "Time-Expanded Network-Creation. The " + getName() + " algorithm creates a time expanded network." );
		}

		/* Create the time expanded network up to the given time horizon. */
		TimeExpandedNetwork tnetwork = new TimeExpandedNetwork( getProblem().getNetwork(), getProblem().getEdgeCapacities(), getProblem().getTransitTimes(), getProblem().getTimeHorizon(), getProblem().getSupplies(), false );

		/* Short debug output including the size of the created expanded network. */
    log.finest( "The time expanded network was created." );
    log.finest( "It has " + tnetwork.nodes().size() + " nodes and " + tnetwork.edges().size() + " edges." );
    fireEvent( "Time-Expanded Network created. It has " + tnetwork.nodes().size() + " nodes and " + tnetwork.edges().size() + " edges." );

    /* Long debug output including the complete expanded network.*/
    log.finest( tnetwork.toString() );

		/* Progress output. */
		if( Flags.ALGO_PROGRESS )
			System.out.println( "the " + getName() + " algorithm is called.. " );
		/* Short debug output telling that the algorithm for the transshipment with time horizon is called. */
		if( Flags.TRANSSHIPMENT_SHORT && !Flags.TRANSSHIPMENT_LONG ) {
			System.out.println( "The " + getName() + " algorithm is called." );
			fireEvent( getName() + " algorithm. The " + getName() + " algorithm is called." );
		}

		/* Compute the static flow according to the specifit transshipment with time horizon. */
		IdentifiableIntegerMapping<Edge> flow = transshipmentWithTimeHorizon( tnetwork );

		/* Progress output. */
		if( Flags.ALGO_PROGRESS ) {
			System.out.print( "Progress: .. call of the " + getName() + " algorithm finished. Time horizon is " );
			if( flow == null )
				System.out.print( "not " );
			System.out.println( "sufficient." );
		}

		/* Short debug output telling whether the current time horizon was sufficient. */
		if( Flags.TRANSSHIPMENT_SHORT ) {
			System.out.print( "A time horizon of " + getProblem().getTimeHorizon() + " is " );
			if( flow == null )
				System.out.print( "not " );
			System.out.println( "sufficient." );
		}

		/* If flow==null, there does not exists a feasible static transshipment (with wished properties)
		 * and therefore there does not exist a feasible dynamic transshipment (with wished properties).*/
		if( flow == null ) {
			resultFlowPathBased = null;
			return;
		}

		/* Long debug output including the flow function of the found flow. */
		if( Flags.TRANSSHIPMENT_LONG ) {
			System.out.println();
			System.out.println( "Static transshipment as flow function:" );
			System.out.println( flow );
		}

		/* Short Debug telling that a path decomposition is calculated.*/
		if( Flags.TRANSSHIPMENT_SHORT ) {
			System.out.println();
			System.out.println( "Calculating path decomposition from sources " + tnetwork.sources() + " to sinks " + tnetwork.sinks() + "." );
		}

		/* Decompose the flow into static paths flows.*/
		PathBasedFlow decomposedFlow = PathDecomposition.calculatePathDecomposition(
						tnetwork, tnetwork.sources(), tnetwork.sinks(), flow );

		/* Long debug output containing the path flows.*/
		if( Flags.TRANSSHIPMENT_LONG ) {
			System.out.println();
			System.out.println( "Static transshipment path based:" );
			System.out.println( decomposedFlow );
		}

		/* Translating the static flow into a dynamic flow.*/
		PathBasedFlowOverTime dynamicTransshipment = new PathBasedFlowOverTime();
		for( StaticFlowPath staticPathFlow : decomposedFlow ) {
			if( staticPathFlow.getAmount() == 0 )
				continue;
			StaticPath staticPath = staticPathFlow.getPath();
			DynamicPath dynamicPath = tnetwork.translatePath( staticPath );
			// The rate of the dynamic path is the amount of the static path,
			// and the amount of the dynamic path is its rate * how long it flows,
			// but as all flows constructed in the time-expanded network have
			// length T-1, flow always flows for one time step, thus amount = rate.
			FlowOverTimePath dynamicPathFlow = new FlowOverTimePath( dynamicPath, staticPathFlow.getAmount(), staticPathFlow.getAmount() );
			dynamicTransshipment.addPathFlow( dynamicPathFlow );
		}

		if( Flags.TRANSSHIPMENT_LONG ) {
			System.out.println( "Dynamic transshipment:" );
			System.out.println( dynamicTransshipment );
		}

		resultFlowPathBased = dynamicTransshipment;
	}
}
