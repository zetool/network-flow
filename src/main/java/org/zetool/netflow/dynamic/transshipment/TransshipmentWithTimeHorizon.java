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
package org.zetool.netflow.dynamic.transshipment;

import org.zetool.netflow.dynamic.problems.DynamicTransshipmentProblem;
import org.zetool.netflow.dynamic.DynamicFlowAlgorithm;
import org.zetool.netflow.classic.maxflow.PathDecomposition;
import org.zetool.netflow.ds.structure.DynamicPath;
import org.zetool.graph.Edge;
import org.zetool.graph.structure.StaticPath;
import org.zetool.netflow.ds.network.TimeExpandedNetwork;
import org.zetool.netflow.ds.flow.FlowOverTime;
import org.zetool.netflow.ds.structure.FlowOverTimePath;
import org.zetool.netflow.ds.flow.PathBasedFlowOverTime;
import org.zetool.netflow.ds.flow.PathBasedFlow;
import org.zetool.netflow.ds.structure.StaticFlowPath;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import java.util.logging.Level;

/**
 * The class {@code TransshipmentWithTimeHorizon} provides a method to calculate a dynamic transshipment with certain
 * properties by using the time-expanded network if the method to compute a adequate transshipment in the time-expanded
 * network is overridden.
 *
 * @param <U>
 */
public abstract class TransshipmentWithTimeHorizon<U extends DynamicTransshipmentProblem>
        extends DynamicFlowAlgorithm<U> {

    /**
     * Abstract method that has to be overridden with the concrete transshipment algorithm.
     *
     * @param network The (time expanded) network the algorithm works on.
     * @return An edge based flow.
     */
    protected abstract IdentifiableIntegerMapping<Edge> transshipmentWithTimeHorizon(TimeExpandedNetwork network);

    @Override
    protected FlowOverTime runAlgorithm(U problem) {
        computeFlow();
        return new FlowOverTime(resultFlowPathBased, getProblem());
    }

    /**
     * Computes a transshipment using the method {@link #transshipmentWithTimeHorizon} that has to be implemented by
     * subclasses. The algorithm creates a time expanded network, calls {@link #transshipmentWithTimeHorizon} to compute
     * a static flow and creates a dynamic flow from the result. If {@code runTransshipment} returns {@code null} this
     * method also returns {@code null}.
     */
    protected void computeFlow() {
        /* Short debug output telling that a time expanded network is created. */
        log.log(Level.INFO, "The {0} algorithm creates a time expanded network.", getName());
        fireEvent("Time-Expanded Network-Creation. The " + getName() + " algorithm creates a time expanded network.");

        /* Create the time expanded network up to the given time horizon. */
        TimeExpandedNetwork tnetwork = new TimeExpandedNetwork(getProblem().getNetwork(),
                getProblem().getEdgeCapacities(), getProblem().getTransitTimes(),
                getProblem().getTimeHorizon(), getProblem().getSupplies(), false);

        /* Short debug output including the size of the created expanded network. */
        log.finest("The time expanded network was created.");
        log.log(Level.FINEST, "It has {0} nodes and {1} edges.",
                new Object[]{tnetwork.nodes().size(), tnetwork.edges().size()});
        fireEvent("Time-Expanded Network created. It has " + tnetwork.nodes().size()
                + " nodes and " + tnetwork.edges().size() + " edges.");

        /* Long debug output including the complete expanded network.*/
        log.finest(tnetwork.toString());

        /* Progress output. */
        log.log(Level.INFO, "the {0} algorithm is called.. ", getName());
        fireEvent(getName() + " algorithm. The " + getName() + " algorithm is called.");

        /* Compute the static flow according to the specifit transshipment with time horizon. */
        IdentifiableIntegerMapping<Edge> flow = transshipmentWithTimeHorizon(tnetwork);

        /* Short debug output telling whether the current time horizon was sufficient. */
        log.log(Level.INFO, "A time horizon of {0} is {1}", new Object[]{getProblem().getTimeHorizon(),
            flow == null ? "not sufficient." : "sufficient."});

        /* If flow==null, there does not exists a feasible static transshipment (with wished properties)
         * and therefore there does not exist a feasible dynamic transshipment (with wished properties).*/
        if (flow == null) {
            resultFlowPathBased = null;
            return;
        }

        /* Long debug output including the flow function of the found flow. */
        log.log(Level.FINEST, "\nStatic transshipment as flow function:\n{0}", flow);
        log.log(Level.FINER, "\nCalculating path decomposition from sources {0} to sinks {1}.",
                new Object[]{tnetwork.sources(), tnetwork.sinks()});

        /* Decompose the flow into static paths flows.*/
        PathBasedFlow decomposedFlow = PathDecomposition.calculatePathDecomposition(
                tnetwork, tnetwork.sources().asList(), tnetwork.sinks().asList(), flow);

        /* Long debug output containing the path flows.*/
        log.log(Level.FINEST, "\nStatic transshipment path based:{0}", decomposedFlow);

        /* Translating the static flow into a dynamic flow.*/
        PathBasedFlowOverTime dynamicTransshipment = new PathBasedFlowOverTime();
        for (StaticFlowPath staticPathFlow : decomposedFlow) {
            if (staticPathFlow.getAmount() == 0) {
                continue;
            }
            StaticPath staticPath = staticPathFlow.getPath();
            DynamicPath dynamicPath = tnetwork.translatePath(staticPath);
			// The rate of the dynamic path is the amount of the static path,
            // and the amount of the dynamic path is its rate * how long it flows,
            // but as all flows constructed in the time-expanded network have
            // length T-1, flow always flows for one time step, thus amount = rate.
            FlowOverTimePath dynamicPathFlow = new FlowOverTimePath(dynamicPath,
                    staticPathFlow.getAmount(), staticPathFlow.getAmount());
            dynamicTransshipment.addPathFlow(dynamicPathFlow);
        }

        log.log(Level.FINEST, "Dynamic transshipment: {0}", dynamicTransshipment);

        resultFlowPathBased = dynamicTransshipment;
    }

}
