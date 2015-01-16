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

package de.tu_berlin.coga.netflow.dynamic.problems;

import de.tu_berlin.coga.netflow.classic.problems.MaximumFlowProblem;
import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Node;
import java.util.List;

/**
 *
 * @author Martin Groß
 */
public class MaximumFlowOverTimeProblem extends MaximumFlowProblem {

    private IdentifiableIntegerMapping<Edge> transitTimes;
    private int timeHorizon;
    
    public MaximumFlowOverTimeProblem(DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, Node source, Node sink, int timeHorizon) {
        super(graph, capacities, source, sink);
        this.timeHorizon = timeHorizon;
        this.transitTimes = transitTimes;        
    }

    public MaximumFlowOverTimeProblem(DirectedGraph network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, List<Node> sources, Node sink, int timeHorizon) {
        super(network, capacities, sources, sink);
        this.timeHorizon = timeHorizon;
        this.transitTimes = transitTimes;
    }

    public MaximumFlowOverTimeProblem(DirectedGraph network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, Node source, List<Node> sinks, int timeHorizon) {
        super(network, capacities, source, sinks);
        this.timeHorizon = timeHorizon;
        this.transitTimes = transitTimes;
    }

    public MaximumFlowOverTimeProblem(DirectedGraph network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes, List<Node> sources, List<Node> sinks, int timeHorizon) {
        super(network, capacities, sources, sinks);
        this.timeHorizon = timeHorizon;
        this.transitTimes = transitTimes;        
    }

    public int getTimeHorizon() {
        return timeHorizon;
    }

    public void setTimeHorizon(int timeHorizon) {
        this.timeHorizon = timeHorizon;
    }

    public IdentifiableIntegerMapping<Edge> getTransitTimes() {
        return transitTimes;
    }

    public void setTransitTimes(IdentifiableIntegerMapping<Edge> transitTimes) {
        this.transitTimes = transitTimes;
    }

  @Override
  public DirectedGraph getNetwork() {
    return (DirectedGraph)super.getNetwork();
  }
    
    
}
