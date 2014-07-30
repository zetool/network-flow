/* zet evacuation tool copyright (c) 2007-09 zet evacuation team
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
package de.tu_berlin.coga.netflow.dynamic.transshipment;

import de.tu_berlin.coga.netflow.dynamic.problems.DynamicTransshipmentProblem;
import de.tu_berlin.coga.netflow.classic.transshipment.StaticTransshipment;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.netflow.ds.network.TimeExpandedNetwork;

/**
 * The class {@code DynamicTransshipment} tests whether it is possible to
 * fulfill given supplies and demands in a network with capacities and transit
 * times within a given time horizon. If this is possible, a flow that satisfies
 * the supplies and demands is computed.
 *
 * The algorithm to find the dynamic transshipment (or to say that non exists)
 * is implemented by calling a static transshipment algorithm on the
 * time-expanded network. Therefore the classes {@link TimeExpandedNetwork} and
 * {@link StaticTransshipment} are used.
 */
public class DynamicTransshipment extends TransshipmentWithTimeHorizon<DynamicTransshipmentProblem> {

  /**
   * Creates a new instance of the dynamic transshipment algorithm by calling
   * the super constructor and setting the name of the algorithm to "Dynamic
   * Transshipment".
   */
  public DynamicTransshipment() {
    setName( "Dynamic Transshipment" );
  }

  @Override
  protected IdentifiableIntegerMapping<Edge> transshipmentWithTimeHorizon( TimeExpandedNetwork tnetwork ) {
    // Create an static transshipment algorithm object with the time expanded network, its capacities and supplies / demands.
    StaticTransshipment statTrAlgo = new StaticTransshipment( tnetwork, tnetwork.capacities(), tnetwork.supplies() );
    // Run algorithm and get resulting flow.
    statTrAlgo.run();
    IdentifiableIntegerMapping<Edge> flow = statTrAlgo.getFlow();
    return flow;
  }
}
