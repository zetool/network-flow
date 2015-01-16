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

package org.zetool.netflow.ds.network;

import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;
import org.zetool.graph.Edge;
import org.zetool.graph.Graph;
import org.zetool.graph.Node;
import org.zetool.graph.UndirectedGraph;
import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface ResidualNetwork extends ResidualGraph, Network {

  
  public static ResidualNetwork getResidualNetwork( Graph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    if( graph instanceof DirectedGraph ) {
      return new DirectedResidualNetwork((DirectedGraph)graph, capacities, sources, sinks );
    } else if( graph instanceof UndirectedGraph ) {
      return new UndirectedResidualNetwork((UndirectedGraph)graph, capacities, sources, sinks );
    } else {
      throw new AssertionError( "Only DirectedGraph and UndirectedGraph are supported" );
    }
  }

  public static ResidualNetwork getResidualNetwork( Graph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    if( graph instanceof DirectedGraph ) {
      return new DirectedResidualNetwork((DirectedGraph)graph, capacities, source, sinks );
    } else if( graph instanceof UndirectedGraph ) {
      return new UndirectedResidualNetwork((UndirectedGraph)graph, capacities, source, sinks );
    } else {
      throw new AssertionError( "Only DirectedGraph and UndirectedGraph are supported" );
    }
  }

  public static ResidualNetwork getResidualNetwork( Graph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    if( graph instanceof DirectedGraph ) {
      return new DirectedResidualNetwork((DirectedGraph)graph, capacities, sources, sink );
    } else if( graph instanceof UndirectedGraph ) {
      return new UndirectedResidualNetwork((UndirectedGraph)graph, capacities, sources, sink );
    } else {
      throw new AssertionError( "Only DirectedGraph and UndirectedGraph are supported" );
    }
  }

  public static ResidualNetwork getResidualNetwork( Graph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
      if( graph instanceof DirectedGraph ) {
      return new DirectedResidualNetwork((DirectedGraph)graph, capacities, source, sink );
    } else if( graph instanceof UndirectedGraph ) {
      return new UndirectedResidualNetwork((UndirectedGraph)graph, capacities, source, sink );
    } else {
      throw new AssertionError( "Only DirectedGraph and UndirectedGraph are supported" );
    }
  }
}
  
