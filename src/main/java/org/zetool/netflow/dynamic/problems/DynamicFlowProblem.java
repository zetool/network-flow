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
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;

/**
 *
 * @author Martin Groß
 */
public class DynamicFlowProblem {

    protected IdentifiableIntegerMapping<Edge> edgeCapacities;
    private DirectedGraph network;
    protected IdentifiableIntegerMapping<Edge> transitTimes;

	public DynamicFlowProblem( IdentifiableIntegerMapping<Edge> edgeCapacities, DirectedGraph graph, IdentifiableIntegerMapping<Edge> transitTimes ) {
		this.edgeCapacities = edgeCapacities;
		this.network = graph;
		this.transitTimes = transitTimes;
	}
		
		
		public IdentifiableIntegerMapping<Edge> getEdgeCapacities() {
        return edgeCapacities;
    }

//    public void setCapacities(IdentifiableIntegerMapping<Edge> capacities) {
//        this.edgeCapacities = capacities;
//    }

    public DirectedGraph getNetwork() {
        return network;
    }

//    public void setNetwork(AbstractNetwork network) {
//        this.network = network;
//    }

    public IdentifiableIntegerMapping<Edge> getTransitTimes() {
        return transitTimes;
    }

//    public void setTransitTimes(IdentifiableIntegerMapping<Edge> transitTimes) {
//        this.transitTimes = transitTimes;
//    }

}
