/* zet evacuation tool copyright (c) 2007-2020 zet evacuation team
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
package org.zetool.netflow.dynamic.earliestarrival;

import java.util.LinkedList;
import java.util.List;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class TestInstance {

    public final DirectedGraph network;
    public final List<Node> sources;
    public final List<Node> sinks;
    public final IdentifiableIntegerMapping<Edge> capacities;
    public final IdentifiableIntegerMapping<Edge> transitTimes;

    /**
     * This is an example network provided in "An Introduction to Flows over Time" by Martin Skutella, page 5. The
     * network consists of 6 nodes (including a single sinc and source) and 7 arcs.
     */
    public static final TestInstance EXAMPLE_NETWORK = createSimpleInstance();

    private TestInstance(DirectedGraph network, List<Node> sources, List<Node> sinks, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes) {
        this.network = network;
        this.sources = sources;
        this.sinks = sinks;
        this.capacities = capacities;
        this.transitTimes = transitTimes;
    }

    private static TestInstance createSimpleInstance() {
        DefaultDirectedGraph network = new DefaultDirectedGraph(6, 7);
        Node s = network.getNode(0);
        Node v1 = network.getNode(1);
        Node v2 = network.getNode(2);
        Node v3 = network.getNode(3);
        Node v4 = network.getNode(4);
        Node t = network.getNode(5);

        List<Node> sources = new LinkedList<>();
        sources.add(s);
        List<Node> sinks = new LinkedList<>();
        sinks.add(t);

        Edge e1 = network.createAndSetEdge(s, v1);
        Edge e2 = network.createAndSetEdge(s, v3);
        Edge e3 = network.createAndSetEdge(v1, v2);
        Edge e4 = network.createAndSetEdge(v2, t);
        Edge e5 = network.createAndSetEdge(v3, v2);
        Edge e6 = network.createAndSetEdge(v3, v4);
        Edge e7 = network.createAndSetEdge(v4, t);

        IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>(network.edgeCount());
        capacities.set(e1, 1);
        capacities.set(e2, 1);
        capacities.set(e3, 1);
        capacities.set(e4, 1);
        capacities.set(e5, 1);
        capacities.set(e6, 1);
        capacities.set(e7, 1);

        IdentifiableIntegerMapping<Edge> transitTimes = new IdentifiableIntegerMapping<>(network.edgeCount());
        transitTimes.set(e1, 3);
        transitTimes.set(e2, 2);
        transitTimes.set(e3, 3);
        transitTimes.set(e4, 2);
        transitTimes.set(e5, 2);
        transitTimes.set(e6, 3);
        transitTimes.set(e7, 3);

        return new TestInstance(network, sources, sinks, capacities, transitTimes);
    }
}
