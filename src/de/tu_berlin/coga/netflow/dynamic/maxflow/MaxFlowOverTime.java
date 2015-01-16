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

package de.tu_berlin.coga.netflow.dynamic.maxflow;

import de.tu_berlin.coga.netflow.dynamic.problems.MaximumFlowOverTimeProblem;
import de.tu_berlin.coga.netflow.classic.mincost.MinimumMeanCycleCancelling;
import de.tu_berlin.coga.netflow.classic.maxflow.PathDecomposition;
import org.zetool.common.algorithm.Algorithm;
import de.tu_berlin.coga.netflow.ds.structure.DynamicPath;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimePath;
import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlow;
import de.tu_berlin.coga.graph.structure.StaticPath;
import de.tu_berlin.coga.netflow.ds.structure.StaticFlowPath;
import de.tu_berlin.coga.netflow.ds.flow.TimeReapeatedFlow;
import de.tu_berlin.coga.netflow.ds.network.ExtendedGraph;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.graph.localization.GraphLocalization;
import de.tu_berlin.coga.netflow.classic.problems.MinimumCostFlowProblem;
import java.util.List;

/**
 * The class {@code MaxFlowOverTime} solves the max flow over time 
 * problem. The flow is computed by a reduction to a minimum cost flow over
 * time computation. The reduction increases the size of the problem input
 * and thus needs the double amount of free space in memory.
 * @author Gordon Schlechter, Jan-Philipp Kappmeier
 */
public class MaxFlowOverTime extends Algorithm<MaximumFlowOverTimeProblem, TimeReapeatedFlow> {
	private DefaultDirectedGraph network;
	private IdentifiableIntegerMapping<Edge> edgeCapacities;
	private List<Node> sinks;
	private List<Node> sources;
	private Node superNode;
	private int timeHorizon;
	private IdentifiableIntegerMapping<Edge> transitTimes;
	private ExtendedGraph ex;

	/** Creates a new instance of MaxFlowOverTime */
	public MaxFlowOverTime() {}

	/**
	 * Reduction for the computation of an maximum flow over time using a
	 * minimum cost flow computation.
	 */
	private void reduction() {
		ex = new ExtendedGraph( network, 1, sources.size() + sinks.size() );
		superNode = ex.getFirstNewNode();

		edgeCapacities.setDomainSize( ex.getEdgeCapacity() ); // reserve space
		transitTimes.setDomainSize( ex.getEdgeCapacity() ); // reserve space

		for( Node source : sources ) {
			Edge newEdge = ex.createAndSetEdge( superNode, source );
			edgeCapacities.set( newEdge, Integer.MAX_VALUE );
			transitTimes.set( newEdge, -(timeHorizon + 1) );
		}

		for( Node sink : sinks ) {
			Edge newEdge = ex.createAndSetEdge( sink, superNode );
			edgeCapacities.set( newEdge, Integer.MAX_VALUE );
			transitTimes.set( newEdge, 0 );
		}
	}

	/**
	 * Hides the added super node and edges in the network. After this you got 
	 * back the original network. The added Edges in the flow are also removed.
	 */
	private void reconstruction( IdentifiableIntegerMapping<Edge> flow ) {
		//ex.undo();
		// the additional edges have the highest numbers, so we can just get rid of them
		flow.setDomainSize( flow.getDomainSize() - (sources.size() + sinks.size()) );
		transitTimes.setDomainSize( flow.getDomainSize() );
		edgeCapacities.setDomainSize( flow.getDomainSize() );
	}

	/**
	 * Creates dynamic flow out of the given static flow. At first static 
	 * flow is divided in the different paths and then it is added to the 
	 * dynamic flow, if the conditions are met. */
	private TimeReapeatedFlow translateIntoMaxFlow( PathBasedFlow minCostFlow ) {
		TimeReapeatedFlow mFlow = new TimeReapeatedFlow( timeHorizon );

		for( StaticFlowPath staticPathFlow : minCostFlow ) {
			if( staticPathFlow.getAmount() == 0 )
				continue;
			StaticPath staticPath = staticPathFlow.getPath();
			int path_transit_time = 0;
			for( Edge e : staticPath )
				path_transit_time += transitTimes.get( e );

			// Add this path only in case that our given time is long 
			// enough to send anything at all over this path
			if( timeHorizon > path_transit_time ) {
				DynamicPath dynamicPath = new DynamicPath( staticPath );
				FlowOverTimePath dynamicPathFlow = new FlowOverTimePath( dynamicPath, staticPathFlow.getAmount(), (timeHorizon - path_transit_time) * staticPathFlow.getAmount() );
				mFlow.addPathFlow( dynamicPathFlow );
			}
		}

		return mFlow;
	}

	@Override
	protected TimeReapeatedFlow runAlgorithm( MaximumFlowOverTimeProblem problem ) {
		sinks = problem.getSinks();
		sources = problem.getSources();
		network = (DefaultDirectedGraph)problem.getNetwork(); // todo avoid cast here?
		edgeCapacities = problem.getCapacities();
		transitTimes = problem.getTransitTimes();
		timeHorizon = problem.getTimeHorizon();

		if( (sources == null) || (sinks == null) )
			throw new IllegalArgumentException( GraphLocalization.LOC.getString( "algo.graph.MaxFlowOverTime.SpecifySourceSinkFirst" ) );

		if( (sources.isEmpty()) || (sinks.isEmpty()) )
			return new TimeReapeatedFlow( timeHorizon );

		reduction();

		MinimumCostFlowProblem p = new MinimumCostFlowProblem( ex, edgeCapacities, transitTimes, new IdentifiableIntegerMapping<Node>( network.nodes().size() ) );
		Algorithm<MinimumCostFlowProblem, IdentifiableIntegerMapping<Edge>> algorithm = new MinimumMeanCycleCancelling();
		algorithm.setProblem( p );
		algorithm.run();
		IdentifiableIntegerMapping<Edge> flow = algorithm.getSolution();

		//SuccessiveShortestPath algo = new SuccessiveShortestPath(network, zeroSupplies, edgeCapacities, transitTimes);
		//algo.run();
		//flow = algo.getFlow();

		reconstruction( flow );

		PathBasedFlow minCostFlow = PathDecomposition.calculatePathDecomposition( null, sources, sinks, flow );
		//PathBasedFlow minCostFlow = PathDecomposition.calculatePathDecomposition( (DefaultDirectedGraph)ex, sources, sinks, flow );

		return translateIntoMaxFlow( minCostFlow );
	}
}
