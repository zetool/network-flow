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

package de.tu_berlin.coga.netflow.ds.flow;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.container.mapping.IdentifiableObjectMapping;
import de.tu_berlin.coga.container.mapping.TimeIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;

/**
 * The {@code EdgeBasedFlowOverTime} class represents an edge based representation
 * of a dynamic flow. For each edge a {@code EdgeBasedFlowOverTime} stores an
 * {@link TimeIntegerMapping} representing the value of the flow depending
 * on the time. The mapping between edges and flow functions is internally
 * stored as an {@link IdentifiableObjectMapping} from {@link Edge} objects to
 * {@link TimeIntegerMapping} objects.
 */
public class EdgeBasedFlowOverTime {

    /**
     * The mapping from edges to flow functions depending on the time.
     */
    private IdentifiableObjectMapping<Edge,TimeIntegerMapping> map;

    /**
     * Creates a new {@code EdgeBasedFlowOverTime} for the network {@code network}
     * where the flow on all edges is zero all the time.
     * The flow functions can later be set by
     * {@code set(Edge edge, TimeIntegerMapping flowFunction)}.
     * @param graph the network for which the empty flow shall be created.
     */
    public EdgeBasedFlowOverTime(DirectedGraph graph) {
        map = new IdentifiableObjectMapping<>(graph.edgeCount());
        IdentifiableCollection<Edge> edges = graph.edges();
        for (Edge edge : edges){
            map.set(edge, new TimeIntegerMapping());
        }
    }

    /**
     * Creates a new {@code EdgeBasedFlowOverTime} object where the edges are mapped
     * to flow functions as given in {@code flowOnEdges}.
     * @param flowOnEdges mapping of edges to flow functions that shall be
     *        used in this {@code EdgeBasedFlowOverTime}
     */
    public EdgeBasedFlowOverTime(IdentifiableObjectMapping<Edge,TimeIntegerMapping> flowOnEdges){
        map = flowOnEdges;
    }

    /**
     * Gets the {@code TimeIntegerMapping} that represents the flow
     * function on the edge {@code edge}. If the flow function is not
     * stored for this edge, null is returned.
     * @param edge
     * @return the {@code TimeIntegerMapping} that represents the flow
     * function on the edge {@code edge}
     */
    public TimeIntegerMapping get(Edge edge) {
        if (map.isDefinedFor(edge))
            return map.get(edge);
        else
            return null;
    }

    /**
     * Sets the flow function of the edge {@code edge} to the
     * {@code TimeIntegerMapping} {@code flowFunction}.
     * @param edge the edge which flow function shall be set
     * @param flowFunction the flow function that shall be set for {@code edge}.
     */
    public void set(Edge edge, TimeIntegerMapping flowFunction){
        map.set(edge, flowFunction);
    }

    /**
     * Returns whether the object {@code o} is equal to this
     * {@code EdgeBasedFlowOverTime} object.
     * The result is true if and only if the argument is not null and is a
     * {@code EdgeBasedFlowOverTime} object having which flow function is equal
     * of the one of this {@code EdgeBasedFlowOverTime}.
     * @param o  o object to compare.
     * @return {@code true} if the given object represents a
     * {@code EdgeBasedFlowOverTime} equivalent to this node, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof EdgeBasedFlowOverTime) {
            EdgeBasedFlowOverTime f = (EdgeBasedFlowOverTime) o;
            return map.equals(f.map);
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code for this {@code EdgeBasedFlowOverTime} object.
     * The hash code is equal to the hashCode of the underlying
     * {@code IdentifiableObjectMapping}.
     * @return the hash code of this object
     */
    @Override
    public int hashCode(){
        return map.hashCode();
    }

    /**
     * Returns a String describing the flow.
     * For a description see the {@code toString()} method of
     * {@link IdentifiableObjectMapping}.
     * @return a String describing the flow, see the {@code toString()} method of
     * {@link IdentifiableObjectMapping}.
     */
    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Clones this {@code EdgeBasedFlowOverTime} by cloning the underlying
     * {@code IdentifiableObjectMapping}.
     * @return a clone of this {@code EdgeBasedFlowOverTime} object.
     */
    @Override
    public EdgeBasedFlowOverTime clone(){
        return new EdgeBasedFlowOverTime(map.clone());
    }

}
