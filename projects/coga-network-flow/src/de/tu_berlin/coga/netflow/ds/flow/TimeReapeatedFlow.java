/**
 * TimeReapeatedFlow.java
 * Created: 12.12.2011, 14:03:07
 */
package de.tu_berlin.coga.netflow.ds.flow;

import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimeEdge;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimePath;

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
