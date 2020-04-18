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

import java.util.List;

import org.zetool.common.algorithm.AbstractAlgorithm;
import org.zetool.common.algorithm.Algorithm;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.structure.StaticPath;
import org.zetool.graph.localization.GraphLocalization;
import org.zetool.netflow.classic.maxflow.PathDecomposition;
import org.zetool.netflow.classic.mincost.MinimumMeanCycleCancelling;
import org.zetool.netflow.classic.problems.MinimumCostFlowProblem;
import org.zetool.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
import org.zetool.netflow.ds.flow.PathBasedFlow;
import org.zetool.netflow.ds.flow.TimeReapeatedFlow;
import org.zetool.netflow.ds.network.ExtendedGraph;
import org.zetool.netflow.ds.structure.DynamicPath;
import org.zetool.netflow.ds.structure.FlowOverTimePath;
import org.zetool.netflow.ds.structure.StaticFlowPath;

/**
 * The class {@code MaxFlowOverTime} solves the max flow over time problem. The flow is computed by a reduction to a
 * minimum cost flow over time computation. The reduction increases the size of the problem input and thus needs the
 * double amount of free space in memory.
 *
 * @author Jan-Philipp Kappmeier
 * @author Gordon Schlechter
 */
public class MaxFlowOverTime extends AbstractAlgorithm<MaximumFlowOverTimeProblem, TimeReapeatedFlow> {

    private DefaultDirectedGraph network;
    private IdentifiableIntegerMapping<Edge> edgeCapacities;
    private List<Node> sinks;
    private List<Node> sources;
    private Node superNode;
    private int timeHorizon;
    private IdentifiableIntegerMapping<Edge> transitTimes;
    private ExtendedGraph ex;

    /**
     * Creates a new instance of MaxFlowOverTime
     */
    public MaxFlowOverTime() {
    }

    /**
     * Reduction for the computation of an maximum flow over time using a minimum cost flow computation.
     */
    private void reduction() {
        int newEdgeCount = sources.size() + sinks.size();
        int newNodeCount = 1;
        ex = new ExtendedGraph(network, newNodeCount,newEdgeCount );
        superNode = ex.getFirstNewNode();

        edgeCapacities.setDomainSize(ex.edgeCount()); // reserve space
        transitTimes.setDomainSize(ex.edgeCount()); // reserve space

        for (Node source : sources) {
            Edge newEdge = ex.createAndSetEdge(superNode, source);
            edgeCapacities.set(newEdge, Integer.MAX_VALUE);
            transitTimes.set(newEdge, -(timeHorizon + 1));
        }

        for (Node sink : sinks) {
            Edge newEdge = ex.createAndSetEdge(sink, superNode);
            edgeCapacities.set(newEdge, Integer.MAX_VALUE);
            transitTimes.set(newEdge, 0);
        }
    }

    /**
     * Hides the added super node and edges in the network. After this you got back the original network. The added
     * Edges in the flow are also removed.
     */
    private void reconstruction(IdentifiableIntegerMapping<Edge> flow) {
        // the additional edges have the highest numbers, so we can just get rid of them
        flow.setDomainSize(flow.getDomainSize() - (sources.size() + sinks.size()));
        transitTimes.setDomainSize(flow.getDomainSize());
        edgeCapacities.setDomainSize(flow.getDomainSize());
    }

    /**
     * Creates dynamic flow out of the given static flow. At first static flow is divided in the different paths and
     * then it is added to the dynamic flow, if the conditions are met.
     */
    private TimeReapeatedFlow translateIntoMaxFlow(PathBasedFlow minCostFlow) {
        TimeReapeatedFlow mFlow = new TimeReapeatedFlow(timeHorizon);

        for (StaticFlowPath staticPathFlow : minCostFlow) {
            if (staticPathFlow.getAmount() == 0) {
                continue;
            }
            StaticPath staticPath = staticPathFlow.getPath();
            int path_transit_time = 0;
            for (Edge e : staticPath) {
                path_transit_time += transitTimes.get(e);
            }

            // Add this path only in case that our given time is long 
            // enough to send anything at all over this path
            if (timeHorizon > path_transit_time) {
                DynamicPath dynamicPath = new DynamicPath(staticPath);
                FlowOverTimePath dynamicPathFlow = new FlowOverTimePath(dynamicPath, staticPathFlow.getAmount(), (timeHorizon - path_transit_time) * staticPathFlow.getAmount());
                mFlow.addPathFlow(dynamicPathFlow);
            }
        }

        return mFlow;
    }

    @Override
    protected TimeReapeatedFlow runAlgorithm(MaximumFlowOverTimeProblem problem) {
        sinks = problem.getSinks();
        sources = problem.getSources();
        network = (DefaultDirectedGraph) problem.getNetwork(); // TODO avoid cast here?
        edgeCapacities = problem.getCapacities();
        transitTimes = problem.getTransitTimes();
        timeHorizon = problem.getTimeHorizon();

        if ((sources == null) || (sinks == null)) {
            throw new IllegalArgumentException(GraphLocalization.LOC.getString("algo.graph.MaxFlowOverTime.SpecifySourceSinkFirst"));
        }

        if ((sources.isEmpty()) || (sinks.isEmpty())) {
            return new TimeReapeatedFlow(timeHorizon);
        }

        reduction();
        
        MinimumCostFlowProblem p = new MinimumCostFlowProblem(ex, edgeCapacities, transitTimes, new IdentifiableIntegerMapping<>(ex.nodes().size()));
        Algorithm<MinimumCostFlowProblem, IdentifiableIntegerMapping<Edge>> algorithm = new MinimumMeanCycleCancelling();
        algorithm.setProblem(p);
        algorithm.run();
        IdentifiableIntegerMapping<Edge> flow = algorithm.getSolution();

        //SuccessiveShortestPath algo = new SuccessiveShortestPath(network, zeroSupplies, edgeCapacities, transitTimes);
        //algo.run();
        //flow = algo.getFlow();
        reconstruction(flow);

        PathBasedFlow minCostFlow = PathDecomposition.calculatePathDecomposition(network, sources, sinks, flow);

        return translateIntoMaxFlow(minCostFlow);
    }
}
