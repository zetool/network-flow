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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zetool.netflow.dynamic.earliestarrival.old;

import org.zetool.netflow.dynamic.transshipment.DynamicTransshipment;
import org.zetool.netflow.dynamic.transshipment.TransshipmentFramework;
import org.zetool.netflow.ds.flow.FlowOverTime;
import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;

/**
 * The class {@code EATransshipment} solves two variants
 * of the earliest arrival transshipment problem: with or
 * without a given time horizon.
 * The implementation is done with time-expanded networks.
 * For the variant without a time horizon, binary search
 * is used.
 */
public class EATransshipmentSSSP extends TransshipmentFramework<EarliestArrivalFlowProblem, DynamicTransshipment<EarliestArrivalFlowProblem>> {
	public EATransshipmentSSSP() {
		super( new DynamicTransshipment<>()/*, new EATransshipmentWithTHSSSP()*/ );
	}

	@Override
	protected FlowOverTime runAlgorithm( EarliestArrivalFlowProblem problem ) {
		FlowOverTime transshipmentWithoutTimeHorizon = super.runAlgorithm( problem );

		if( getFeasibleTimeHorizon() > -1 && transshipmentWithoutTimeHorizon != null ) {
			EATransshipmentWithTHSSSP eat = new EATransshipmentWithTHSSSP();
			problem.setTimeHorizon( getFeasibleTimeHorizon() );
			eat.setProblem( problem );

			eat.run();
			transshipmentWithoutTimeHorizon = eat.getSolution();
			System.out.println( "Progress: Additional transshipment algorithm has finished and the new solution was set." );
			fireProgressEvent( 1, "Run with additional transshipment algorithm has finished. The new solution was set." );
		}
		return transshipmentWithoutTimeHorizon;
	}
}
