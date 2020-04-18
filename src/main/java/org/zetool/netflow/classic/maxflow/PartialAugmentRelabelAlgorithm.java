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
package org.zetool.netflow.classic.maxflow;

import org.zetool.netflow.classic.problems.MaximumFlowProblem;
import org.zetool.graph.Edge;
import org.zetool.graph.Node;
import org.zetool.netflow.ds.flow.MaximumFlow;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class PartialAugmentRelabelAlgorithm extends PushRelabel {

	@Override
	protected MaximumFlow runAlgorithm( MaximumFlowProblem problem ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected int push( Edge e ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected int relabel( Node v ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected void computeMaxFlow() {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected void makeFeasible() {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

}
