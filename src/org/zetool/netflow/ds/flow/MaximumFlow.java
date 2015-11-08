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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.zetool.netflow.ds.flow;

import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Node;
import org.zetool.graph.util.GraphUtil;

/**
 *
 * @author Martin Groß
 */
public class MaximumFlow extends Flow {
	private final MaximumFlowProblem problem;

	public MaximumFlow( MaximumFlowProblem problem, IdentifiableIntegerMapping<Edge> flow ) {
		super( flow );
		this.problem = problem;
	}

	public MaximumFlowProblem getProblem() {
		return problem;
	}

  public long getFlowValue() {
    long result = 0;
    for( Node source : problem.getSources() ) {
      for( Edge edge : GraphUtil.outgoingIterator( problem.getNetwork(), source ) ) {
        result += get( edge );
      }
      for( Edge edge : GraphUtil.incomingIterator( problem.getNetwork(), source ) ) {
        result -= get( edge );
      }
    }
    return result;
  }

  /**
   * The check methods checks flow conservation. It only works for directed
   * graphs.
   * @return {@literal true} if the flow satisfies flow conservation, {@literal false} otherwise
   */
  public boolean check() {
    boolean problems = false;
    for( Node v : problem.getNetwork() ) {
      if( problem.getSources().contains( v ) || problem.getSinks().contains( v ) ) {
        continue;
      }
      // check flow conservation
      int sum = 0;
      // sum incoming
      for( Edge e : GraphUtil.incomingIterator( problem.getNetwork(), v ) ) {
        sum += get( e );
      }
      // sum outcoming
      for( Edge e : GraphUtil.outgoingIterator( problem.getNetwork(), v ) ) {
        sum -= get( e );
      }

      if( sum != 0 ) {
        System.out.println( "Flow conservation at node " + v.toString() + " is violated. Value is " + sum );
      }
      problems = sum != 0;
    }

    for( Edge e : problem.getNetwork().edges() ) {
      if( get( e ) > problem.getCapacities().get( e ) ) {
        System.out.println( "Capacity on edge " + e.toString() + " is violated: " + get( e ) + " > "
                + problem.getCapacities().get( e ) );
        problems = false;
      }
      if( get( e ) < 0 ) {
        System.out.println( "Capacity on edge " + e.toString() + " is violated: " + get( e ) + " < 0" );
        problems = false;
      }
    }
    return !problems;
  }
}
