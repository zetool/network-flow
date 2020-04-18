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

package org.zetool.netflow.ds.network;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.jmock.Expectations.same;
import org.zetool.graph.DirectedGraph;
import org.jmock.Mockery;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class DirectedNetworkTest {
  
  @Test
  public void testConstructor() {
    Mockery context = new Mockery();
    DirectedGraph graph = context.mock(DirectedGraph.class);
    @SuppressWarnings("unchecked")
    IdentifiableIntegerMapping<Edge> capacities = new IdentifiableIntegerMapping<>( 0 );
    Node source = new Node( 0 );
    Node sink = new Node( 1 );
    
    DirectedNetwork network = new DirectedNetwork(graph, capacities, source, sink );
    
    assertThat( network.getGraph(), is( same( graph ) ) );
    assertThat( network.getCapacities(), is( same( capacities ) ) );
    assertThat( network.getSource(), is( same( source ) ) );
    assertThat( network.getSink(), is( same( sink ) ) );
    assertThat( network.getSources().size(), is( equalTo( 1 ) ) );
    assertThat( network.getSinks().size(), is( equalTo( 1 ) ) );
  }
}
