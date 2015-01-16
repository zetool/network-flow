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
import org.zetool.graph.Edge;
import org.zetool.graph.Graph;
import org.zetool.graph.structure.Path;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface ResidualGraph extends Graph {
  /**
   * Returns the reverse edge of the specified edge. The reverse edge of an reverse edge is the original
   * edge again.
   * @param edge the edge for which the reverse edge is to be returned.
   * @return the reverse edge of the specified edge.
   */
  public Edge reverseEdge( Edge edge );

  /**
   * Checks is whether the specified edge is a reverse edge. An edge is called reverse if it does not exist in the
   * original graph.
   * @param edge the edge to be tested.
   * @return {@code true} if the specified edge is a reverse edge, {@code false} otherwise.
   */
  public boolean isReverseEdge( Edge edge );

  /**
   * Augments flow. Increased on original edges, decreased on reverse edges.
   * @param edge the edge
   * @param amount the amount
   */
  void augmentFlow( Edge edge, int amount );
  
  /**
   * Augments flow on a path, e.g. augments the flow by the given amount on
   * each edge in the path.
   * @param path the path
   * @param amount the amount
   */
  default void augmentFlow( Path path, int amount ) {
    for( Edge e : path ) {
      augmentFlow( e, amount );
    }
  }

  /**
   * The residual capacity of an edge.
   * @param edge the edge
   * @return the residual capacity of the edge
   */
  int residualCapacity( Edge edge );

  /**
   * Returns the flow associated with this residual graph. Runtime O(1).
   * @return the flow associated with this residual graph.
   */
  public IdentifiableIntegerMapping<Edge> flow();
}
