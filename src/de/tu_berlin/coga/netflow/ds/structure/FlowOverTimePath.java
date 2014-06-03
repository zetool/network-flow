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

/*
 * FlowOverTimePath.java
 */
package de.tu_berlin.coga.netflow.ds.structure;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.structure.Path;
import java.util.Iterator;

/**
 * The {@code @link FlowOverTimePath} class represents the flow on one
 * {@link Path} in a network. The delay time in the first node of the path
 * implies the time when the represented flow would start to leave the first node.
 * The flow is send with a constant rate of {@code rate}. It sends a
 * total quantity of {@code amount} units of flow. Together with the rate
 * and the starting time this implies the point in time where the flow
 * will stop sending.
 * {@code DynamicPathFlows} are needed to represent dynamic flows path based.
 */
@SuppressWarnings( "serial" )
public class FlowOverTimePath extends FlowOverTimeEdgeSequence {
	/**
	 * Creates a {@code FlowOverTimePath} with unit {@code rate}, {@code amount}
	 * zero and an empty path.
	 */
	public FlowOverTimePath() {
		super();
	}

	/**
	 * Creates a new {@code FlowOverTimePath} that contains the same
	 * edges. Note, that <b>the edges are not copied</b>, thus the instances of
	 * the objects remain the same.
	 * @param path the path that is copied.
	 */
	public FlowOverTimePath( FlowOverTimePath path ) {
		super( path );
	}

	/**
	 * Creates a new {@code FlowOverTimePath} that contains the same
	 * edges. Note, that <b>the edges are not copied</b>, thus the instances of
	 * the objects remain the same.
	 * @param edgeSequence the edge sequence that is copied.
	 */
	public FlowOverTimePath( FlowOverTimeEdgeSequence edgeSequence ) {
		super( edgeSequence );
	}

	@Deprecated
	public FlowOverTimePath( DynamicPath path, int rate, int amount ) {
		for( Edge edge : path )
			addLast( new FlowOverTimeEdge( edge, path.getDelay( edge ) ) );
		setRate( rate );
		setAmount( amount );
	}

	public Iterable<Edge> edges() {
		return new Iterable<Edge>() {
			@Override
			public Iterator<Edge> iterator() {
				return new Iterator<Edge>() {
					private Iterator<FlowOverTimeEdge> internal = FlowOverTimePath.this.iterator();

					@Override
					public boolean hasNext() {
						return internal.hasNext();
					}

					@Override
					public Edge next() {
						return internal.next().getEdge();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException( "Not supported yet." );
					}
				};
			}
		};
	}

	public Edge firstEdge() {
		return getFirstEdge().getEdge();
	}

	public Edge lastEdge() {
		return getLastEdge().getEdge();
	}
}
