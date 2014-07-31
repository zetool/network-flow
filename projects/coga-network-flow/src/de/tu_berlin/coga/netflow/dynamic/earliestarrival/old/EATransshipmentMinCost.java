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
package de.tu_berlin.coga.netflow.dynamic.earliestarrival.old;

import de.tu_berlin.coga.netflow.dynamic.transshipment.DynamicTransshipment;
import de.tu_berlin.coga.netflow.dynamic.problems.DynamicTransshipmentProblem;
import de.tu_berlin.coga.netflow.dynamic.transshipment.TransshipmentFramework;
import de.tu_berlin.coga.netflow.ds.flow.FlowOverTime;

/**
 * The class {@code EATransshipment} solves two variants
 * of the earliest arrival transshipment problem: with or
 * without a given time horizon.
 * The implementation is done with time-expanded networks.
 * For the variant without a time horizon, binary search
 * is used.
 */
public class EATransshipmentMinCost extends TransshipmentFramework<DynamicTransshipmentProblem,DynamicTransshipment<DynamicTransshipmentProblem>/*,EATransshipmentWithTHMinCost*/> {
    public EATransshipmentMinCost() {
			super( new DynamicTransshipment()/*, new EATransshipmentWithTHMinCost() */);
			setName( "Earliest Arrival Transshipment TH MinCost" );
		}

	@Override
	protected FlowOverTime runAlgorithm( DynamicTransshipmentProblem problem ) {
		FlowOverTime transshipmentWithoutTimeHorizon = super.runAlgorithm( problem );

		/* if an additional algorithm was set, it is applied for the optimal time horizon.
		 * The new flow is than the result flow. */
		if( getFeasibleTimeHorizon() > -1 && transshipmentWithoutTimeHorizon != null ) {
			EATransshipmentWithTHMinCost eat = new EATransshipmentWithTHMinCost();
			problem.setTimeHorizon( getFeasibleTimeHorizon() );
			eat.setProblem( problem );

			eat.run();
			transshipmentWithoutTimeHorizon = eat.getSolution();

				//transshipmentWithoutTimeHorizon = useTransshipmentAlgorithm( eat );
					System.out.println( "Progress: Additional transshipment algorithm has finished and the new solution was set." );
				//AlgorithmTask.getInstance().publish( 100, "Run with additional transshipment algorithm has finished.", "The new solution was set." );
				fireProgressEvent( 1, "Run with additional transshipment algorithm has finished. The new solution was set." );
		}
			//if( additionalTHTAlgorithm != null && additionalTHTAlgorithm != standardTHTAlgorithm ) {
			//}
		return transshipmentWithoutTimeHorizon;
	}


}
