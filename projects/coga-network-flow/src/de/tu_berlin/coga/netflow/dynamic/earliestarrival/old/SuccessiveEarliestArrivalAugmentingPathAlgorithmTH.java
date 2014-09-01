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

import de.tu_berlin.coga.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import de.tu_berlin.coga.netflow.dynamic.problems.DynamicTransshipmentProblem;
import de.tu_berlin.coga.netflow.dynamic.transshipment.TransshipmentWithTimeHorizon;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.netflow.ds.network.TimeExpandedNetwork;
import de.tu_berlin.coga.netflow.dynamic.earliestarrival.SuccessiveEarliestArrivalAugmentingPathAlgorithm;

/**
 * This class calculates an earliest arrival transshipment for a given time horizon
 * by using a successive earliest arrival augmenting path algorithm.
 */
public class SuccessiveEarliestArrivalAugmentingPathAlgorithmTH extends TransshipmentWithTimeHorizon<EarliestArrivalFlowProblem> {
	/**
	 * Creates a new {@code SuccessiveEarliestArrivalAugmentingPathAlgorithmTH} object
	 * with the given parameters. The method {@code runAlgorithm()} tests a time horizon and
	 * finds an earliest arrival transshipment for the time horizon, if possible.
	 */
	public SuccessiveEarliestArrivalAugmentingPathAlgorithmTH() {
		//super(network, transitTimes, edgeCapacities, nodeCapacities, supplies, timeHorizon,"Successive Earliest Arrival Augmenting Path Algorithm TH");
		setName( "Successive Earliest Arrival Augmenting Path Algorithm TH" );
	}

	/**
	 *
	 */
	public void runAlgorithm() {
		EarliestArrivalFlowProblem problem = new EarliestArrivalFlowProblem( getProblem() );
		SuccessiveEarliestArrivalAugmentingPathAlgorithm algo = new SuccessiveEarliestArrivalAugmentingPathAlgorithm();
		algo.setProblem( problem );
		algo.run();
		if( algo.getSolution().getFlowAmount() == problem.getTotalSupplies() )
			resultFlowPathBased = algo.getSolution().getPathBased();
		else
			System.out.println( algo.getSolution().getFlowAmount() + " vs. " + problem.getTotalSupplies() );
	}

	/**
	 * As we do not use the original {@code runAlgorithm()} method, {@code transshipmentWithTimeHorizon}
	 * is never called. Thus it's only a stub.
	 */
	@Override
	protected IdentifiableIntegerMapping<Edge> transshipmentWithTimeHorizon( TimeExpandedNetwork network ) {
		return null;
	}
}
