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
package org.zetool.netflow.dynamic.transshipment;

import org.zetool.netflow.dynamic.problems.DynamicTransshipmentProblem;

/**
 * The class {@code QuickestTransshipment} calculates the smallest
 * time horizon T such that it is
 * possible to fulfill given supplies and demands in a network
 * with capacities and transit times within this time horizon.
 * A corresponding flow that satisfies the supplies and
 * demands is also computed.
 *
 * The algorithm to find the so called quickest transshipment
 * is implemented by using binary search and calling a dynamic transshipment
 * algorithm at each point of the search. Therefore
 * the classes {@link DynamicTransshipment} is used.
 * The class {@code TransshipmentFramework} implements the binary search
 * for an arbitrary dynamic transshipment algorithm which possibly guarantees more properties.
 */
public class QuickestTransshipment extends TransshipmentFramework<DynamicTransshipmentProblem,DynamicTransshipment<DynamicTransshipmentProblem>> {

	/**
	 * Creates a new quickest transshipment algorithm instance.
	 */
	public QuickestTransshipment(){
		//super(network, transitTimes, capacities, null, supplies, DynamicTransshipment.class, null);
		super( new DynamicTransshipment() );
	}

/*    *//**
     * A static method to compute a quickest transshipment in a given network.
     * Should always return a transshipment, otherwise a bug occurred.
     * @param network The given network.
	 * @param transitTimes The transit times for all edges in the network.
	 * @param edgeCapacities The capacities of all edges in the network.
	 * @param supplies Supplies and demands of all nodes in the network.
     * @return A quickest transshipment fulfilling all supplies and demands.
     *//*
	public static DynamicFlow compute(AbstractNetwork network,
			IdentifiableIntegerMapping<Edge> transitTimes,
			IdentifiableIntegerMapping<Edge> edgeCapacities,
			IdentifiableIntegerMapping<Node> supplies) {
		QuickestTransshipment qt = QuickestTransshipment.getInstance();
		return qt.computeNonStatic(network, transitTimes, edgeCapacities,
				supplies);
	}

    *//**
     * A method to compute a quickest transshipment in a given network.
     * Should always return a transshipment, otherwise a bug occurred.
     * @param network The given network.
	 * @param transitTimes The transit times for all edges in the network.
	 * @param edgeCapacities The capacities of all edges in the network.
	 * @param supplies Supplies and demands of all nodes in the network.
     * @return A quickest transshipment fulfilling all supplies and demands.
     *//*
	public DynamicFlow computeNonStatic(AbstractNetwork network,
			IdentifiableIntegerMapping<Edge> transitTimes,
			IdentifiableIntegerMapping<Edge> edgeCapacities,
			IdentifiableIntegerMapping<Node> supplies) {

		DynamicTransshipment algo = DynamicTransshipment.getInstance();
		return computeNonStatic(network, transitTimes, edgeCapacities,
				supplies, algo);
	}*/
}