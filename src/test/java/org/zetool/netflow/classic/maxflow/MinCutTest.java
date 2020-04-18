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
package org.zetool.netflow.classic.maxflow;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import org.junit.Test;
import org.zetool.graph.Node;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.netflow.ds.network.ResidualNetwork;

/**
 * Tests computation of cuts by the {@link FordFulkerson} algorithm.
 * @author Jan-Philipp Kappmeier
 */
public class MinCutTest {
  private static class MockFordFulkerson extends FordFulkerson {

    /**
     * Allows instantiate an instance of the {@link FordFulkerson} algorithm and to set a residual network.
     * @param mockedResidualNetwork the residual network
     */
    public MockFordFulkerson( ResidualNetwork mockedResidualNetwork, Node source ) {
      super();
      this.residualNetwork = mockedResidualNetwork;
      this.source = source;
      setProblem( new MaximumFlowProblem( residualNetwork ) );
    }
  }

  @Test
  public void testSimpleInstanceNodeSet() {
    MaximumFlowProblem problem = new MaximumFlowProblem( FlowTestInstances.getSimpleExample() );
    ResidualNetwork residualNetwork = ResidualNetwork.getResidualNetwork( problem.getNetwork(), problem.getCapacities(),
            problem.getSource(), problem.getSink() );
    residualNetwork.augmentFlow( problem.getNetwork().getEdge( 0 ), 1 );

    MockFordFulkerson ff = new MockFordFulkerson( residualNetwork, problem.getSource() );
    Set<Node> cutSet = ff.computeCutNodes();
    
    assertThat(cutSet.size(), is( equalTo( 1 ) ) );
    assertThat(cutSet.contains( problem.getSource() ), is( true ) );
    assertThat(ff.isInCut( problem.getSource() ), is( true ) );
    assertThat(cutSet.contains( problem.getSink() ), is( false ) );
    assertThat(ff.isInCut( problem.getSink() ), is( false ) );
  }

  @Test
  public void testSimpleInstanceEdgeSet() {
    MaximumFlowProblem problem = new MaximumFlowProblem( FlowTestInstances.getSimpleExample() );
    ResidualNetwork residualNetwork = ResidualNetwork.getResidualNetwork( problem.getNetwork(), problem.getCapacities(),
            problem.getSource(), problem.getSink() );
    residualNetwork.augmentFlow( problem.getNetwork().getEdge( 0 ), 1 );

    MockFordFulkerson ff = new MockFordFulkerson( residualNetwork, problem.getSource() );

    ff.computeCutEdges();
    assertThat(ff.getOutgoingCut().size(), is( equalTo( 0 ) ) );
    assertThat(ff.getIncomingCut().size(), is( equalTo( 1 ) ) );
    assertThat(ff.getIncomingCut().contains( residualNetwork.getEdge( 1 ) ), is( true ) );
  }
}
