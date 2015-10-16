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
package org.zetool.netflow.dynamic;

import org.zetool.common.algorithm.AbstractAlgorithm;
import org.zetool.graph.localization.GraphLocalization;
import org.zetool.netflow.dynamic.problems.DynamicFlowProblem;
import org.zetool.netflow.classic.PathComposition;
import org.zetool.netflow.ds.flow.PathBasedFlowOverTime;
import org.zetool.netflow.ds.flow.EdgeBasedFlowOverTime;
import org.zetool.netflow.ds.flow.FlowOverTime;

/**
 * This class represents a dynamic flow algorithm.
 * @param <U>
 */
public abstract class DynamicFlowAlgorithm<U extends DynamicFlowProblem> extends AbstractAlgorithm<U, FlowOverTime> {

    /** Flow based result flow. */
    protected PathBasedFlowOverTime resultFlowPathBased = null;
    /** Edge based result flow. */
    protected EdgeBasedFlowOverTime resultFlowEdgeBased = null;

    /**
     * Creates a new algorithm object. Can only be called for subclasses.
     */
    public DynamicFlowAlgorithm() {
    }

    /**
     * Returns the calculated flow as {@code PathBasedFlowOverTime} object. Returns null if the algorithm has not run
     * yet, but throws an exception if it has run and only computed an edge based flow.
     *
     * @return the calculated flow as {@code PathBasedFlowOverTime} object.
     */
    public final PathBasedFlowOverTime getResultFlowPathBased() {
        if (resultFlowPathBased != null) {
            return resultFlowPathBased;
        }
        if (resultFlowEdgeBased != null) {
            throw new AssertionError(GraphLocalization.LOC.getString("algo.graph.dynmicflow.NoEdgeBasedFlowException"));
        }
        return null;
    }

    /**
     * Tells whether a path based result flow is available. Returns true if the edge based result flow is available but
     * the path based is not.
     *
     * @return whether a path based result flow is available.
     */
    public final boolean isPathBasedFlowAvailable() {
        return !(resultFlowPathBased == null && resultFlowEdgeBased != null);
    }

    /**
     * Returns the calculated flow as {@code EdgeBasedFlowOverTime} object. If the algorithm has not run yet, null is
     * returned. If the algorithm produced a path based flow, the edge based flow is calculated and returned.
     *
     * @return the calculated flow as {@code EdgeBasedFlowOverTime} object.
     */
    public final EdgeBasedFlowOverTime getResultFlowEdgeBased() {
        if (resultFlowEdgeBased != null) {
            return resultFlowEdgeBased;
        }
        if (resultFlowPathBased != null) {
            if (getProblem().getNetwork() == null) {
                throw new AssertionError("The variable network is null.");
            }
            if (getProblem().getTransitTimes() == null) {
                throw new AssertionError("The variable transitTimes is null.");
            }
            PathComposition pathComposition = new PathComposition(getProblem().getNetwork(), getProblem().getTransitTimes(), resultFlowPathBased);
            pathComposition.run();
            resultFlowEdgeBased = pathComposition.getEdgeFlows();
            return resultFlowEdgeBased;
        }
        return null;
    }
}
