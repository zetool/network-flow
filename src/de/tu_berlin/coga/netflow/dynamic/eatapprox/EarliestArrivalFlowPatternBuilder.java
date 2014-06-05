/**
 * EarliestArrivalFlowPatternBuilder.java
 * Created: 27.01.2014, 11:58:17
 */
package de.tu_berlin.coga.netflow.dynamic.eatapprox;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimePath;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlowOverTime;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import java.util.ArrayList;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EarliestArrivalFlowPatternBuilder {
	ArrayList<Integer> flowPattern;

	public static EarliestArrivalFlowPattern fromPathBased( PathBasedFlowOverTime df, IdentifiableIntegerMapping<Edge> transitTimes, int neededTimeHorizon ) {
		EarliestArrivalFlowPatternBuilder builder = new EarliestArrivalFlowPatternBuilder( neededTimeHorizon + 2 );

		for( FlowOverTimePath p : df ) {
			//System.out.println( "Arriving at " + p.getArrival( transitTimes ) + ": " + p.getAmount() );
			builder.addFlowValue( p.getArrival( transitTimes ), p.getAmount() );
			//builder.addFlowValue( builder.getArrival( p, transitTimes ), p.getAmount() );
		}

		return builder.build();
	}

	private int getArrival( FlowOverTimePath p, IdentifiableIntegerMapping<Edge> transitTimes ) {
		int arrival = 0;
		for( Edge e : p.edges() ) {
			// this does not work!
			arrival += transitTimes.get( e );
		}
		return arrival;
	}

	public EarliestArrivalFlowPatternBuilder() {
		flowPattern = new ArrayList<>();
	}

	public EarliestArrivalFlowPatternBuilder( int timeHorizon) {
		flowPattern = new ArrayList<>( timeHorizon );
	}

	public void addFlowValue( int value ) {
		flowPattern.add( value );
	}

	public EarliestArrivalFlowPattern build() {
		return new EarliestArrivalFlowPattern( flowPattern );
	}

	public void addFlowValue( int arrival, int amount ) {
		if( arrival >= flowPattern.size() )
			for( int i = flowPattern.size(); i <= arrival; ++i )
				flowPattern.add( flowPattern.isEmpty() ? 0 : flowPattern.get( i - 1 ) );

		for( int i = arrival; i < flowPattern.size(); ++i )
			flowPattern.set( i, flowPattern.get( i ) + amount );
	}
}
