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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import org.zetool.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.netflow.dynamic.earliestarrival.TestInstance;

public class MaxFlowOverTimeTest {

    // TODO: some additional simple test cases, empty network, network withoug s-t-path, two nodes, one arc, parallel arcs    
    @Test
    public void MaxFlowTest() {
        System.out.println("TEST 1:");
        DefaultDirectedGraph network = new DefaultDirectedGraph(9, 10);
        Node source1 = network.getNode(0);
        Node source2 = network.getNode(1);
        Node source3 = network.getNode(2);
        Node sink1 = network.getNode(3);
        Node sink2 = network.getNode(4);
        Node sink3 = network.getNode(5);
        Node a = network.getNode(6);
        Node b = network.getNode(7);
        Node c = network.getNode(8);

        Edge e1 = network.createAndSetEdge(source1, a);
        Edge e2 = network.createAndSetEdge(source1, b);
        Edge e3 = network.createAndSetEdge(source2, a);
        Edge e4 = network.createAndSetEdge(source2, source3);
        Edge e5 = network.createAndSetEdge(source3, c);
        Edge e6 = network.createAndSetEdge(a, b);
        Edge e7 = network.createAndSetEdge(a, source3);
        Edge e8 = network.createAndSetEdge(b, sink1);
        Edge e9 = network.createAndSetEdge(b, sink2);
        Edge e10 = network.createAndSetEdge(sink2, sink3);

        IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>(network.edgeCount());
        capacities.set(e1, 1);
        capacities.set(e2, 1);
        capacities.set(e3, 2);
        capacities.set(e4, 2);
        capacities.set(e6, 3);
        capacities.set(e7, 3);
        capacities.set(e8, 1);
        capacities.set(e9, 2);
        capacities.set(e5, 1);
        capacities.set(e10, 1);
        IdentifiableIntegerMapping<Edge> transitTimes = new IdentifiableIntegerMapping<>(network.edgeCount());
        transitTimes.set(e1, 1);
        transitTimes.set(e2, 1);
        transitTimes.set(e3, 2);
        transitTimes.set(e4, 2);
        transitTimes.set(e6, 3);
        transitTimes.set(e7, 3);
        transitTimes.set(e8, 1);
        transitTimes.set(e9, 2);
        transitTimes.set(e5, 1);
        transitTimes.set(e10, 1);
        List<Node> sources = new LinkedList<>();
        sources.add(source1);
        sources.add(source2);
        sources.add(source3);
        List<Node> sinks = new LinkedList<>();
        sinks.add(sink1);
        sinks.add(sink2);
        sinks.add(sink3);

        MaxFlowOverTime algo = new MaxFlowOverTime();

        MaximumFlowOverTimeProblem mfotp = new MaximumFlowOverTimeProblem(network, capacities, transitTimes, sources, sinks, 25);

        algo.setProblem(mfotp);

        algo.runAlgorithm();

        System.out.println(algo.getSolution());
        System.out.println("Value: " + algo.getSolution().getValue());
    }

    /**
     * This is an example provided in "An Introduction to Flows over Time" by Martin Skutella, page 5f. The network
     * consists of 6 nodes (including a single sinc and source) and 7 arcs, the time horizon is 9.
     */
    @Test
    public void MaxFlowTest2() {
        TestInstance exampleNetwork = TestInstance.EXAMPLE_NETWORK;

        int timeHorizon = 9;
        MaximumFlowOverTimeProblem mfotp = new MaximumFlowOverTimeProblem(exampleNetwork.network, exampleNetwork.capacities,
                exampleNetwork.transitTimes, exampleNetwork.sources, exampleNetwork.sinks, timeHorizon);

        MaxFlowOverTime fixture = new MaxFlowOverTime();
        fixture.setProblem(mfotp);
        fixture.run();

        assertThat(fixture.getSolution().getValue(), is(equalTo(3L)));
    }
}
