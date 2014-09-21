
package de.tu_berlin.coga.netflow.dynamic.eatapprox;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimePath;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlowOverTime;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.container.mapping.TimeIntegerMapping;
import de.tu_berlin.coga.container.mapping.TimeIntegerPair;
import de.tu_berlin.coga.netflow.ds.flow.EdgeBasedFlowOverTime;
import java.util.ArrayList;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EarliestArrivalFlowPatternBuilder {
	ArrayList<Integer> flowPattern;

	public static EarliestArrivalFlowPattern fromPathBased( PathBasedFlowOverTime df, IdentifiableIntegerMapping<Edge> transitTimes, int neededTimeHorizon ) {
		EarliestArrivalFlowPatternBuilder builder = new EarliestArrivalFlowPatternBuilder( neededTimeHorizon + 1 );

		for( FlowOverTimePath p : df ) {
			//System.out.println( "Arriving at " + p.getArrival( transitTimes ) + ": " + p.getAmount() );
      int arrivalTime = getArrival( p, transitTimes );
			builder.addFlowValue( p.getArrival( transitTimes ), p.getAmount() );
			//builder.addFlowValue( builder.getArrival( p, transitTimes ), p.getAmount() );
		}

		return builder.build();
	}
  
  public static EarliestArrivalFlowPattern fromEdgeBased( EdgeBasedFlowOverTime ef, Iterable<Edge> edges, int neededTimeHorizon ) {
    EarliestArrivalFlowPatternBuilder builder = new EarliestArrivalFlowPatternBuilder( neededTimeHorizon + 1 );
    for( Edge e : edges ) {
      TimeIntegerMapping arrivals = ef.get( e );
      int lastTime = -1;
      int lastValue = 0;
      for( TimeIntegerPair tip : arrivals ) {
        System.out.println( tip );
        if( tip.time() < 0 || tip.time() == Integer.MAX_VALUE ) {
          continue;
        }
        builder.addFlowValue( tip.time(), tip.value() );
        if( lastTime != -1 && lastValue != 0 ) {
          for( int i = lastTime + 1; i < tip.time(); ++i ) {
            builder.addFlowValue( i, lastValue );
          }
        }
        lastTime = tip.time();
        lastValue = tip.value();
      }
    }
    
    return builder.build();
  }

	private static int getArrival( FlowOverTimePath p, IdentifiableIntegerMapping<Edge> transitTimes ) {
		int arrival = 0;
		for( Edge e : p.edges() ) {
			// TODO: this does not work! if waiting on a path the waiting time is not included
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
