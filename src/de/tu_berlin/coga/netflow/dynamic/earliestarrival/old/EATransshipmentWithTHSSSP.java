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

import de.tu_berlin.coga.netflow.dynamic.transshipment.TransshipmentWithTimeHorizon;
import de.tu_berlin.coga.netflow.classic.mincost.SuccessiveShortestPath;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.netflow.ds.network.TimeExpandedNetwork;
import de.tu_berlin.coga.netflow.dynamic.problems.EarliestArrivalFlowProblem;

public class EATransshipmentWithTHSSSP extends TransshipmentWithTimeHorizon<EarliestArrivalFlowProblem> {
	public EATransshipmentWithTHSSSP() {
		setName( "Earliest Arrival Transshipment TH SSSP" );
	}

	@Override
	protected IdentifiableIntegerMapping<Edge> transshipmentWithTimeHorizon( TimeExpandedNetwork tnetwork ) {
		SuccessiveShortestPath sssp = new SuccessiveShortestPath( tnetwork, tnetwork.supplies(), tnetwork.capacities(), tnetwork.costs() );
		sssp.run();
		return sssp.getFlow();
	}

}
