/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.zetool.netflow.dynamic.eatapprox;

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

  public int getTimeHorizon() {
    return timeHorizon;
  }



}
