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

package de.tu_berlin.coga.netflow.dynamic.earliestarrival;

import de.tu_berlin.coga.netflow.dynamic.problems.EarliestArrivalFlowProblem;
import de.tu_berlin.math.coga.algorithm.shortestpath.Dijkstra;
import de.tu_berlin.coga.common.algorithm.Algorithm;
import de.tu_berlin.coga.common.algorithm.AlgorithmStatusEvent;
import de.tu_berlin.coga.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.flow.FlowOverTimeImplicit;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the Successive Earliest Arrival Augmenting Path Algorithm as described by Stevanus Tjandra in his Ph.D.
 * thesis.
 * @author Martin Groß
 */
public class SEAAPAlgorithm extends Algorithm<EarliestArrivalFlowProblem, FlowOverTimeImplicit> {
  /** The logger of the main class. */
  private static final Logger log = Logger.getGlobal();

  /**
   * Stores the arrival time of the last flow unit sent (monotonically increasing).
   */
  private int arrivalTime;
  /**
   * Stores whether the residual paths are automatically converted into a flow at the end of the algorithm.
   */
  private boolean autoConvert;
  /**
   * Stores the shortest path distances for each source, sorted ascending.
   */
  private int[] distances;
  /**
   * Stores how much flow has already been sent.
   */
  private int flowUnitsSent;
  /**
   * Stores a reference to the underlying time-expanded network in which the paths are calculated.
   */
  private ImplicitTimeExpandedResidualNetwork implicitTimeExpandedNetwork;
  /**
   * Stores the original time horizon.
   */
  private int originalTimeHorizon;
  /**
   * Stores the current earliest arrival augmenting path in an iteration.
   */
  private EarliestArrivalAugmentingPath path;
  /**
   * Stores a reference to the algorithm for computing earliest arrival augmenting paths.
   */
  private EarliestArrivalAugmentingPathAlgorithm pathAlgorithm;
  /**
   * Stores a reference to the underlying path problem that is solved in each iteration.
   */
  private EarliestArrivalAugmentingPathProblem pathProblem;
  /**
   * Stores the residual paths computed by the algorithm.
   */
  private LinkedList<EarliestArrivalAugmentingPath> paths;

  /**
   * Creates a new instance of the Successive Earliest Arrival Augmenting Path Algorithm as described by Tjandra. The
   * residual paths are automatically converted into a flow by uncrossing at the end.
   */
  public SEAAPAlgorithm() {
    this( true );
  }

  /**
   * Creates a new instance of the Successive Earliest Arrival Augmenting Path Algorithm as described by Tjandra. The
   * residual paths are automatically converted into a flow by uncrossing at the end.
   * @param autoConvert whether the paths should be converted into a flow at the end of the algorithm. Switching this
   * off is only useful for benchmarking purposes.
   */
  public SEAAPAlgorithm( boolean autoConvert ) {
    pathAlgorithm = new EarliestArrivalAugmentingPathAlgorithm();
    this.autoConvert = autoConvert;
  }

  /**
   * Implements the actual algorithm.
   * @param problem the problem that the algorithm is to solve.
   * @return an earliest arrival flow from the specified problem, or <code>null</code>, if automatic conversion of paths
   * into a flow is switched off.
   */
  @Override
  protected FlowOverTimeImplicit runAlgorithm( EarliestArrivalFlowProblem problem ) {
    // Initialize the data structures
    calculateShortestPathLengths();
    flowUnitsSent = 0;
    implicitTimeExpandedNetwork = new ImplicitTimeExpandedResidualNetwork( problem );

    originalTimeHorizon = problem.getTimeHorizon();

    pathProblem = new EarliestArrivalAugmentingPathProblem( implicitTimeExpandedNetwork, implicitTimeExpandedNetwork.superSource(), problem.getSink(), Math.min( getNextDistance( 0 ) + 1, problem.getTimeHorizon() ) );
    paths = new LinkedList<>();
    // If there are no supplies, we are done
    if( problem.getTotalSupplies() == 0 ) {
      return new FlowOverTimeImplicit( implicitTimeExpandedNetwork, paths );
    }

    pathAlgorithm.setProblem( pathProblem );
    // Compute our first augmenting path
    calculateEarliestArrivalAugmentingPath();
    while( !path.isEmpty() && path.getCapacity() > 0 && flowUnitsSent < problem.getTotalSupplies() ) {
      // Add the path
      paths.add( path );
      implicitTimeExpandedNetwork.augmentPath( path );
      // Update the amount of flow sent
      path.setCapacity( Math.min( path.getCapacity(), problem.getTotalSupplies() - flowUnitsSent ) );
      flowUnitsSent += path.getCapacity();
      log.log( Level.FINEST, "Found path {0} with capacity {1}", new Object[]{path.toString(), path.getCapacity()});
      //System.out.println( "Progress: " + (flowUnitsSent * 1.0 / problem.getTotalSupplies()) );
      fireProgressEvent( flowUnitsSent * 1.0 / problem.getTotalSupplies(), String.format( "%1$s von %2$s Personen evakuiert.", flowUnitsSent, problem.getTotalSupplies() ) );
      // Compute the next path
      calculateEarliestArrivalAugmentingPath();
    }
    // Convert our paths into a flow, if desired
    if( autoConvert ) {
      fireEvent( new AlgorithmStatusEvent( this, "INIT_PATH_DECOMPOSITION" ) );
      FlowOverTimeImplicit flow = new FlowOverTimeImplicit( implicitTimeExpandedNetwork, paths );
      return flow;
    } else {
      return null;
    }
  }

  int total;

  private void calculateEarliestArrivalAugmentingPath() {
    // If all flow units have been sent, we are done.
    if( flowUnitsSent == getProblem().getTotalSupplies() ) {
      path = new EarliestArrivalAugmentingPath();
      return;
    }
    boolean pathFound = false;
    while( !pathFound ) {
      // Compute a path with the current time horizon
      pathAlgorithm.run();
      path = pathAlgorithm.getSolution();
      // If no suitable path has been found
      if( path.isEmpty() || path.getCapacity() == 0 ) {
        // Try to increase the time horizon
        int newTimeHorizon = Math.max( pathProblem.getTimeHorizon() + 1, 0 );
        // If our new time horizon gets to large, we are done, otherwise update the time horizon
        if( newTimeHorizon > originalTimeHorizon ) {
          path = new EarliestArrivalAugmentingPath();
          return;
        } else {
          pathProblem.setTimeHorizon( newTimeHorizon );
        }
      } else {
        pathFound = true;
      }
    }
    if( !path.isEmpty() && path.getCapacity() > 0 ) {
      arrivalTime = path.getArrivalTime();
      total += arrivalTime * path.getCapacity();
    }
  }

  private void calculateShortestPathLengths() {
    distances = new int[getProblem().getSources().size()];
    int index = 0;
    Dijkstra dijkstra = new Dijkstra( getProblem().getNetwork(), getProblem().getTransitTimes(), getProblem().getSink(), true );
    dijkstra.run();
    for( Node source : getProblem().getSources() ) {
      distances[index++] = dijkstra.getDistance( source );
    }
    Arrays.sort( distances );
  }

  private int getNextDistance( int currentDistance ) {
    return currentDistance + 1;
    /*
     int index = Arrays.binarySearch(distances, currentDistance + 1);
     if (index >= 0) {
     return currentDistance + 1;
     } else {
     return distances[-index - 1];
     }*/
  }
}
