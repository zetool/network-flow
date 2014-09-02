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
/*
 * PathBasedFlowOverTime.java
 *
 */
package de.tu_berlin.coga.netflow.ds.flow;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimePath;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The {@code PathBasedFlowOverTime} class represents a dynamic flow in a path based representation.
 * The dynamic flow is stored as a {@code Vector} of {@link FlowOverTimePath} objects.
 */
public class PathBasedFlowOverTime implements Iterable<FlowOverTimePath> {
	/** The path flows belonging to this {@code PathBasedFlowOverTime}. */
	ArrayList<FlowOverTimePath> pathFlows;

	/** Creates a new {@code PathBasedFlowOverTime} object without any path flows. */
	public PathBasedFlowOverTime() {
		pathFlows = new ArrayList<>();
	}

	/**
	 * Adds a path flow to this dynamic flow.
	 * @param pathFlow the path flow to be add.
	 */
	public void addPathFlow( FlowOverTimePath pathFlow ) {
		if( pathFlow != null )
			pathFlows.add( pathFlow );
	}

	public boolean remove( FlowOverTimePath pathFlow ) {
		return pathFlows.remove( pathFlow );
	}

	/**
	 * Returns an iterator to iterate over the {@code DynamicPathFlows}
	 * contained in this {@code PathBasedFlowOverTime}.
	 * @return an iterator to iterate over the {@code DynamicPathFlows}
	 * contained in this {@code PathBasedFlowOverTime}.
	 */
	@Override
	public Iterator<FlowOverTimePath> iterator() {
		return pathFlows.iterator();
	}

	/**
	 * Calculates the total value (the number of sent flow units) of this flow
	 * over time. The running time is in O(#paths)
	 * @return the total value of this flow
	 */
	public long getValue() {
		int val = 0;
		for( FlowOverTimePath p : this ) {
			val += p.getAmount();
		}
		return val;
	}

	/**
	 * Returns a String containing a description of all contained {@code DynamicPathFlows}.
	 * @return a String containing a description of all  contained {@code DynamicPathFlows}.
	 */
	@Override
	public String toString() {
		String result = "[\n";
		for( FlowOverTimePath pathFlow : pathFlows )
			result += " " + pathFlow.toString() + "\n";
		result += "]";
		return result;
	}

	public String toString( IdentifiableIntegerMapping<Edge> transitTimes ) {
		String result = "[\n";
		for( FlowOverTimePath pathFlow : pathFlows )
			result += " " + pathFlow.toString( transitTimes ) + "\n";
		result += "]";
		return result;
	}
}
