/**
 * EarliestArrivalFlowPattern.java
 * Created: 27.01.2014, 11:54:11
 */
package de.tu_berlin.coga.netflow.dynamic.eatapprox;

import java.util.ArrayList;
import java.util.Arrays;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EarliestArrivalFlowPattern {
	ArrayList<Integer> flowValues;
	int timeHorizon;

	EarliestArrivalFlowPattern( ArrayList<Integer> flowValues ) {
		this.timeHorizon = flowValues.size();
		this.flowValues = flowValues;
	}

	public int getValue( int i ) {
		return flowValues.get( i );
	}

	@Override
	public int hashCode() {
		int hash = 3;
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		final EarliestArrivalFlowPattern other = (EarliestArrivalFlowPattern)obj;
		if( this.timeHorizon != other.timeHorizon )
			return false;
		return( other.flowValues.equals( this.flowValues ) );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "TimeHorizon: " ).append( timeHorizon ).append( "\n" );
		for( int i = 0; i < timeHorizon; ++i ) {
			sb.append( i ).append( ": ").append( flowValues.get( i ) ).append( "\n" );
		}
		return sb.toString();
	}



}
