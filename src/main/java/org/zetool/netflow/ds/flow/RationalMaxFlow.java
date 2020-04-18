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
/*
 * RationalMaxFlow.java
 *
 */
package org.zetool.netflow.ds.flow;

import org.zetool.netflow.classic.problems.RationalMaxFlowProblem;
import org.zetool.graph.Edge;
import org.zetool.container.mapping.IdentifiableDoubleMapping;

/**
 *
 * @author Sebastian Schenker
 */
public class RationalMaxFlow extends RationalFlow {

	private RationalMaxFlowProblem problem;

	public RationalMaxFlow( RationalMaxFlowProblem problem, IdentifiableDoubleMapping<Edge> flow ) {
		super( flow );
		this.problem = problem;
	}

	public RationalMaxFlowProblem getProblem() {
		return problem;
	}

	public double getFlowValue() {
		double result = 0.0;
		for( Edge edge : problem.getNetwork().outgoingEdges( problem.getSource() ) ) {
			result += get( edge );
		}
		for( Edge edge : problem.getNetwork().incomingEdges( problem.getSource() ) ) {
			result -= get( edge );
		}
		return result;
	}
}
