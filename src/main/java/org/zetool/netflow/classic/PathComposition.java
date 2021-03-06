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
package org.zetool.netflow.classic;

import org.zetool.netflow.ds.flow.PathBasedFlowOverTime;
import org.zetool.netflow.ds.structure.FlowOverTimePath;
import org.zetool.graph.Edge;
import org.zetool.netflow.ds.flow.EdgeBasedFlowOverTime;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.DirectedGraph;
import org.zetool.netflow.ds.structure.FlowOverTimeEdge;

/**
 *
 * @author Martin Groß
 */
public class PathComposition {

  private DirectedGraph network;
  private EdgeBasedFlowOverTime edgeFlows;
  private PathBasedFlowOverTime pathFlows;
  private IdentifiableIntegerMapping<Edge> transitTimes;
  private int maxFlowRate;

  public PathComposition( DirectedGraph network, IdentifiableIntegerMapping<Edge> transitTimes, PathBasedFlowOverTime pathFlows ) {
    this.network = network;
    this.edgeFlows = new EdgeBasedFlowOverTime( network );
    this.pathFlows = pathFlows;
    this.transitTimes = transitTimes;
    this.maxFlowRate = 0;
  }

  public void run() {
    for( FlowOverTimePath pathFlow : pathFlows ) {
      addPathFlow( pathFlow );
    }
  }

  private void addPathFlow( FlowOverTimePath pathFlow ) {
    Edge edge = pathFlow.firstEdge();
    int time = pathFlow.getFirst().getDelay();
    edgeFlows.get( edge ).increase( time, time + pathFlow.getAmount() / pathFlow.getRate(), pathFlow.getRate() );
    time += transitTime( edge );
    boolean first = true;
    for( FlowOverTimeEdge e : pathFlow ) {
      if( first ) {
        first = false;
        continue;
      }
      time += e.getDelay();
      //System.out.println(e + " " + edgeFlows.get(e));
      edgeFlows.get( e.getEdge() ).increase( time, time + pathFlow.getAmount() / pathFlow.getRate(), pathFlow.getRate() );
      if( edgeFlows.get( e.getEdge() ).get( time ) > maxFlowRate ) {
        maxFlowRate = edgeFlows.get( e.getEdge() ).get( time );
      }
      time += transitTimes.get( e.getEdge() );
    }
  }

  public int getMaxFlowRate() {
    return maxFlowRate;
  }

  public EdgeBasedFlowOverTime getEdgeFlows() {
    return edgeFlows;
  }

  private int transitTime( Edge edge ) {
    return transitTimes.get( edge );
  }
}
