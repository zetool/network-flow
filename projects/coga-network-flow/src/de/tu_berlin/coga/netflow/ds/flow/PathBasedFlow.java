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
 * PathBasedFlow.java
 * 
 */

package de.tu_berlin.coga.netflow.ds.flow;

import de.tu_berlin.coga.netflow.ds.structure.StaticFlowPath;
import java.util.Iterator;
import java.util.Vector;

/**
 * The {@code PathBasedFlow} class represents a static flow in a path based representation.
 * The static flow is stored as a {@code Vector} of {@link StaticFlowPath} objects.
 */
public class PathBasedFlow implements Iterable<StaticFlowPath>{
    
	/**
	 * The static path flows belonging to this {@code PathBasedFlow}.
	 */
    Vector<StaticFlowPath> staticPathFlows;
    
    /**
     * Creates a new {@code DynamicFlow} object without any path flows.
     */
    public PathBasedFlow(){
    	staticPathFlows = new Vector<StaticFlowPath>();
    }
    
    /**
     * Adds a path flow to this dynamic flow.
     * @param staticPathFlow the path flow to be add.
     */
    public void addPathFlow(StaticFlowPath staticPathFlow){
        if (staticPathFlow != null)
            staticPathFlows.add(staticPathFlow);
    }
    
    /**
     * Returns an iterator to iterate over the {@code StaticPathFlows} 
     * contained in this {@code PathBasedFlow}.
     * @return an iterator to iterate over the {@code StaticPathFlows} 
     * contained in this {@code PathBasedFlow}.

     */
    @Override
    public Iterator<StaticFlowPath> iterator(){
    	return staticPathFlows.iterator();
    }
    
    /**
     * Returns a String containing a description of all 
     * contained {@code StaticPathFlows}.
     * @return a String containing a description of all 
     * contained {@code StaticPathFlows}.
     */
    @Override
    public String toString(){
    	String result = "[\n";
    	for (StaticFlowPath pathFlow : staticPathFlows){
    		result += " " + pathFlow.toString() + "\n";
    	}
    	result += "]";
    	return result;
    }
    
}