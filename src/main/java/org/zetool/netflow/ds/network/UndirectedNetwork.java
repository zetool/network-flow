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

import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.graph.UndirectedGraph;
import java.util.List;

/**
 *
 * @author kapman
 */
public class UndirectedNetwork extends GeneralNetwork implements UndirectedGraph {
  // TODO: replace constructors and change to a builder that maybe also automatically builds the network!

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, List<Node> sinks ) {
    super( graph, capacities, sources, sinks );
  }

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, List<Node> sinks ) {
    super( graph, capacities, source, sinks );
  }

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, List<Node> sources, Node sink ) {
    super( graph, capacities, sources, sink );
  }

  public UndirectedNetwork( UndirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, Node source, Node sink ) {
    super( graph, capacities, source, sink );
  }

  @Override
  public UndirectedGraph getGraph() {
    return (UndirectedGraph)super.getGraph();
  }

  //******************************************************************************************************
  // Delegated methods of the graph of the network such that the network can be accessed as a graph itself

}
