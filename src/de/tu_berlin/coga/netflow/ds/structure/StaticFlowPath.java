/* zet evacuation tool copyright (c) 2007-10 zet evacuation team
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

import ds.graph.GraphLocalization;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.graph.structure.StaticPath;
import java.util.Iterator;

/**
 * The {@code @link StaticFlowPath} class represents the static flow on one
 * {@link StaticPath} in a network. A total amount of {@code amount} is send
 * over the path. {@code StaticPathFlows} are needed to represent static flows
 * path-based.
 */
public class StaticFlowPath implements Iterable<Edge> {

  /**
   * The underlying path.
   */
  protected StaticPath path;

  /**
   * The amount of flow flowing over the path.
   */
  protected int amount;

  /**
   * Creates a {@code StaticPathFlow} with amount zero and an empty path.
   */
  public StaticFlowPath() {
    amount = 0;
    path = new StaticPath();
  }

  /**
   * Creates a {@code StaticPathFlow} with amount {@code amount} and underlying
   * path {@code path}.
   *
   * @param path the path along which flow is to be sent.
   * @param amount the total number of flow units to be sent.
   * @exception NullPointerException if {@code path} is null.
   */
  public StaticFlowPath( StaticPath path, int amount ) {
    if( path == null ) {
      throw new NullPointerException( GraphLocalization.loc.getString( "ds.graph.PathIsNullException" ) );
    }
    setPath( path );
    setAmount( amount );
  }

  /**
   * Returns the edges of the path belonging to this {@code StaticFlowPath}. If
   * no path is set, {@code null} is returned.
   *
   * @return the edges of the path belonging to this {@code StaticFlowPath}. or
   * {@code null} if no path is set.
   */
  public IdentifiableCollection<Edge> edges() {
    if( path == null ) {
      return null;
    } else {
      return path.getEdges();
    }
  }

  /**
   * Returns the first edge of the path belonging to this
   * {@code StaticFlowPath}. If no path is set, {@code null} is returned.
   *
   * @return the first edge of the path belonging to this {@code StaticFlowPath}
   * or {@code null} if no path is set.
   */
  public Edge firstEdge() {
    if( path == null ) {
      return null;
    } else {
      return path.first();
    }
  }

  /**
   * Returns the last edge of the path belonging to this {@code StaticFlowPath}.
   * If no path is set, {@code null} is returned.
   *
   * @return the last edge of the path belonging to this {@code StaticFlowPath}
   * or {@code null} if no path is set.
   */
  public Edge lastEdge() {
    if( path == null ) {
      return null;
    } else {
      return path.last();
    }
  }

  /**
   * Returns an iterator for the edges of the path belonging to this
   * {@code StaticFlowPath}. With the iterator one can iterate comfortable
   * through all edges.
   *
   * @return an iterator for the edges of the path belonging to this
   * {@code StaticFlowPath}.
   */
  public Iterator<Edge> iterator() {
    return path.iterator();
  }

  /**
   * Returns the path belonging to this {@code StaticFlowPath}.
   *
   * @return the path belonging to this {@code StaticFlowPath}.
   */
  public StaticPath getPath() {
    return path;
  }

  /**
   * Sets the path belonging to this {@code StaticFlowPath}.
   *
   * @param path the path to be set.
   * @exception NullPointerException if {@code path} is null.
   */
  public void setPath( StaticPath path ) {
    if( path == null ) {
      throw new NullPointerException( GraphLocalization.loc.getString( "ds.graph.PathIsNullException" ) );
    }
    this.path = path;
  }

  /**
   * Returns the amount of flow flowing over this {@code StaticFlowPath}.
   *
   * @return the amount of flow flowing over this {@code StaticFlowPath}.
   */
  public int getAmount() {
    return amount;
  }

  /**
   * Sets the amount of flow flowing over this {@code StaticFlowPath}.
   *
   * @param amount the amount of flow flowing over this {@code StaticFlowPath}.
   */
  public void setAmount( int amount ) {
    this.amount = amount;
  }

  /**
   * Returns a String consisting of the amount and a description of the path
   * that is described in {@link StaticPath}.
   *
   * @return a String consisting of the amount and a description of the path
   * that is described in {@link StaticPath}.
   */
  @Override
  public String toString() {
    return String.format( "{%1$s, %2$s}", amount, path );
  }

  /**
   * Returns whether an object is equal to this {@code StaticFlowPath}. The
   * result is {@code true} if and only if the argument is not null and is a
   * {@code StaticFlowPath} object having the same amount and containing a path
   * equal to the path in this {@code StaticFlowPath}.
   *
   * @param o object to compare.
   * @return {@code true} if the given object represents a
   * {@code StaticFlowPath} equivalent to this object, {@code false} otherwise.
   */
  @Override
  public boolean equals( Object o ) {
    if( o == null || !(o instanceof StaticFlowPath) ) {
      return false;
    } else {
      StaticFlowPath staticPathFlow = (StaticFlowPath) o;
      return (staticPathFlow.getAmount() == this.amount
        && staticPathFlow.getPath().equals( this.path ));
    }
  }

  /**
   * Returns the hash code of this {@code StaticFlowPath}. The hash code is
   * calculated by computing the arithmetic mean of the amount and the hash code
   * of the underlying {@link StaticPath} of this {@code StaticFlowPath}.
   * Therefore the hash code is equal for static path flows that are equal
   * according to the {@code equals}-method, but not necessarily different for
   * static path flows different to the {@code equals}-method. If hashing of
   * path flows is heavily used, the implementation of this method should be
   * reconsidered.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return (path.hashCode() / 2 + amount / 2);
  }

  /**
   * Clones this {@code StaticFlowPath} by cloning the underlying
   * {@link StaticPath} and creating a new {@code StaticFlowPath} with the clone
   * and the same {@code amount} as this {@code StaticFlowPath}.
   *
   * @return a {@code StaticFlowPath} object with a clone of the path of this
   * static path flow and the same amount as this static path flow.
   */
  @Override
  public StaticFlowPath clone() {
    return new StaticFlowPath( (StaticPath) this.path.clone(), this.amount );
  }
}
