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

package de.tu_berlin.coga.netflow.util;

import org.zetool.container.collection.ListSequence;
import de.tu_berlin.coga.graph.Node;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.container.mapping.IdentifiableObjectMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.traversal.DepthFirstSearch;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides a method to check a supplies mapping for a given network. The method tests whether all sources in
 * the network are connected to sinks. If a source is found that is not connected to any sink, the method will delete
 * the source if there is only one sink. If there are more sinks, it cannot be decided where to subtract the
 * corresponding need. In this case an exception is thrown.
 */
public class GraphInstanceChecker {

  private ListSequence<Node> sources, sinks;
  private LinkedList<Node> newSources;
  private LinkedList<Node> deletedSources;
  private DirectedGraph network;
  private IdentifiableIntegerMapping<Node> supplies;
  private IdentifiableIntegerMapping<Node> newSupplies;
  boolean hasRun = false;

  /**
   * Creates a new instance of the checker.
   * @param network the network to be checked.
   * @param supplies the supplies of the network.
   */
  public GraphInstanceChecker( DirectedGraph network,
          IdentifiableIntegerMapping<Node> supplies ) {
    this.network = network;
    this.supplies = supplies;
  }

  /**
   * Returns whether the network has already been checked.
   * @return whether the network has already been checked.
   */
  public boolean hasRun() {
    return hasRun;
  }

  /**
   * If the algorithm has already been called, this method gives the resulting supply mapping. Else an exception is
   * thrown.
   * @return If the algorithm has already been called, this method gives the resulting supply mapping. Else an exception
   * is thrown.
   */
  public IdentifiableIntegerMapping<Node> getNewSupplies() {
    if( hasRun ) {
      return newSupplies;
    } else {
      throw new AssertionError( "supplyChecker has to be called first." );
    }
  }

  /**
   * If the algorithm has already been called, this method gives the resulting list of sources. Else an exception is
   * thrown.
   * @return If the algorithm has already been called, this method gives the resulting list of sources. Else an
   * exception is thrown.
   */
  public List<Node> getNewSources() {
    if( hasRun ) {
      return Collections.unmodifiableList( newSources );
    } else {
      throw new AssertionError( "supplyChecker has to be called first." );
    }
  }

  /**
   * If the algorithm has already been called, this method gives the list of deleted sources. Else an exception is
   * thrown.
   * @return If the algorithm has already been called, this method gives the list of deleted sources. Else an exception
   * is thrown.
   */
  public List<Node> getDeletedSources() {
    if( hasRun ) {
      return Collections.unmodifiableList( deletedSources );
    } else {
      throw new AssertionError( "supplyChecker has to be called first." );
    }
  }

  /**
   * This method tests whether all sources in the network are connected to sinks. If a source is found that is not
   * connected to any sink, the method will delete the source if there is only one sink. If there are more sinks, it
   * cannot be decided where to subtract the corresponding need. In this case an exception is thrown.
   */
  public void supplyChecker() {

    /* Find all sources and sinks. */
    sources = new ListSequence<>();
    sinks = new ListSequence<>();
    for( Node node : network.nodes() ) {
      if( !supplies.isDefinedFor( node ) ) {
        throw new AssertionError(
                "There is a node in the network object that has no defined supply according to the supplies-Mapping." );
      } else {
        if( supplies.get( node ) > 0 ) {
          sources.add( node );
        }
        if( supplies.get( node ) < 0 ) {
          sinks.add( node );
        }
      }
    }

    /* A mapping to save which sources can reach a sink.
     * We will search inverse (from sinks to sources),
     * in this scenario each source has to be reachable.
     */
    IdentifiableObjectMapping<Node, Boolean> reachable
            = new IdentifiableObjectMapping<>( sources.size() ); // TODO identifiableboolean mapping
    int reachableSources = 0;

    /* Go through all sinks. */
    for( Node sink : sinks ) {
      /* Call depth first search. */
      DepthFirstSearch dfs = new DepthFirstSearch();
      dfs.setProblem( network );
      dfs.setStart( sink );
      dfs.setReverse( true );
      dfs.setStart( sink );
      dfs.run();
      for( Node source : sources ) {
        /* Check whether the source is reachable.*/
        if( dfs.getNumber( source ) != 0 ) {
          reachableSources++;
          reachable.set( source, true );
        } else {
          reachable.set( source, false );
        }
      }
    }

    newSources = sources;
    deletedSources = new LinkedList<>();

    /* Check whether there are error sources. */
    if( sources.size() > reachableSources ) {
      /* This case can't be repaired automatically if you want to subtract supply and need symmetrically. */
      if( sinks.size() != 1 ) {
        throw new AssertionError( "There are sources that cannot reach any sink, and there are " + sinks.size()
                + " sinks. This method can only automatically repairthe network for exactly one sink." );
      }
      /* Now there is only one sink. Set the supply of each non reachable source to zero and
       * subtract the corresponding value from the need of the sink. */
      newSources = new LinkedList<>();
      Node sink = sinks.first();
      for( Node source : sources ) {
        if( !reachable.get( source ) ) {
          supplies.set( sink, supplies.get( sink ) + supplies.get( source ) );
          supplies.set( source, 0 );
          deletedSources.add( source );
        } else {
          newSources.add( source );
        }
      }
    }

    /* sets the corrected mapping as the new supply mapping. */
    newSupplies = supplies;
    hasRun = true;
  }

  /**
   * Checks whether there are no supplies in the network, i. e. true is returned if there are no supplies and demands.
   * If there are supplies and demands, but supplies=-demands does not hold, an exception is thrown. If
   * supplies=-demands holds and is not zero, false is returned.
   * @param network the network to be checked.
   * @param supplies the supply mapping to be checked.
   * @return {@code true} if no demands and supplies are given, {@code false} otherwise
   */
  public static boolean emptySupplies( DirectedGraph network, IdentifiableIntegerMapping<Node> supplies ) {
    int sup = 0, dem = 0;
    for( Node node : network.nodes() ) {
      if( supplies.get( node ) > 0 ) {
        sup += supplies.get( node );
      }
      if( supplies.get( node ) < 0 ) {
        dem += supplies.get( node );
      }
    }
    if( sup == -dem && sup > 0 ) {
      return false;
    }
    if( sup == -dem && sup == 0 ) {
      return true;
    }
    if( sup != -dem ) {
      throw new AssertionError( "Number of supplies and demands is different, supplies: " + sup + ", demands: " + dem + "." );
    }
    throw new RuntimeException( "Unknown error in method 'emptySupplies'." );
  }

}
