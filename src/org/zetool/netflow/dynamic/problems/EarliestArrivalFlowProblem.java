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
package org.zetool.netflow.dynamic.problems;

import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Groß
 */
public class EarliestArrivalFlowProblem extends DynamicTransshipmentProblem {

//    private IdentifiableIntegerMapping<Edge> edgeCapacities;
//		private AbstractNetwork network;
//    private IdentifiableIntegerMapping<Edge> transitTimes;
//    private IdentifiableIntegerMapping<Node> nodeCapacities;
  protected Node supersink;
  protected List<Node> sources;
//    private IdentifiableIntegerMapping<Node> supplies;
//    private int timeHorizon;
  private int totalSupplies;

  public EarliestArrivalFlowProblem( IdentifiableIntegerMapping<Edge> edgeCapacities, DirectedGraph graph, IdentifiableIntegerMapping<Node> nodeCapacities, Node sink, List<Node> sources, int timeHorizon, IdentifiableIntegerMapping<Edge> transitTimes, IdentifiableIntegerMapping<Node> supplies ) {
    super( edgeCapacities, graph, nodeCapacities, timeHorizon, transitTimes, supplies );
			//this.edgeCapacities = edgeCapacities;
    //  this.network = network;
    //       this.nodeCapacities = nodeCapacities;
    this.supersink = sink;
    this.sources = sources;
 //       this.supplies = supplies;
    //       this.timeHorizon = timeHorizon;
    //  this.transitTimes = transitTimes;
    for( Node source : sources ) {
      totalSupplies += supplies.get( source );
    }
  }

  public EarliestArrivalFlowProblem( DynamicTransshipmentProblem dyn ) {
    super( dyn.getEdgeCapacities(), dyn.getNetwork(), dyn.getNodeCapacities(), dyn.getTimeHorizon(), dyn.getTransitTimes(), dyn.getSupplies() );

    sources = new LinkedList<>();
    Node supersink = null;
    for( Node node : getNetwork().nodes() ) {
      if( getSupplies().get( node ) > 0 ) {
        sources.add( node );
        totalSupplies += getSupplies().get( node );
      }
      if( getSupplies().get( node ) < 0 ) {
        supersink = node;
      }
    }
    this.supersink = supersink;
  }

  /**
   * Sets a new time horizon for the instance. Use this if a time horizon has
   * changed, for example if an estimator has been used.
   *
   * @param timeHorizon the new time horizon
   */
//		public void setTimeHorizon( int timeHorizon ) {
//			this.timeHorizon = timeHorizon;
//		}
//    public IdentifiableIntegerMapping<Edge> getEdgeCapacities() {
//        return edgeCapacities;
//    }
//    public AbstractNetwork getNetwork() {
//        return network;
//    }
//    public IdentifiableIntegerMapping<Node> getNodeCapacities() {
//        return nodeCapacities;
//    }
  public Node getSink() {
    return supersink;
  }

  public List<Node> getSources() {
    return sources;
  }

//    public IdentifiableIntegerMapping<Node> getSupplies() {
//        return supplies;
//    }    
//    
//    public int getTimeHorizon() {
//        return timeHorizon;
//    }
  public int getTotalSupplies() {
    return totalSupplies;
  }

//    public IdentifiableIntegerMapping<Edge> getTransitTimes() {
//        return transitTimes;
//    }
  
  @Override
  public String toString() {
    return "EarliestArrivalFlowProblem{\n" + "edgeCapacities=" + getEdgeCapacities() + "\n, network=" + getNetwork() + "\n, nodeCapacities=" + getNodeCapacities() + "\n, sink=" + supersink + "\n, sources=" + sources + "\n, supplies=" + getSupplies() + "\n, timeHorizon=" + getTimeHorizon() + "\n, totalSupplies=" + totalSupplies + "\n, transitTimes=" + getTransitTimes() + '}';
  }
}
