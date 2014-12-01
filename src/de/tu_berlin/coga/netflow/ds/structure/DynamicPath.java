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
package de.tu_berlin.coga.netflow.ds.structure;

import de.tu_berlin.coga.container.collection.ListSequence;
import de.tu_berlin.coga.graph.structure.StaticPath;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.structure.Path;
import de.tu_berlin.coga.netflow.ds.network.Network;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The {@code DynamicPath} class represents a dynamic path in a {@link Network}.
 * It extends the {@link StaticPath} by the possibility to save delay times in
 * nodes: Flow going through a network can (sometimes) wait in nodes, therefore
 * we consider a dynamic flow as a alternating sequence of delay times and
 * edges. In this class delay times get saved together with edges: The delay
 * time corresponding to an edge is the delay time in the start node of the
 * edge, i.e. the delays are always directly before the corresponding edge. The
 * delay times are internally stored in a {@code ArrayList}.
 */
public class DynamicPath implements Path {

  private ArrayList<Integer> delays;
  private StaticPath path;

  /**
   * Constructs a new {@code DynamicPath} without edges. Edges can be added with
   * the corresponding methods.
   */
  public DynamicPath() {
    path = new StaticPath();
    delays = new ArrayList<>();
  }

  /**
   * Constructs a new {@code DynamicPath} with the given edges. The delays in
   * the startnode of the edges are set to zero. They can be changed later by
   * using {@code setDelay(Edge edge, int delay}. The edges must be consistent,
   * i.e. the endnode of an edge must be equal to the startnode of the next edge
   * (if there follows one more). If the edges are not consistent, an
   * {@code IllegalArgumentException} is thrown.
   *
   * @param edges the edges the path shall be contained of
   */
  public DynamicPath( Edge... edges ) {
    this();
    for( Edge edge : edges ) {
      this.addLastEdge( edge );
    }
  }

  /**
   * Constructs a new {@code DynamicPath} with the given edges. The delays in
   * the startnode of the edges are set to zero. They can be changed later by
   * using {@code setDelay(Edge edge, int delay}. The edges must be consistent,
   * i.e. the endnode of an edge must be equal to the startnode of the next edge
   * (if there follows one more). If the edges are not consistent, an
   * {@code IllegalArgumentException} is thrown.
   *
   * @param edges the edges the path shall be contained of
   */
  public DynamicPath( Iterable<Edge> edges ) {
    this();
    for( Edge edge : edges ) {
      this.addLastEdge( edge );
    }
  }

  /**
   * Extends the path by adding an edge at the end and sets the delay in the
   * startnode of {@code edge} to {@code delay}. The edge must be consistent to
   * the current last edge of the path, i.e. i.e. the endnode of the current
   * last edge must be equal to the startnode of {@code edge}.
   *
   * @param edge the edge to insert at the end of the path.
   * @param delay the delay in the startnode of {@code edge}
   * @return {@code true} if the insertion was successful, {@code false} else.
   */
  public boolean addLastEdge( Edge edge, int delay ) {
    boolean successful = path.addLastEdge( edge );
    if( successful ) {
      delays.add( delay );
    }
    return successful;
  }

  /**
   * Extends the path by adding an edge at the start and sets the delay in the
   * startnode of {@code edge} to {@code delay}. Until additional edges are
   * added at the start, the delay in this node will be the delay at the
   * beginning of the {@code DynamicPath}, i.e. the time before flow on this
   * path would start leaving the first node. The new edge must be consistent to
   * the current first edge of the path, i.e. i.e. the startnode of the current
   * first edge must be equal to the endnode of {@code edge}.
   *
   * @param edge the edge to insert at the end of the path.
   * @param delay the delay before traversingt he edge
   * @return {@code true} if the insertion was successful, {@code false} else.
   */
  public boolean addFirstEdge( Edge edge, int delay ) {
    boolean successful = path.addFirstEdge( edge );
    if( successful ) {
      delays.add( 0, delay );
    }
    return successful;
  }

  /**
   * Extends the path by adding an edge at the end. The edge must be consistent
   * to the current last edge of the path, i.e. i.e. the endnode of the current
   * last edge must be equal to the startnode of {@code edge}. The delay in the
   * startnode of {@code edge} is set to zero.
   *
   * @param edge the edge to insert at the end of the path.
   * @return {@code true} if the insertion was successful, {@code false} else.
   */
  @Override
  public boolean addLastEdge( Edge edge ) {
    return addLastEdge( edge, 0 );
  }

  /**
   * Extends the path by adding an edge at the start. The edge must be
   * consistent to the current first edge of the path, i.e. i.e. the startnode
   * of the current first edge must be equal to the endnode of {@code edge}. The
   * delay in the startnode of {@code edge} is set to zero.
   *
   * @param edge the edge to insert at the end of the path.
   * @return {@code true} if the insertion was successful, {@code false} else.
   */
  @Override
  public boolean addFirstEdge( Edge edge ) {
    return addFirstEdge( edge, 0 );
  }

  /**
   * Returns an iterator for the edges of this path. With the iterator one can
   * iterate comfortable through all the edges of the path.
   *
   * @return an iterator for the edges of this path.
   */
  @Override
  public Iterator<Edge> iterator() {
    return path.iterator();
  }

  /**
   * Returns the delay in the startnode of an edge. If the edge is not present
   * in the path, -1 will be returned.
   *
   * @param edge the delay in the startnode of this edge is returned.
   * @return the delay in the startnode of {@code edge}.
   */
  public int getDelay( Edge edge ) {
    if( getEdges().contains( edge ) ) {
      int index = getEdges().indexOf( edge );
      if( delays.size() > index ) {
        return delays.get( index );
      } else {
        return 0;
      }
    } else {
      return (-1);
    }
  }

  /**
   * Sets the delay in the startnode of edge {@code edge}. If the edge is not
   * present in the path, nothing happens.
   *
   * @param edge the delay of the startnode of this edge will be set.
   * @param delay the delay to be set.
   */
  public void setDelay( Edge edge, int delay ) {
    if( getEdges().contains( edge ) ) {
      while( path.getEdges().indexOf( edge ) >= delays.size() ) {
        delays.add( 0 );
      }
      delays.set( getEdges().indexOf( edge ), delay );
    }
  }

  /**
   * Shortens the path by removing the first edge. The delay time in the
   * startnode of this edge will also be deleted. If the path is empty, nothing
   * happens.
   *
   * @return {@code false} if there was no element to be removed, {@code true}
   * else.
   */
  @Override
  public boolean removeFirstEdge() {
    boolean successful = path.removeFirstEdge();
    if( successful ) {
      delays.remove( 0 );
      return true;
    } else {
      return false;
    }
  }

  /**
   * Shortens the path by removing the last edge. The delay time in the
   * startnode of this edge will also be deleted. If the path is empty, nothing
   * happens.
   *
   * @return {@code false} if there was no element to be removed, {@code true}
   * else.
   */
  @Override
  public boolean removeLastEdge() {
    boolean successful = path.removeLastEdge();
    if( successful && delays.size() >= getEdges().size() ) {
      delays.remove( delays.size() - 1 );
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns a String containing tuples of delay times and edges. The delay time
   * in a tuple belongs to the startnode of the edge in the tuple. An edge
   * e=(a,b) will be represented by (a,b) in the string.
   *
   * @return a String containing tuples of delay times and edges.
   */
  public String nodesAndDelaysToString() {
    StringBuilder builder = new StringBuilder();
    for( int i = 0; i < getEdges().size(); i++ ) {
      builder.append( "(" + delays.get( i ) + "," + getEdges().get( i ).nodesToString() + ")," );
    }
    if( length() > 0 ) {
      builder.deleteCharAt( builder.length() - 1 );
    }
    return builder.toString();
  }

  /**
   * Returns a String containing tuples of delay times and edge IDs. The delay
   * time in a tuple belongs to the startnode of the edge in the tuple.
   *
   * @return a String containing tuples of delay times and edge IDs.
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for( int i = 0; i < getEdges().size(); i++ ) {
      builder.append( "(" + delays.get( i ) + "," + getEdges().get( i ) + ")," );
    }
    if( length() > 0 ) {
      builder.deleteCharAt( builder.length() - 1 );
    }
    return builder.toString();
  }

  /**
   * Clones this path by cloning all edges and delays and creating a new
   * {@code DynamicPath} object with the clone.
   *
   * @return a {@code Dynamic} object with clones of the edges and delays of
   * this object.
   */
  @Override
  public DynamicPath clone() {
    DynamicPath dynamicPath = new DynamicPath();
    Iterator<Edge> it = getEdges().iterator();
    for( int i = 0; i < getEdges().size(); i++ ) {
      Edge e = (Edge) (it.next()).clone();
      if( delays.size() > i ) {
        dynamicPath.addLastEdge( e, delays.get( i ) );
      } else {
        dynamicPath.addLastEdge( e, 0 );
      }
    }
    return dynamicPath;
  }

  /**
   * Returns the hash code of this dynamic path. The hash code is calculated by
   * computing the arithmetic mean of the delays together with the hash codes of
   * all edges. Therefore the hash code is equal for dynamic path equal
   * according to the {@code equals}-method, but not necessarily different for
   * dynamic path different according to the {@code equals}-method. If hashing
   * of dynamic path is heavily used, the implementation of this method should
   * be reconsidered.
   *
   * @return the hash code of this dynamic path.
   */
  @Override
  public int hashCode() {
    int h = 0;
    for( Integer i : delays ) {
      h += Math.floor( i / (delays.size() + getEdges().size()) );
    }
    for( Edge e : getEdges() ) {
      h += Math.floor( e.hashCode() / (delays.size() + getEdges().size()) );
    }
    return h;
  }

  /**
   * Returns whether an object is equal to this dynamic path. The result is true
   * if and only if the argument is not null and is a {@code DynamicPath} object
   * having a sequence of edges that is equal to this path's sequence of edges
   * (i.e. all edges must have the same IDs) and all the delays are equal.
   *
   * @param o object to compare.
   * @return {@code true} if the given object represents a {@code DynamicPath}
   * equivalent to this node, {@code false} otherwise.
   */
  @Override
  public boolean equals( Object o ) {
    if( o == null || !(o instanceof DynamicPath) ) {
      return false;
    } else {
      DynamicPath p = (DynamicPath) o;
      if( p.getEdges().size() != getEdges().size() ) {
        return false;
      }
      Iterator<Edge> it = getEdges().iterator();
      for( Edge e : p.getEdges() ) {
        if( !e.equals( it.next() ) || getDelay( e ) != p.getDelay( e ) ) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public Edge first() {
    return path.first();
  }

  @Override
  public ListSequence<Edge> getEdges() {
    return path.getEdges();
  }

  @Override
  public Edge last() {
    return path.last();
  }

  @Override
  public int length() {
    return path.length();
  }

  @Override
  public String nodesToString() {
    return path.nodesToString();
  }

  @Override
  public Node start() {
    return path.start();
  }

  @Override
  public Node end() {
    return path.end();
  }
}
