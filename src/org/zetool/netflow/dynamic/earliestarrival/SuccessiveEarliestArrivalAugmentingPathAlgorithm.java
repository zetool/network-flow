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

package org.zetool.netflow.dynamic.earliestarrival;

import org.zetool.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import org.zetool.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.FlowOverTimeImplicit;
import java.util.LinkedList;
import org.zetool.common.algorithm.Algorithm;

/**
 *
 * @author Martin Groß
 */
public class SuccessiveEarliestArrivalAugmentingPathAlgorithm extends Algorithm<EarliestArrivalFlowProblem, FlowOverTimeImplicit> {

  @Override
  protected FlowOverTimeImplicit runAlgorithm( EarliestArrivalFlowProblem problem ) {
    ImplicitTimeExpandedResidualNetwork implicitResidualNetwork = new ImplicitTimeExpandedResidualNetwork( problem );
    System.out.println( "Time horizon: " + problem.getTimeHorizon() );
    EarliestArrivalAugmentingPathProblem pathProblem = new EarliestArrivalAugmentingPathProblem( implicitResidualNetwork, implicitResidualNetwork.superSource(), problem.getSink(), problem.getTimeHorizon() );
    EarliestArrivalAugmentingPathAlgorithm pathAlgorithm = new EarliestArrivalAugmentingPathAlgorithm();
    pathAlgorithm.setProblem( pathProblem );
    pathAlgorithm.run();
    EarliestArrivalAugmentingPath path = pathAlgorithm.getSolution();
    int flowUnitsSent = 0;
    int flowUnitsTotal = 0;
    for( Node source : problem.getSources() ) {
      flowUnitsTotal += problem.getSupplies().get( source );
    }
    LinkedList<EarliestArrivalAugmentingPath> paths = new LinkedList<>();
    while( !path.isEmpty() && path.getCapacity() > 0 ) {
      flowUnitsSent += path.getCapacity();
      System.out.println( flowUnitsSent );
      fireProgressEvent( flowUnitsSent * 1.0 / flowUnitsTotal, String.format( "%1$s von %2$s Personen evakuiert.", flowUnitsSent, flowUnitsTotal ) );
      paths.add( path );
      implicitResidualNetwork.augmentPath( path );
      pathAlgorithm = new EarliestArrivalAugmentingPathAlgorithm();
      pathAlgorithm.setProblem( pathProblem );
      pathAlgorithm.run();
      path = pathAlgorithm.getSolution();
    }

    FlowOverTimeImplicit flow = new FlowOverTimeImplicit( implicitResidualNetwork, paths );

    if( flow.getFlowAmount() != problem.getTotalSupplies() ) {
      System.out.println( flow.getFlowAmount() + " vs. " + problem.getTotalSupplies() );
    }

    return flow;
  }
}
