/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
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
import de.tu_berlin.coga.netflow.dynamic.DynamicFlowAlgorithm;
import de.tu_berlin.coga.netflow.util.GraphInstanceChecker;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlowOverTime;
import de.tu_berlin.coga.netflow.ds.flow.FlowOverTime;

/**
 * The class {@code TransshipmentFramework} implements a binary search to compute the minimal
 * time horizon needed to get a transshipment with given properties. The wished transshipment
 * is defined and calculated by the generic class {@code TT} that has to inherit the
 * class {@code TransshipmentWithTimeHorizon}.
 * @param <W>
 * @param <TS>
 */
public abstract class TransshipmentFramework<W extends DynamicTransshipmentProblem, TS extends TransshipmentWithTimeHorizon<W>> extends DynamicFlowAlgorithm<W> {
  private final static boolean ALGO_PROGRESS = false;
  private final static boolean MEL = false;
  private final static boolean TRANSSHIPMENT_RESULT_FLOW = false;
  private final static boolean TRANSSHIPMENT_SHORT = false;
  private final static boolean TRANSSHIPMENT_LONG = false;
    
	/** Class type of the specific transshipment algorithm. */
	TS standardTHTAlgorithm;

	int feasibleTimeHorizon= -1;

	/** Node capacities
	 * @param standardTHTAlgorithm
	 * @param additionalTHTAlgorithm
	 */
	public TransshipmentFramework( TS standardTHTAlgorithm/*, TA additionalTHTAlgorithm*/ ) {
		this.standardTHTAlgorithm = standardTHTAlgorithm;
		//this.additionalTHTAlgorithm = additionalTHTAlgorithm;
	}

//	public void setAdditionalTHTAlgorithm( TA additionalTHTAlgorithm ) {
//		this.additionalTHTAlgorithm = additionalTHTAlgorithm;
//	}

	public void setStandardTHTAlgorithm( TS standardTHTAlgorithm ) {
		this.standardTHTAlgorithm = standardTHTAlgorithm;
	}



	/**
	 * Private method that calculates the result of the specific transshipment
	 * algorithm by creating a new instance of it, catching exceptions and
	 * running it.
	 * @param standardTranshipmentAlgorithm
	 * @return The result of the specific transshipment algorithm on the given input.
	 */
	protected PathBasedFlowOverTime useTransshipmentAlgorithm( TransshipmentWithTimeHorizon<W> standardTranshipmentAlgorithm ) {
		standardTranshipmentAlgorithm.setProblem( getProblem() );

		standardTranshipmentAlgorithm.run();

		return standardTranshipmentAlgorithm.getResultFlowPathBased();
	}

	@Override
	protected FlowOverTime runAlgorithm( W problem ) {
		runAlgorithm();
		return new FlowOverTime( resultFlowPathBased, getProblem() );
	}

	/**
	 * This method performs binary search to find a minimal time horizon.
	 * For each time step, the time-expanded network is created and the algorithm
	 * defined by the object @code{transshipmentAlgorithm} is used to check
	 * whether the time horizon is sufficient.
	 */
	public void runAlgorithm() {
		if( ALGO_PROGRESS ) {
			System.out.println( "Progress: Transshipment algorithm was started." );
			System.out.flush();
		}

		fireEvent( "Transshipment algorithm started." );
		//AlgorithmTask.getInstance().publish( "TransshipmentFramework algorithm started.", "" );

		if( MEL ) {
			System.out.println( "Eingabe: " );
			System.out.println( "Network: " + getProblem().getNetwork() );
			System.out.println( "Edge capacities:" + getProblem().getEdgeCapacities() );
			System.out.println( "Supplies: " + getProblem().getSupplies() );
		}

		if( GraphInstanceChecker.emptySupplies( getProblem().getNetwork(), getProblem().getSupplies() ) ) {
			if( MEL )
				System.out.println( "No individuals - no flow." );
			resultFlowPathBased = new PathBasedFlowOverTime();
			return;
		}

		/* Calculate an upper bound for the time horizon. */
		int upperBound;
		upperBound = TransshipmentBoundEstimator.calculateBound( getProblem().getNetwork(), getProblem().getTransitTimes(), getProblem().getEdgeCapacities(), getProblem().getSupplies() );

		/* Short debug output telling the computed upper bound. */
		if( ALGO_PROGRESS )
			System.out.println( "Progress: The upper bound for the time horizon was calculated." );
		if( TRANSSHIPMENT_SHORT )
			System.out.println( "Upper bound for time horizon: " + (upperBound - 1) );

		/* Initialization */
		int left = 1, right = upperBound;
		PathBasedFlowOverTime transshipmentWithoutTimeHorizon = null;

		/* Do geometric search: */

		if( ALGO_PROGRESS )
			System.out.println( "Progress: Now testing time horizon 1." );
		//AlgorithmTask.getInstance().publish( "Uppder bound for the time horizon was calculated.", "Now testing time horizon 1." );
		fireEvent( "Upper bound for the time horizon was calculated. Now testing time horizon 1." );
		getProblem().setTimeHorizon( 1 );
		/* Use the specific transshipment algorithm to check whether testTimeHorizon is sufficient. */
		PathBasedFlowOverTime dynamicTransshipment = useTransshipmentAlgorithm( standardTHTAlgorithm );

		boolean found = false;
		int nonFeasibleT = 0;
		int feasibleT = -1;

		if( dynamicTransshipment == null )
			nonFeasibleT = 1;
		else {
			nonFeasibleT = 0;
			feasibleT = 1;
			found = true;
		}

		while( !found ) {
			int testTimeHorizon = (nonFeasibleT * 2);
			if( testTimeHorizon >= upperBound ) {
				feasibleT = upperBound;
				found = true;
			} else {
				if( ALGO_PROGRESS )
					System.out.println( "Progress: Now testing time horizon " + testTimeHorizon + "." );
				//AlgorithmTask.getInstance().publish( "Now testing time horizon " + testTimeHorizon + ".", "" );
				fireEvent( "Now testing time horizon " + testTimeHorizon + "." );
				System.out.println( System.currentTimeMillis() + " ms" );
				getProblem().setTimeHorizon( testTimeHorizon );
				dynamicTransshipment = useTransshipmentAlgorithm( standardTHTAlgorithm );
				if( dynamicTransshipment == null )
					nonFeasibleT = testTimeHorizon;
				else {
					feasibleT = testTimeHorizon;
					found = true;
				}
			}
		}

		left = nonFeasibleT;
		right = Math.min( feasibleT + 1, upperBound );

		/* Do binary search: */
		do {

			/* Compute the middle of the search intervall. */
			int testTimeHorizon = (left + right) / 2;

			if( ALGO_PROGRESS )
				System.out.println( "Progress: Now testing time horizon " + testTimeHorizon + "." );
			//AlgorithmTask.getInstance().publish( "Now testing time horizon " + testTimeHorizon + ".", "" );
			fireEvent( "Now testing time horizon " + testTimeHorizon + "." );

			getProblem().setTimeHorizon( testTimeHorizon );

			/* Use the specific transshipment algorithm to check whether testTimeHorizon is sufficient. */
			dynamicTransshipment = useTransshipmentAlgorithm( standardTHTAlgorithm );

			/* If the time horizon is sufficient, adjust left border, else adjust right border of the intervall.*/
			if( dynamicTransshipment == null )
				left = testTimeHorizon;
			else {
				right = testTimeHorizon;
				transshipmentWithoutTimeHorizon = dynamicTransshipment;
			}


		} while( left < right - 1 ); /* Stop if the borders reach each other. */

		/* If a transshipment was found print the result. */
		if( left == right - 1 && transshipmentWithoutTimeHorizon != null ) {
			this.feasibleTimeHorizon = right;
			if( ALGO_PROGRESS )
				System.out.println( "Progress: Transshipment algorithm has finished. Time horizon: " + right );
			//AlgorithmTask.getInstance().publish("Solution found.", "The optimal time horizon is: " + right +" (estimated upper bound: "+(upperBound-1)+")");
			fireEvent( "Solution found. The optimal time horizon is: " + right + " (estimated upper bound: " + (upperBound - 1) + ")" );
			if( TRANSSHIPMENT_SHORT )
				System.out.println( "The optimal time horizon is: " + right + " (estimated upper bound: " + (upperBound - 1) + ")" );
			if( TRANSSHIPMENT_LONG ) {
				System.out.println( "A transshipment with time horizon (" + (upperBound - 1) + ")" + +right + ": " );
				System.out.println( transshipmentWithoutTimeHorizon );
			}
			if( TRANSSHIPMENT_RESULT_FLOW )
				System.out.println( transshipmentWithoutTimeHorizon );
		} else {
			//AlgorithmTask.getInstance().publish("No solution found.","");
			fireEvent( "No solution found." );
			if( TRANSSHIPMENT_SHORT )
				System.out.println( "No solution found." );
			if( ALGO_PROGRESS )
				System.out.println( "Progress: Transshipment algorithm has finished. No solution." );
			throw new AssertionError( "No solution found. Upper bound wrong?" );
		}

//		/* if an additional algorithm was set, it is applied for the optimal time horizon.
//		 * The new flow is than the result flow. */
//		if( left == right - 1 && transshipmentWithoutTimeHorizon != null )
//			if( additionalTHTAlgorithm != null && additionalTHTAlgorithm != standardTHTAlgorithm ) {
//				transshipmentWithoutTimeHorizon = useTransshipmentAlgorithm( additionalTHTAlgorithm );
//				if( TRANSSHIPMENT_SHORT )
//					System.out.println( "Additional run with additional transshipment algorithm has finished." );
//				if( ALGO_PROGRESS )
//					System.out.println( "Progress: Additional transshipment algorithm has finished and the new solution was set." );
//				//AlgorithmTask.getInstance().publish( 100, "Run with additional transshipment algorithm has finished.", "The new solution was set." );
//				fireProgressEvent( 100, "Run with additional transshipment algorithm has finished. The new solution was set." );
//			}

		/* May be null if upperBound was not sufficient. This should not happen!
		 * Else the optimal transshipment is saved. */
		resultFlowPathBased = transshipmentWithoutTimeHorizon;
	}

	public int getFeasibleTimeHorizon() {
		return feasibleTimeHorizon;
	}


}
