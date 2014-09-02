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
 */
package de.tu_berlin.coga.netflow.ds.structure;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.graph.structure.Path;
import de.tu_berlin.coga.graph.structure.StaticPath;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import java.util.LinkedList;

/**
 * <p>The {@code @link FlowOverTimeEdgeSequence} class represents the flow on
 * one  {@link Path} in a network. The delay time in the first node of the path
 * implies the time when the represented flow would start to leave the first
 * node. The flow is send with a constant {@code rate}. It sends a total
 * quantity of {@code amount} units of flow. Together with the rate and the
 * starting time this implies the point in time where the flow will stop
 * sending.</p>
 */
@SuppressWarnings("serial")
public class FlowOverTimeEdgeSequence extends LinkedList<FlowOverTimeEdge> {

    /** The amount of flow flowing through this edge sequence. */
    private int amount;
    /** The rate with which the flow enters the edge sequence. */
    private int rate;

    /**
     * Creates an empty flow sequence for an {@code amount} of one unit of flow
     * entering with {@code rate} 1.
     */
    public FlowOverTimeEdgeSequence() {
        super();
        amount = 1;
        rate = 1;
    }

    /**
     * Creates a new {@code FlowOverTimeEdgeSequence} that contains the same
     * edges. Note, that <b>the edges are not copied</b>, thus the instances of
     * the objects remain the same.
     * @param edgeSequence the edge sequence that is copied.
     */
    public FlowOverTimeEdgeSequence(FlowOverTimeEdgeSequence edgeSequence) {
        rate = edgeSequence.getRate();
        amount = edgeSequence.getAmount();
        for (FlowOverTimeEdge edge : edgeSequence) {
            add(edge);
        }
    }

    /**
     * Appends a sequence of edges to this edge sequence.
     * @param sequence the added sequence
     */
    public void append(FlowOverTimeEdgeSequence sequence) {
        addAll(sequence);
    }

    /**
     * Appends a sequence to this edge sequence.
     * @param sequence the sequence
     * @param time ignored.
     */
    public void append(FlowOverTimeEdgeSequence sequence, int time) {
        append(sequence);
        // TODO: time is ignored
    }

    /**
     * Returns the first edge of the sequence.
     * @return the first edge of the sequence
     */
    public FlowOverTimeEdge getFirstEdge() {
        return getFirst();
    }

    /**
     * Returns the last edge of the sequence.
     * @return the last edge of the sequence
     */
    public FlowOverTimeEdge getLastEdge() {
        return getLast();
    }

    /**
     * Returns the copy of the edge that is contained in this edge sequence, if
     * it is contained. {@code null} otherwise.
     * @param edge the edge sat is searched in this edge sequence
     * @return the (maybe copy) of the edge or {@code null}
     */
    public FlowOverTimeEdge get(Edge edge) {
        for (FlowOverTimeEdge e : this) {
            if (e.getEdge().equals(edge)) {
                return e;
            }
        }
        return null;
    }

    public FlowOverTimeEdge get(Edge edge, int time) {
        for (FlowOverTimeEdge e : this) {
            if (e.getEdge().equals(edge) && e.getTime() == time) {
                return e;
            }
        }
        return null;
    }

    public FlowOverTimeEdge get(IdentifiableIntegerMapping<Edge> transitTimes, Node node, int time) {
        int lastArrival = 0;
        for (FlowOverTimeEdge e : this) {
            if (e.getEdge().start().equals(node) && lastArrival <= time && time <= e.getTime()) {
                return e;
            }
            lastArrival = e.getTime() + transitTimes.get(e.getEdge());
        }
        return null;
    }

    public int length(IdentifiableIntegerMapping<Edge> transitTimes) {
        return getLast().getTime() + transitTimes.get(getLast().getEdge());
    }

    public int lengthUntil(IdentifiableIntegerMapping<Edge> transitTimes, FlowOverTimeEdge edge) {
        return edge.getTime();
    }

    public int lengthUpTo(IdentifiableIntegerMapping<Edge> transitTimes, FlowOverTimeEdge edge) {
        return edge.getTime() + transitTimes.get(edge.getEdge());
    }

    public FlowOverTimeEdgeSequence subsequence(FlowOverTimeEdge from, FlowOverTimeEdge to) {
        return subsequence(from, to, false, false);
    }

    public FlowOverTimeEdgeSequence subsequence(FlowOverTimeEdge from, FlowOverTimeEdge to, boolean fromInclusive, boolean toInclusive) {
        FlowOverTimeEdgeSequence result = new FlowOverTimeEdgeSequence();
        result.setAmount(amount);
        result.setRate(rate);
        boolean copying = (from == null);
        for (FlowOverTimeEdge edge : this) {
            if (copying && edge != to) {
                result.add(edge);
            } else if (copying && edge == to) {
                break;
            } else if (!copying && edge == from) {
                copying = true;
            }
        }
        if (fromInclusive && from != null) {
            result.addFirst(from);
        }
        if (toInclusive && to != null) {
            result.addLast(to);
        }
        return result;
    }

    /**
     * Returns the amount of flow that is sent through this edge sequence in
		 * total. The amount should be a strictly positive integral multiple of the
		 * rate.
     * @return the amount of flow that is sent through this edge sequence
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the total amount of flow that should be sent through the edge
		 * sequence. If the amount is larger than the rate, the path must be used
		 * for several time steps. The time can be computed as amount/rate. The
		 * {@code amount} shoud be a strictly positive integral multiple of the
		 * {@code rate}.
     * @param amount the amount of flow
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Returns the rate with which the flow enters the edge sequence.
     * @return the rate with which the flow enters the edge sequence
     */
    public int getRate() {
        return rate;
    }

    /**
     * Sets the rate of flow with which the flow can enter the edge sequence.
     * @param rate the rate with which the flow enters the sequence
     */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * Returns a String consisting of the rate, the amount and a description
     * of the path that is described in {@link DynamicPath}.
     * @return a String consisting of the rate, the amount and a description
     * of the path that is described in {@link DynamicPath}.
     */
    @Override
    public String toString() {
        return String.format("{%1$s, %2$s}", rate, super.toString());
    }

		public String toString( IdentifiableIntegerMapping<Edge> transitTimes ) {
			return toString() + " arrival: " + getArrival( transitTimes );
		}

    /**
     * Returns a detailed text representation of the flow over time. The output
     * can be generated using all transit time.
     * @param transitTimes the transit times that are used
     * @return a detailed multi line text representation
     */
    public String toText(IdentifiableIntegerMapping<Edge> transitTimes) {
        StringBuilder result = new StringBuilder(size() * 40);
        result.append(toString()).append("\n");
        int lastArrival = 0;
        for (FlowOverTimeEdge edge : this) {
            result.append(" Reaching node ").append(edge.getEdge().start()).append(" at time ").append(lastArrival).append(".\n");
            if (edge.getTime() - lastArrival != 0) {
                result.append(" Waiting for ").append(edge.getTime() - lastArrival).append(".\n");
            }
            result.append(" Entering edge ").append(edge.getEdge().id()).append(" at ").append(edge.getTime()).append(".\n");
            lastArrival = edge.getTime() + transitTimes.get(edge.getEdge());
        }
        return result.toString();
    }

	public int getArrival( IdentifiableIntegerMapping<Edge> transitTimes ) {
		return getLastEdge().getTime() + transitTimes.get( getLastEdge().getEdge() );
	}

	public StaticPath asStatic() {
		StaticPath s = new StaticPath();
		for( FlowOverTimeEdge fote : this ) {
			s.addLastEdge( fote.getEdge() );
		}
		return s;
	}
}
