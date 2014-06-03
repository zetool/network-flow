/* zet evacuation tool copyright (c) 2007-10 zet evacuation team
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

/*
 * MinimumCostFlowProblem.java
 *
 */
package de.tu_berlin.coga.netflow.classic.problems;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.netflow.ds.network.AbstractNetwork;
import de.tu_berlin.coga.graph.Node;

/**
 *
 * @author Martin Groß
 */
public class MinimumCostFlowProblem {
	private AbstractNetwork network;
	private IdentifiableIntegerMapping<Node> balances;
	private IdentifiableIntegerMapping<Edge> capacities;
	private IdentifiableIntegerMapping<Edge> costs;

	public MinimumCostFlowProblem( AbstractNetwork network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> costs, IdentifiableIntegerMapping<Node> balances ) {
		this.network = network;
		this.balances = balances;
		this.capacities = capacities;
		this.costs = costs;
	}

	public IdentifiableIntegerMapping<Node> getBalances() {
		return balances;
	}

	public void setBalances( IdentifiableIntegerMapping<Node> balances ) {
		this.balances = balances;
	}

	public IdentifiableIntegerMapping<Edge> getCapacities() {
		return capacities;
	}

	public void setCapacities( IdentifiableIntegerMapping<Edge> capacities ) {
		this.capacities = capacities;
	}

	public IdentifiableIntegerMapping<Edge> getCosts() {
		return costs;
	}

	public void setCosts( IdentifiableIntegerMapping<Edge> costs ) {
		this.costs = costs;
	}

	public AbstractNetwork getNetwork() {
		return network;
	}

	public void setNetwork( AbstractNetwork network ) {
		this.network = network;
	}

	@Override
	public String toString() {
		return "MinimumCostFlowProblem{" + "network=" + network + ", balances=" + balances + ", capacities=" + capacities + ", costs=" + costs + '}';
	}
}
