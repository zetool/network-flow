/* zet evacuation tool copyright (c) 2007-09 zet evacuation team
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
 * LongestShortestPathTimeHorizonEstimator.java
 *
 */

package de.tu_berlin.coga.netflow.dynamic.earliestarrival;

import de.tu_berlin.coga.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import de.tu_berlin.math.coga.algorithm.shortestpath.Dijkstra;
import de.tu_berlin.coga.common.algorithm.Algorithm;
import de.tu_berlin.coga.graph.Node;

/**
 *
 * @author Martin Groß
 */
public class LongestShortestPathTimeHorizonEstimator extends Algorithm<EarliestArrivalFlowProblem, TimeHorizonBounds> {

    @Override
    protected TimeHorizonBounds runAlgorithm(EarliestArrivalFlowProblem problem) {
        int longest = 0;
        for (Node source : problem.getSources()) {
            Dijkstra dijkstra = new Dijkstra(problem.getNetwork(), problem.getTransitTimes(), source);
            dijkstra.run();
            if (dijkstra.getDistance(problem.getSink()) > longest) {
                longest = dijkstra.getDistance(problem.getSink());
            }
        }
        int supply = 0;
				for( Node source : problem.getSources() )
					supply += problem.getSupplies().get( source );
        return new TimeHorizonBounds(longest + 1, longest + supply + 1);
    }

}
