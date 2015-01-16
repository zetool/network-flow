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
 * FlowOverTimePath.java
 *
 */
package de.tu_berlin.coga.netflow.ds.structure;

import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.structure.Path;

/**
 * The {@code @link FlowOverTimePath} class represents the flow on one 
 * {@link Path} in a network. The delay time in the first node of the path
 * implies the time when the represented flow would start to leave the first node.
 * The flow is send with a constant rate of {@code rate}. It sends a
 * total quantity of {@code amount{@code  units of flow. Together with the rate
 * and the starting time this implies the point in time where the flow
 * will stop sending.
 * {@code DynamicPathFlows} are needed to represent dynamic flows path based.
 */
public class FlowOverTimeCycle extends FlowOverTimeEdgeSequence {

    private int offset = 0;

    public FlowOverTimeCycle(FlowOverTimeEdgeSequence edgeSequence, int offset) {
        super(edgeSequence);
        //this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
/*
    @Override
    public int length(IdentifiableIntegerMapping<Edge> transitTimes) {
        int result = offset;
        for (FlowOverTimeEdge e : this) {
            result += e.getDelay();
            result += transitTimes.get(e.getEdge());
        }
        return result;
    }

    @Override
    public int lengthUntil(IdentifiableIntegerMapping<Edge> transitTimes, FlowOverTimeEdge edge) {
        int result = offset;
        for (FlowOverTimeEdge e : this) {
            result += e.getDelay();
            if (edge == e) {
                break;
            }
            result += transitTimes.get(e.getEdge());
        }
        return result;
    }

    @Override
    public int lengthUpTo(IdentifiableIntegerMapping<Edge> transitTimes, FlowOverTimeEdge edge) {
        int result = offset;
        for (FlowOverTimeEdge e : this) {
            result += e.getDelay();
            result += transitTimes.get(e.getEdge());
            if (edge == e) {
                break;
            }
        }
        return result;
    }
*/
    /**
     * Returns a String consisting of the rate, the amount and a description
     * of the path that is described in {@link DynamicPath}.
     * @return a String consisting of the rate, the amount and a description
     * of the path that is described in {@link DynamicPath}.
     */
    @Override
    public String toString() {
        return String.format("{%1$s, %2$s}", getOffset(), super.toString());
    }
}
