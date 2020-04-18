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
package org.zetool.netflow.dynamic.problems;

import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class DynamicTransshipmentProblem extends DynamicFlowProblem {
	protected IdentifiableIntegerMapping<Node> nodeCapacities;
	protected IdentifiableIntegerMapping<Node> supplies;
	private int timeHorizon;

	public DynamicTransshipmentProblem( IdentifiableIntegerMapping<Edge> edgeCapacities, DirectedGraph network, IdentifiableIntegerMapping<Node> nodeCapacities, int timeHorizon, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Node> supplies ) {
		super( edgeCapacities, network, transitTimes );
		this.nodeCapacities = nodeCapacities;
		this.supplies = supplies;
		this.timeHorizon = timeHorizon;
		//  this.transitTimes = transitTimes;
//        for (Node source : sources) {
//            totalSupplies += supplies.get(source);
//        }
	}

	public int getTimeHorizon() {
		return timeHorizon;
	}

	/**
	 * Sets a new time horizon for the instance. Use this if a time horizon
	 * has changed, for example if an estimator has been used.
	 * @param timeHorizon the new time horizon
	 */
	public void setTimeHorizon( int timeHorizon ) {
		this.timeHorizon = timeHorizon;
	}

	public IdentifiableIntegerMapping<Node> getNodeCapacities() {
		return nodeCapacities;
	}

	public IdentifiableIntegerMapping<Node> getSupplies() {
		return supplies;
	}
}
