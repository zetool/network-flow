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

import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DefaultDirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.netflow.ds.network.DirectedNetwork;
import org.zetool.netflow.ds.network.Network;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class FordFulkersonTest {
  @Test
  public void testMultipleSources() {
    DefaultDirectedGraph graph = new DefaultDirectedGraph( 3, 2 );
    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 2 );

    Edge e1 = graph.createAndSetEdge( graph.getNode( 0 ), graph.getNode( 2 ) );
    Edge e2 = graph.createAndSetEdge( graph.getNode( 1 ), graph.getNode( 2 ) );

    capacities.set( e1, 1 );
    capacities.set( e2, 2 );
    
    List<Node> sources = new LinkedList<>();
    sources.add( graph.getNode( 0 ) );
    sources.add( graph.getNode( 1 ) );
    List<Node> sinks = Collections.singletonList( graph.getNode( 2 ) );
    
    Network network = new DirectedNetwork( graph, capacities, sources, sinks );
    MaximumFlowProblem mfp = new MaximumFlowProblem( network );

    FordFulkerson ff = new FordFulkerson();

    ff.setProblem( mfp );
    ff.run();

    assertEquals( "Flow value", 3, ff.getSolution().getFlowValue() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }
  
  @Test
  public void testInstance() {
    MaximumFlowProblem mfp = new MaximumFlowProblem( FlowTestInstances.getDiamondExample() );
    FordFulkerson ff = new FordFulkerson();
    ff.setProblem( mfp );

    ff.run();

    assertEquals( "Flow value", 3, ff.getSolution().getFlowValue() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }

  @Test
  public void testInstanceUndirected() {
    MaximumFlowProblem mfp = new MaximumFlowProblem( FlowTestInstances.getDiamondExampleUndirected() );
    FordFulkerson ff = new FordFulkerson();
    ff.setProblem( mfp );

    ff.run();

    assertEquals( "Flow value", 0, ff.getSolution().getFlowValue() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }

  @Test
  public void testHarrisRoss() {
    MaximumFlowProblem mfp = new MaximumFlowProblem( FlowTestInstances.getHarrisRossOriginal() );
    FordFulkerson ff = new FordFulkerson();
    ff.setProblem( mfp );

    ff.run();
    
    assertEquals( "Flow value", 163, ff.getSolution().getFlowValue() );
    assertEquals( "Check the solution.", true, ff.getSolution().check() );
  }
}
