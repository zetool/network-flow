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
/*
 * FlowOverTimeImplicit.java
 *
 */
package de.tu_berlin.coga.netflow.ds.flow;

import de.tu_berlin.coga.netflow.ds.flow.EdgeBasedFlowOverTime;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlowOverTime;
import de.tu_berlin.coga.netflow.ds.flow.FlowOverTimeInterface;
import de.tu_berlin.coga.netflow.dynamic.FlowOverTimePathDecomposition;
import de.tu_berlin.coga.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import de.tu_berlin.coga.netflow.dynamic.earliestarrival.EarliestArrivalAugmentingPath;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimePath;
import java.util.Queue;
import java.util.logging.Logger;

/**
 *
 * @author Martin Groß
 */
public class FlowOverTimeImplicit implements FlowOverTimeInterface {
	/** The logger of the main class. */
	private static final Logger log = Logger.getGlobal();
	private EdgeBasedFlowOverTime edgeBased;
	private PathBasedFlowOverTime pathBased;
	private int flowAmount;
	private int timeHorizon;
	private int totalCost;

	public FlowOverTimeImplicit( ImplicitTimeExpandedResidualNetwork network, Queue<EarliestArrivalAugmentingPath> eaaPaths ) {
		edgeBased = new EdgeBasedFlowOverTime( network.flow() );
		FlowOverTimePathDecomposition decomposition = new FlowOverTimePathDecomposition();
		decomposition.setProblem( network );
		decomposition.run();
		pathBased = decomposition.getSolution();

		log.fine( "Value of the flow:" );
		int sum = 0;
		for( FlowOverTimePath p : pathBased ) {
                        System.out.println(p.getRate() + ": " + p);
			sum += p.getRate();
		}
		log.fine( Integer.toString( sum ) );

		//pathBased = new PathBasedFlowOverTime();
		//LinkedList<FlowOverTimeEdgeSequence> paths = new LinkedList<FlowOverTimeEdgeSequence>();
		//int index = 0;
		totalCost = 0;
		for( EarliestArrivalAugmentingPath eaaPath : eaaPaths ) {
			//paths.add(eaaPath.getFlowOverTimeEdgeSequence(network));
			flowAmount += eaaPath.getCapacity();
			timeHorizon = Math.max( timeHorizon, eaaPath.getArrivalTime());
			totalCost += eaaPath.getCapacity() * eaaPath.getArrivalTime();
		}

		if( flowAmount != sum )
			throw new IllegalStateException( "Flow value in Edge based and Path based differ!\nEdge-Based: " + flowAmount + "\nPath-Based: " + sum );
	}

	@Override
	public EdgeBasedFlowOverTime getEdgeBased() {
		return edgeBased;
	}

	public int getFlowAmount() {
		return flowAmount;
	}

	@Override
	public PathBasedFlowOverTime getPathBased() {
		return pathBased;
	}

	public int getTimeHorizon() {
		return timeHorizon;
	}

	/**
	 * Returns the total costs for the flow.
	 * @return
	 */
	public int getTotalCost() {
		return totalCost;
	}
}
