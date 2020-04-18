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

package org.zetool.netflow.dynamic.maxflow;

import org.zetool.common.algorithm.AbstractAlgorithm;
import org.zetool.netflow.ds.structure.DynamicPath;
import org.zetool.netflow.ds.network.TimeExpandedNetwork;
import org.zetool.netflow.ds.structure.StaticFlowPath;
import org.zetool.netflow.ds.structure.FlowOverTimePath;
import org.zetool.graph.structure.StaticPath;
import org.zetool.netflow.ds.flow.PathBasedFlowOverTime;
import org.zetool.netflow.ds.flow.PathBasedFlow;
import org.zetool.netflow.ds.flow.MaximumFlow;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
import org.zetool.netflow.classic.maxflow.PushRelabelHighestLabelGlobalGapRelabelling;
import org.zetool.netflow.classic.maxflow.PathDecomposition;
import org.zetool.common.algorithm.Algorithm;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;

/**
 * Calculates a maximum flow over time by reducing it to the maximum flow 
 * problem using a time expanded network.
 * @author Martin Groß
 */
public class TimeExpandedMaximumFlowOverTime extends AbstractAlgorithm<MaximumFlowOverTimeProblem, PathBasedFlowOverTime> {

    @Override
    protected PathBasedFlowOverTime runAlgorithm(MaximumFlowOverTimeProblem problem) {
            if( problem.getSources().isEmpty() || problem.getSinks().isEmpty() ) {
            System.out.println("TimeExpandedMaximumFlowOverTime: The problem is invalid - this should not happen!");
            return new PathBasedFlowOverTime();
        }
        int v = 0;        
        for (Node source : problem.getSources()) {
            for (Edge edge : problem.getNetwork().outgoingEdges(source)) {
                v += problem.getCapacities().get(edge) * problem.getTimeHorizon();
            }
        }
        TimeExpandedNetwork ten = new TimeExpandedNetwork(problem.getNetwork(), problem.getCapacities(), problem.getTransitTimes(), problem.getTimeHorizon(), problem.getSources(), problem.getSinks(), v, false);
        MaximumFlowProblem maximumFlowProblem = new MaximumFlowProblem(ten, ten.capacities(), ten.sources().asList(), ten.sinks().asList());
        Algorithm<MaximumFlowProblem, MaximumFlow> algorithm = new PushRelabelHighestLabelGlobalGapRelabelling();
        algorithm.setProblem(maximumFlowProblem);
        algorithm.run();

        PathBasedFlow decomposedFlow = PathDecomposition.calculatePathDecomposition(ten, ten.sources().asList(), ten.sinks().asList(), algorithm.getSolution());
        PathBasedFlowOverTime dynamicFlow = new PathBasedFlowOverTime();
        for (StaticFlowPath staticPathFlow : decomposedFlow) {
            if (staticPathFlow.getAmount() == 0) {
                System.out.println("TimeExpandedMaximumFlowOverTime: There is a flow path with zero units - this should not happen!");
                continue;
            }
            StaticPath staticPath = staticPathFlow.getPath();
            DynamicPath dynamicPath = ten.translatePath(staticPath);
            FlowOverTimePath dynamicPathFlow = new FlowOverTimePath(dynamicPath, staticPathFlow.getAmount(), staticPathFlow.getAmount());
            dynamicFlow.addPathFlow(dynamicPathFlow);
        }
        return dynamicFlow;
    }
}
