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

import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.structure.StaticPath;

/**
 * This class provides an implementation of the cycle cancelling method to solve
 * minimum cost flow problems. More specifically, it implements the minimum mean
 * cycle cancelling algorithm. In every cycle cancelling step, a cycle of 
 * minimum mean total cost is calculated and augmengted. In comparison to other
 * cycle cancelling strategies, the selection of cycles with minimum mean total 
 * cost leads to a polynomial running time.
 * 
 * @author Martin Groß
 */
public class MinimumMeanCycleCancelling extends CycleCancelling {

    /**
     * Creates a new instance of the minimum mean cycle cancelling algorithm.
     */
    public MinimumMeanCycleCancelling() {
    }

    /**
     * Determines a cycle of minimum mean cost in the given network and returns 
     * it if its total cost is negative.
     * @param graph the network.
     * @param costs the cost function.
     * @return a cycle of minimum mean cost, if the minimum mean cost is 
     * negative. If the minimum mean cost of a cycle is non-negative or the 
     * given network is acyclic.
     */
    @Override
    protected StaticPath findCycle( DirectedGraph graph, IdentifiableIntegerMapping<Edge> costs) {
        StaticPath cycle = MinimumMeanCycleDetector.detect((DefaultDirectedGraph)graph, costs);        
        if (cycle == null) {
            return null;
        } else {
            int cycleCost = costs.sum(cycle);
            if (cycleCost >= 0) {
                return null;
            } else {
                return cycle;
            }
        }
    }
}
