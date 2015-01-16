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

package de.tu_berlin.coga.netflow.classic.problems;

import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Node;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Groß
 */
public class TransshipmentProblem {

  private IdentifiableIntegerMapping<Edge> capacities;
  private DirectedGraph graph;
  private IdentifiableIntegerMapping<Node> supplies;
  private transient List<Node> sinks;
  private transient List<Node> sources;

  public TransshipmentProblem( DirectedGraph graph, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Node> supplies ) {
    this.capacities = capacities;
    this.graph = graph;
    this.supplies = supplies;
    sources = new LinkedList<>();
    sinks = new LinkedList<>();
    for( Node node : graph.nodes() ) {
      if( supplies.get( node ) < 0 ) {
        sinks.add( node );
      }
      if( supplies.get( node ) > 0 ) {
        sources.add( node );
      }
    }
  }

  public IdentifiableIntegerMapping<Edge> getCapacities() {
    return capacities;
  }

  public void setCapacities( IdentifiableIntegerMapping<Edge> capacities ) {
    this.capacities = capacities;
  }

  public DirectedGraph getNetwork() {
    return graph;
  }

  public void setGraph( DirectedGraph graph ) {
    this.graph = graph;
  }

  public List<Node> getSinks() {
    return sinks;
  }

  public List<Node> getSources() {
    return sources;
  }

  public IdentifiableIntegerMapping<Node> getSupplies() {
    return supplies;
  }

  public void setSupplies( IdentifiableIntegerMapping<Node> supplies ) {
    this.supplies = supplies;
  }
}
