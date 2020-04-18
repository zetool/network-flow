/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zetool.netflow.ds.flow;

import org.zetool.netflow.ds.structure.FlowOverTimeEdge;
import org.zetool.netflow.ds.structure.FlowOverTimePath;

/**
 * A time repeated flow is a special {@link PathBasedFlowOverTime} without
 * delay in nodes.
 * @author Jan-Philipp Kappmeier
 */
public class TimeReapeatedFlow extends PathBasedFlowOverTime {
	private int timeHorizon;
	private long value;

	public TimeReapeatedFlow( int timeHorizon ) {
		this.timeHorizon = timeHorizon;
	}

	/**
	 * Adds the {@link FlowOverTimePath} to the path flows. If the path contains
	 * a delay (or waiting time) different from 0, an exception is throwsn.
	 * @param pathFlow a path that is added
	 * @throws IllegalArgumentException if the delays are not 0
	 */
	@Override
	public void addPathFlow( FlowOverTimePath pathFlow ) throws IllegalArgumentException {
		if( pathFlow.getFirst().getDelay() != 0 )
			throw new IllegalArgumentException( "Time Repeated Flow starts at time 0.");
		for( FlowOverTimeEdge e : pathFlow )
			if( e.getDelay() != 0 )
				throw new IllegalArgumentException( "No waiting allowed in Time Repeated Flow" );
		super.addPathFlow( pathFlow );
		value += pathFlow.getAmount();
	}

	@Override
	public boolean remove( FlowOverTimePath pathFlow ) {
		if( super.remove( pathFlow ) ) {
			value -= pathFlow.getAmount();
			return true;
		}
		return false;
	}

	/**
	 * Returns the value of the time repeated flow over time.
	 * @return the value of the time repeated flow over time
	 */
	public long getValue() {
		return value;
	}
}
