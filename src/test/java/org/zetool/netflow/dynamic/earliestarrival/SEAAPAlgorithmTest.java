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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Node;
import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class SEAAPAlgorithmTest {

    /**
     * This is an example provided in "An Introduction to Flows over Time" by Martin Skutella, page 14. The network
     * consists of 6 nodes (including a single sinc and source) and 7 arcs. It sends 6 units of flow within a time
     * horizon of 10. (The last flow unit is completely at the source at time 11 if this were a continuous flow).
     */
    @Test
    public void simpleInstanceTest() {
        TestInstance exampleNetwork = TestInstance.EXAMPLE_NETWORK;

        IdentifiableIntegerMapping<Node> nodeCapacities = new IdentifiableIntegerMapping<>(exampleNetwork.network.nodes());
        IdentifiableIntegerMapping<Node> currentAssignment = new IdentifiableIntegerMapping<>(exampleNetwork.network.nodes());

        int flowValue = 6;

        exampleNetwork.sources.forEach((source) -> {
            currentAssignment.set(source, flowValue);
        });
        currentAssignment.set(exampleNetwork.sinks.get(0), -flowValue);

        EarliestArrivalFlowProblem eat = new EarliestArrivalFlowProblem(exampleNetwork.capacities, exampleNetwork.network, nodeCapacities,
                exampleNetwork.sinks.get(0), exampleNetwork.sources, 20, exampleNetwork.transitTimes, currentAssignment);
        SEAAPAlgorithm fixture = new SEAAPAlgorithm();
        fixture.setProblem(eat);
        fixture.run();
        assertThat(fixture.getSolution().getTimeHorizon(), is(equalTo(10)));
    }
}
