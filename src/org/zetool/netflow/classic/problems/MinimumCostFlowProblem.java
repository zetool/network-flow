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

/*
 * MinimumCostFlowProblem.java
 *
 */
package org.zetool.netflow.classic.problems;

import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Node;

/**
 *
 * @author Martin Groß
 */
public class MinimumCostFlowProblem {
	private DirectedGraph graph;
	private IdentifiableIntegerMapping<Node> balances;
	private IdentifiableIntegerMapping<Edge> capacities;
	private IdentifiableIntegerMapping<Edge> costs;

	public MinimumCostFlowProblem( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> costs, IdentifiableIntegerMapping<Node> balances ) {
		this.graph = graph;
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

	public DirectedGraph getGraph() {
		return graph;
	}

	public void getGraph( DirectedGraph graph ) {
		this.graph = graph;
	}

	@Override
	public String toString() {
		return "MinimumCostFlowProblem{" + "graph=" + graph + ", balances=" + balances + ", capacities=" + capacities + ", costs=" + costs + '}';
	}
}
