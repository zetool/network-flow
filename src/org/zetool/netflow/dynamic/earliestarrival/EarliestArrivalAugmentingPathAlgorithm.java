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

import org.zetool.common.algorithm.Algorithm;
import org.zetool.graph.Edge;
import org.zetool.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import org.zetool.graph.Node;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.container.mapping.IdentifiableObjectMapping;
import org.zetool.container.mapping.TimeIntegerMapping;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Implements the Earliest Arrival Augmentation Path Algorithm based on Stevanus Tjandra's Ph.D. thesis.
 * @author Martin Groß
 */
public class EarliestArrivalAugmentingPathAlgorithm extends Algorithm<EarliestArrivalAugmentingPathProblem, EarliestArrivalAugmentingPath> {

  /**
   * An internal reference for a non-existing node.
   */
  private static final Node EMPTY_NODE = new Node( -1 );
  /**
   * Stores the time at which we left the predecessor of a node to reach it at a point in time.
   */
  private transient IdentifiableObjectMapping<Node, TimeIntegerMapping> departureTimes;
  /**
   * Stores the earliest arrival times for nodes.
   */
  private transient IdentifiableIntegerMapping<Node> labels;
  /**
   * Stores a reference to the underlying network of the problem.
   */
  private transient ImplicitTimeExpandedResidualNetwork network;
  /**
   * Stores the node from which a node was reached from at a point in time.
   */
  private transient IdentifiableObjectMapping<Node, Node[]> predecessorNodes;
  /**
   * Stores the edge used to reach a specific node at a point in time.
   */
  private transient IdentifiableObjectMapping<Node, Edge[]> predecessorEdges;
  /**
   * Stores a reference to the underlying problem.
   */
  private transient EarliestArrivalAugmentingPathProblem problem;

  /**
   * Implements the algorithm specified by Stevanus Tjandra in his Ph.D. thesis.
   * @param problem the problem instance that the algorithm should solve.
   * @return an <code>EarliestArrivalAugmentingPath</code> according to Tjandra's definition or an empty path object, if
   * no such path exists.
   */
  @Override
  protected EarliestArrivalAugmentingPath runAlgorithm( EarliestArrivalAugmentingPathProblem problem ) {
    // Store some references for easier access
    this.problem = problem;
    network = problem.getNetwork();
    int timeHorizon = problem.getTimeHorizon();
    // Initialize predecessor and departure time data structures for nodes
    departureTimes = new IdentifiableObjectMapping<>( network.nodes() );
    predecessorNodes = new IdentifiableObjectMapping<>( network.nodes() );
    predecessorEdges = new IdentifiableObjectMapping<>( network.nodes() );
    for( Node node : network.nodes() ) {
      predecessorNodes.set( node, new Node[timeHorizon] );
      predecessorEdges.set( node, new Edge[timeHorizon] );
      departureTimes.set( node, new TimeIntegerMapping() );
      predecessorNodes.get( node )[0] = null;
      departureTimes.get( node ).set( 0, Integer.MAX_VALUE );
    }
    // Initialize predecessor nodes and departure times for the source
    for( int time = 0; time < timeHorizon; time++ ) {
      departureTimes.get( problem.getSource() ).set( time, time );
      predecessorNodes.get( problem.getSource() )[time] = EMPTY_NODE;
    }
    // Initialize the labels (i.e. the earliest arrival times of the nodes)
    labels = new IdentifiableIntegerMapping( network.nodes() );
    for( Node node : network.nodes() ) {
      labels.set( node, Integer.MAX_VALUE );
    }
    labels.set( problem.getSource(), 0 );
    // Start finding an earliest arrival augmenting path from the source
    Queue<Node> candidates = new LinkedList<>();
    candidates.add( problem.getSource() );
    while( !candidates.isEmpty() ) {
      // Choose an unprocessed node
      Node node = candidates.poll();
      // Look at all outgoing edges
      for( Edge edge : network.outgoingEdges( node ) ) {
        // Look at all possible times
        for( int time = labels.get( edge.start() ); time < timeHorizon - Math.max( network.transitTime( edge ), 0 ); time++ ) {
          // Skip time steps where there is no residual capacity or there is no predecessor to reach our current node at that point
          if( time + network.transitTime( edge ) < 0 || network.capacity( edge, time ) == 0 || predecessorNodes.get( edge.start() )[time] == null ) {
            continue;
          }
          Node[] predecessorNodeOfEdgeEnd = predecessorNodes.get( edge.end() );
          // If the node at the end of the edge has not been reached yet at the time our current edge can reach it
          if( predecessorNodeOfEdgeEnd[time + network.transitTime( edge )] == null ) {
            // If we can use this edge to arrive earlier then we could previously, update the earliest arrival label for the end node
            if( labels.get( edge.end() ) > time + network.transitTime( edge ) ) {
              labels.set( edge.end(), time + network.transitTime( edge ) );
            }
            // Set the predecessor & corresponding departure time for the end node of our current edge
            predecessorNodeOfEdgeEnd[time + network.transitTime( edge )] = edge.start();
            predecessorEdges.get( edge.end() )[time + network.transitTime( edge )] = edge;
            departureTimes.get( edge.end() ).set( time + network.transitTime( edge ), time );
            // Add the end nodes to the candidate set
            candidates.add( edge.end() );
            // Check whether we can use waiting at the end node to reach more node copies
            int newTime = time + network.transitTime( edge ) + 1;
            while( newTime < timeHorizon && network.capacity( edge.end(), newTime - 1 ) > 0 && predecessorNodeOfEdgeEnd[newTime] == null ) {
              predecessorNodeOfEdgeEnd[newTime] = edge.end();
              predecessorEdges.get( edge.end() )[newTime] = null;
              departureTimes.get( edge.end() ).set( newTime, newTime - 1 );
              newTime++;
            }
            // Check whether we can use wait-cancelling at the end node to reach more node copies
            if( edge.end().id() == problem.getSink().id() ) {
              continue;
            }
            newTime = time + network.transitTime( edge ) - 1;
            while( newTime >= 0 && network.capacity( edge.end(), newTime + 1, true ) > 0 && predecessorNodeOfEdgeEnd[newTime] == null ) {
              if( labels.get( edge.end() ) > newTime ) {
                labels.set( edge.end(), newTime );
              }
              predecessorNodeOfEdgeEnd[newTime] = edge.end();
              predecessorEdges.get( edge.end() )[newTime] = null;
              departureTimes.get( edge.end() ).set( newTime, newTime + 1 );
              newTime--;
            }
          }
        }
      }
    }
    // Construct the actual path out of this data
    return constructPath();
  }

  /**
   * Construct an <code>EarliestArrivalAugmentingPath</code> out of the information generated by the algorithm.
   * @return an <code>EarliestArrivalAugmentingPath</code>.
   */
  protected EarliestArrivalAugmentingPath constructPath() {
    EarliestArrivalAugmentingPath path = new EarliestArrivalAugmentingPath();
    if( labels.get( problem.getSink() ) >= problem.getTimeHorizon() ) {
      // If we cannot reach the sink interface the time horizon, return an empty path.
      return path;
    } else {
      // Our path begins at the sink
      Node currentNode = problem.getSink();
      int currentTime = labels.get( problem.getSink() );
      Node predecessorNode = predecessorNodes.get( currentNode )[currentTime];
      Edge predecessorEdge = predecessorEdges.get( currentNode )[currentTime];
      path.insertFirst( currentNode, currentTime, currentTime );
      int pathBottleneckCapacity = Integer.MAX_VALUE;
      int predecessorDepartureTime;
      int nextTime = 0;
      // As long as we haven't traced our path back to the source...
      while( currentNode != EMPTY_NODE && predecessorNode != EMPTY_NODE ) {
        // Determine the time at which we leave the predecessor
        predecessorDepartureTime = departureTimes.get( currentNode ).get( currentTime );
        // Update the path bottleneck capacity
        int capacity;
        if( currentNode != predecessorNode ) {
          capacity = network.capacity( predecessorEdge, predecessorDepartureTime );
          nextTime = predecessorDepartureTime;
        } else if( currentTime > predecessorDepartureTime ) {
          capacity = network.capacity( predecessorNode, predecessorDepartureTime );
        } else {
          capacity = network.capacity( predecessorNode, predecessorDepartureTime, true );
        }
        pathBottleneckCapacity = Math.min( pathBottleneckCapacity, capacity );
        // The predecessor becomes the current node
        Node oldPredecessor = predecessorNode;
        currentNode = predecessorNode;
        currentTime = predecessorDepartureTime;
        predecessorEdge = predecessorEdges.get( currentNode )[currentTime];
        predecessorNode = predecessorNodes.get( currentNode )[currentTime];
        // Create a path segment
        if( oldPredecessor != predecessorNode/* && !oldPredecessor.equals(path.getFirst().getNode())*/ ) {
          predecessorDepartureTime = departureTimes.get( currentNode ).get( currentTime );
          int transitTime;
          if( predecessorNode != EMPTY_NODE && currentNode != predecessorNode ) {
            transitTime = network.transitTime( predecessorEdge );
          } else {
            transitTime = 0;
          }
          //assert !oldPredecessor.equals(path.getFirst().getNode()) : "Cycle: " + oldPredecessor + " " + predecessorNode + " " + path.getFirst();
          if( oldPredecessor.equals( path.getFirst().getNode() ) ) {
            //System.out.println(Arrays.toString(predecessorNodes.get(currentNode)));
          }
          path.insertFirst( oldPredecessor, predecessorDepartureTime + transitTime, nextTime );
        }
      }
      // Set the capacity to the bottleneck capacity
      path.setCapacity( pathBottleneckCapacity );
      // Return the path
      return path;
    }
  }
}
