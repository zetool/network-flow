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

package org.zetool.netflow.classic.mincost;

import org.zetool.common.algorithm.AbstractAlgorithm;
import org.zetool.netflow.classic.problems.MinimumCostFlowProblem;
import org.zetool.netflow.classic.transshipment.StaticTransshipment;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.graph.DirectedGraph;
import org.zetool.netflow.ds.network.OldResidualNetwork;
import org.zetool.graph.structure.StaticPath;
import org.zetool.netflow.ds.flow.Flow;

/**
 * A framework for cycle cancelling algorithms to solve minimum cost flow 
 * problems.
 * @author Martin Groß
 */
public abstract class CycleCancelling extends AbstractAlgorithm<MinimumCostFlowProblem, IdentifiableIntegerMapping<Edge>> {

    /**
     * Solves the given minimum cost flow problem by cycle cancelling. A 
     * feasible solution is computed using a static transshipment and optimized
     * afterwards by augmenting cycles of negative cost.
     * @param problem the minium cost flow problem.
     * @return a minimum cost flow for the specified problem or 
     * {@code null} if there is no feasible flow.
     */
    @Override
    protected Flow runAlgorithm(MinimumCostFlowProblem problem) {
        StaticTransshipment algorithm = new StaticTransshipment((DefaultDirectedGraph)problem.getGraph(), problem.getCapacities(), problem.getBalances());
        algorithm.run();
        OldResidualNetwork residualNetwork = algorithm.getResidualNetwork();
        if (residualNetwork == null) {
            fireEvent("The instance has no feasible solution.");
            return null;
        } else {
            IdentifiableIntegerMapping<Edge> residualCosts = residualNetwork.expandCostFunction(problem.getCosts());
            StaticPath cycle = findCycle(residualNetwork, residualCosts);
            while (cycle != null) {
                int minimumCycleCapacity = residualNetwork.residualCapacities().minimum(cycle);
                for (Edge edge : cycle) {
                    residualNetwork.augmentFlow(edge, minimumCycleCapacity);
                }
                cycle = findCycle(residualNetwork, residualCosts);
            }
            return new Flow(residualNetwork.flow());
        }
    }

    /**
     * Obtain a cycle with negative total cost if such a cycle exists.
     * @return a cycle of negative total cost or {@code null} if no such 
     * cycle exists.
     */
    protected abstract StaticPath findCycle(DirectedGraph graph, IdentifiableIntegerMapping<Edge> costs);
}
