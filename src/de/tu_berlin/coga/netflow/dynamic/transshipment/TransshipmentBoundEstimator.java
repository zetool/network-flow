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
package de.tu_berlin.coga.netflow.dynamic.transshipment;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import de.tu_berlin.coga.netflow.classic.transshipment.StaticTransshipment;
import de.tu_berlin.coga.netflow.classic.maxflow.PathDecomposition;
import de.tu_berlin.coga.graph.Edge;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.Node;
import de.tu_berlin.coga.netflow.ds.flow.PathBasedFlow;
import de.tu_berlin.coga.netflow.ds.structure.StaticFlowPath;
import de.tu_berlin.coga.graph.DefaultDirectedGraph;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.localization.GraphLocalization;

/** 
 * Calculates an upper bound for the time horizon needed
 * to fulfill all supplies and demands.
 */
public class TransshipmentBoundEstimator {
  private final static boolean ALGO_PROGRESS = false;
  private final static boolean BOUND_ESTIMATOR_STATIC_FLOW = false;
  private final static boolean BOUND_ESTIMATOR_LONG = false;
  private final static boolean BOUND_ESTIMATOR = false;
  
	public static int calculateBoundByLongestPath(DirectedGraph network,
			IdentifiableIntegerMapping<Edge> transitTimes,
			IdentifiableIntegerMapping<Edge> edgeCapacities,
			IdentifiableIntegerMapping<Node> supplies){
		// Calculate an upper Bound for the time horizon:
		// find the n longest edges (n = number of nodes)
		// the sum of the transit times of this edges
		// is an upper bound for the length of a path
		// from any source to any sink.
		// Find also the edge with smallest capacity
		// and the sum of all supplies in the network.
		// Upper bound is then
		// (upper bound for path length) + round up((sum of supplies) / minimal capacitiy)
		int upperBoundForLongestPath = 0;
		int neededFlow = 0;
		IdentifiableCollection<Edge> originalEdges = network.edges();
		LinkedList<Integer> edgesList = new LinkedList<Integer>();
		int minCap = Integer.MAX_VALUE;
		for (Edge edge: originalEdges){
			edgesList.add(transitTimes.get(edge));
			if (edgeCapacities.get(edge) < minCap)
				minCap = edgeCapacities.get(edge);
		}
		Collections.sort(edgesList, Collections.reverseOrder()); 	
		
		Iterator<Integer> it = edgesList.iterator();
		for (int i = 0; i < network.nodes().size(); i++){
			if (it.hasNext())
				upperBoundForLongestPath += it.next();
			int s = supplies.get(network.nodes().get(i));
			if (s > 0)
				neededFlow += s;
		}
		
		int timeNeededWhileFlowing = (int)Math.ceil(neededFlow / minCap);
		
		 // +1 because upper bound itself is never tested.
		return (upperBoundForLongestPath + timeNeededWhileFlowing + 1);

	}
	
	public static int calculateBoundByStaticMaxFlows(DefaultDirectedGraph network,
			IdentifiableIntegerMapping<Edge> transitTimes,
			IdentifiableIntegerMapping<Edge> edgeCapacities,
			IdentifiableIntegerMapping<Node> supplies) {
				
		if ( ALGO_PROGRESS){
			System.out.println("Bound calculation by static max flow started.");
		}
		LinkedList<Node> sources = new LinkedList<Node>();
		LinkedList<Node> sinks = new LinkedList<Node>();
		int maxSupply = 0;
		Node sink = null;
		for (Node node : network.nodes()){
			if (supplies.get(node)<0){
				if (sink!= null)
					throw new AssertionError(GraphLocalization.LOC.getString ("algo.graph.dynamicflow.OnlyOneSinkException"));
				if (sink==null)
					sink = node;
			}
			if (supplies.get(node)>0){
				sources.add(node);
				if (supplies.get(node)>maxSupply)
					maxSupply = supplies.get(node);
			}
		}
		sinks.add(sink);
		
		IdentifiableIntegerMapping<Node> restSupplies = new IdentifiableIntegerMapping<Node>(supplies.getDomainSize());
		for (Node n : network.nodes()){
			restSupplies.set(n, supplies.get(n));
		}
		
		int maxLength = 0; //int test = 0;
		int sumOfMaxSupplies=0;
		
		//System.out.println(restSupplies);
		IdentifiableIntegerMapping<Node> flowLeavingSource;

		do {
			flowLeavingSource = new IdentifiableIntegerMapping<Node>(supplies.getDomainSize());
			int maxSupplyInThisRun=0;
			if (BOUND_ESTIMATOR_STATIC_FLOW){
				System.out.println("New run");
			}
			if ( ALGO_PROGRESS){
				System.out.println("New run of the max flow algorithm starts.");
			}
			StaticTransshipment staticTransshipment = new StaticTransshipment(network, edgeCapacities, restSupplies);
			staticTransshipment.run();
			IdentifiableIntegerMapping<Edge> staticFlow = staticTransshipment.getFlowEvenIfNotFeasible();
			for (Edge e:network.edges()){
				Edge f = network.getEdge(e.end(), e.start());
				if (f != null){
					int m = Math.min(staticFlow.get(e),staticFlow.get(f));
					staticFlow.decrease(e, m);
					staticFlow.decrease(f,m);
				}
			}
			//System.out.println(staticFlow);
		
			PathBasedFlow staticFlowAsPaths = PathDecomposition.calculatePathDecomposition(network, supplies, sources, sinks, staticFlow);
		
			for (StaticFlowPath staticPathFlow : staticFlowAsPaths){
				int length = 0;
				//test += staticPathFlow.getAmount();
				if (BOUND_ESTIMATOR_STATIC_FLOW){
					System.out.println("Source: "+staticPathFlow.firstEdge().start()+" Amount: "+staticPathFlow.getAmount());
				}
				if (ALGO_PROGRESS){
					System.out.println("Removed "+staticPathFlow.getAmount()+" supply from "+staticPathFlow.firstEdge().start()+". ");
				}
			//	System.out.println(staticPathFlow);
				restSupplies.decrease(staticPathFlow.firstEdge().start(),staticPathFlow.getAmount());
				flowLeavingSource.increase(staticPathFlow.firstEdge().start(), staticPathFlow.getAmount());
				int hasLeft = flowLeavingSource.get(staticPathFlow.firstEdge().start());
				if (hasLeft > maxSupplyInThisRun){
					maxSupplyInThisRun = hasLeft;
				}
				if (restSupplies.get(staticPathFlow.firstEdge().start())==0){
					sources.remove(staticPathFlow.firstEdge().start());
				}
				restSupplies.increase(sink, staticPathFlow.getAmount());
				if (BOUND_ESTIMATOR_STATIC_FLOW){
					System.out.println("Sink: "+restSupplies.get(sink));
				}
				for (Edge edge : staticPathFlow){
					length += transitTimes.get(edge);
				}
				if (length > maxLength){
					maxLength = length;
				}
			}
			if (BOUND_ESTIMATOR_STATIC_FLOW){
				System.out.println("max supply in this run "+maxSupplyInThisRun);
				System.out.println("max length "+maxLength);
			}
			sumOfMaxSupplies += maxSupplyInThisRun;
			//System.out.println(restSupplies);
			if (BOUND_ESTIMATOR_STATIC_FLOW){
				System.out.println(sources);
			}
			if (ALGO_PROGRESS){
				System.out.println("Max flow run finished. Remaining supplies: "+(-restSupplies.get(sink)));
			}
		} while (restSupplies.get(sink) < 0);
		if (BOUND_ESTIMATOR_STATIC_FLOW){
			System.out.println("sum of max "+sumOfMaxSupplies);
		}
		
		return maxLength + sumOfMaxSupplies + 1;
	}
	
	public static int calculateBoundByStaticTransshipmentAndScaleFactorSearch(DefaultDirectedGraph network,
			IdentifiableIntegerMapping<Edge> transitTimes,
			IdentifiableIntegerMapping<Edge> edgeCapacities,
			IdentifiableIntegerMapping<Node> supplies) {
		
		/* Find sink and sources and the maximal supply. Create a supply mapping that is 1 for all sources, -1 for the sink and 0 else. */
		Node sink = null;
		IdentifiableIntegerMapping<Node> oneSupplies = new IdentifiableIntegerMapping<>(supplies.getDomainSize());
		LinkedList<Node> sources = new LinkedList<>();
		LinkedList<Node> sinks = new LinkedList<>();
		int maxSupply = 0;
		for (Node node : network.nodes()){
			if (supplies.get(node)<0){
				if (sink!= null)
					throw new AssertionError(GraphLocalization.LOC.getString ("algo.graph.dynamicflow.OnlyOneSinkException"));
				if (sink==null)
					sink = node;
			}
			if (supplies.get(node)>0){
				oneSupplies.set(node, 1);
				sources.add(node);
				if (supplies.get(node)>maxSupply)
					maxSupply = supplies.get(node);
			}
		}
		oneSupplies.set(sink, - sources.size());
		sinks.add(sink);

		/* Initialization */
		int upperBound = sources.size()+1;
		int left=1, right=upperBound;		
		StaticTransshipment staticTransshipment = null;
		IdentifiableIntegerMapping<Edge> staticFlow = null;
		IdentifiableIntegerMapping<Edge> resultStaticFlow = null;
	
		/* Do geometric search: */
	
		/* Use the static transshipment algorithm to check whether 1 is sufficient as capacity scale factor. */
		/* -> no multiplying of edgeCapacities */
		staticTransshipment = new StaticTransshipment(network, edgeCapacities, oneSupplies);
		staticTransshipment.run();
		staticFlow = staticTransshipment.getFlow();
	
		boolean found = false;
		int nonFeasibleT = 0;
		int feasibleT = -1;

		if (staticFlow == null)
			nonFeasibleT = 1;
		else {
			nonFeasibleT = 0;
			feasibleT = 1;
			found = true;
		}
		
		while (!found) {
			int testScaleFactor = (nonFeasibleT*2);
			if (testScaleFactor >= upperBound){
				feasibleT = upperBound;
				found = true;
			} else {
	
				/* Create a mapping where all capacities are multiplied by testScaleFactor */
				IdentifiableIntegerMapping<Edge> multipliedCapacities = new IdentifiableIntegerMapping<Edge>(edgeCapacities.getDomainSize());
				for (Edge edge:network.edges()){
					int eCap = edgeCapacities.get(edge);
					if (eCap >= Math.floor(Integer.MAX_VALUE / testScaleFactor))
						multipliedCapacities.set(edge,Integer.MAX_VALUE);
					else
						multipliedCapacities.set(edge, edgeCapacities.get(edge)*testScaleFactor);
				}
				staticTransshipment = new StaticTransshipment(network, multipliedCapacities, oneSupplies);
				staticTransshipment.run();
				staticFlow = staticTransshipment.getFlow();
				if (staticFlow == null)
					nonFeasibleT = testScaleFactor;
				else {
					feasibleT = testScaleFactor;
					found = true;
				}	
			}
		}
	
		left = nonFeasibleT;
		right = Math.min(feasibleT+1, upperBound);
	
		/* Do binary search: */
		do {
		
			/* Compute the middle of the search intervall. */
			int testScaleFactor = (left + right) / 2;
		
			/* Use the specific transshipment algorithm to check whether testTimeHorizon is sufficient. */
			/* Create a mapping where all capacities are multiplied by testScaleFactor */
			IdentifiableIntegerMapping<Edge> multipliedCapacities = new IdentifiableIntegerMapping<Edge>(edgeCapacities.getDomainSize());
			for (Edge edge:network.edges()){
				int eCap = edgeCapacities.get(edge);
				if (eCap >= Math.floor(Integer.MAX_VALUE / testScaleFactor))
					multipliedCapacities.set(edge,Integer.MAX_VALUE);
				else
					multipliedCapacities.set(edge, edgeCapacities.get(edge)*testScaleFactor);
			}
			staticTransshipment = new StaticTransshipment(network, multipliedCapacities, oneSupplies);
			staticTransshipment.run();
			staticFlow = staticTransshipment.getFlow();
			
			/* If the time horizon is sufficient, adjust left border, else adjust right border of the intervall.*/
			if (staticFlow == null)
				left = testScaleFactor;
			else {
				right = testScaleFactor;
				resultStaticFlow = staticFlow;
			}
		
		} while (left < right - 1); /* Stop if the borders reach each other. */

		/* If a transshipment was found print the result. */
		if (left == right - 1 && resultStaticFlow != null) {
			PathBasedFlow pathFlows = PathDecomposition.calculatePathDecomposition(network, supplies, sources, sinks, staticFlow);
			
			int maxLength = 0;
			for (StaticFlowPath staticPathFlow : pathFlows){
				int length = 0;
				for (Edge edge : staticPathFlow){
					length += transitTimes.get(edge);
				}
				if (length > maxLength)
					maxLength = length;
			}
			
			if (BOUND_ESTIMATOR_LONG) {
				System.out.println("Path decomposition: " + pathFlows);
			}
			if (BOUND_ESTIMATOR){
				System.out.println();
				System.out.println("Max Length: " + maxLength + " Max Supply: "
						+ maxSupply + " Sum:" + (maxLength + maxSupply)
						* sources.size());
				System.out.println();
			}
			
			// +1 because upper bound itself is never tested.
			return ((maxLength + maxSupply)* right+1);
		}
		
		throw new AssertionError("Binary search found no working testScaleFactor.");
	}
	
	public static int calculateBoundByStaticTransshipment(DefaultDirectedGraph network,
			IdentifiableIntegerMapping<Edge> transitTimes,
			IdentifiableIntegerMapping<Edge> edgeCapacities,
			IdentifiableIntegerMapping<Node> supplies) {		
		
		/* Find sink and sources and the maximal supply. Create a supply mapping that is 1 for all sources, -1 for the sink and 0 else. */
		Node sink = null;
		IdentifiableIntegerMapping<Node> oneSupplies = new IdentifiableIntegerMapping<>(supplies.getDomainSize());
		LinkedList<Node> sources = new LinkedList<>();
		LinkedList<Node> sinks = new LinkedList<>();
		int maxSupply = 0;
		for (Node node : network.nodes()){
			if (supplies.get(node)<0){
				if (sink!= null)
					throw new AssertionError(GraphLocalization.LOC.getString ("algo.graph.dynamicflow.OnlyOneSinkException"));
				if (sink==null)
					sink = node;
			}
			if (supplies.get(node)>0){
				oneSupplies.set(node, 1);
				sources.add(node);
				if (supplies.get(node)>maxSupply)
					maxSupply = supplies.get(node);
			}
		}
		oneSupplies.set(sink, - sources.size());
		sinks.add(sink);
		
		/* Create a mapping where all capacities are multiplied by maxSupply */
		IdentifiableIntegerMapping<Edge> multipliedCapacities = new IdentifiableIntegerMapping<Edge>(edgeCapacities.getDomainSize());
		for (Edge edge:network.edges()){
			int eCap = edgeCapacities.get(edge);
			if (eCap >= Math.floor(Integer.MAX_VALUE / sources.size()))
				multipliedCapacities.set(edge,Integer.MAX_VALUE);
			else
				multipliedCapacities.set(edge, edgeCapacities.get(edge)*sources.size());
		}
		if (BOUND_ESTIMATOR_LONG){
			System.out.println();
			System.out.println();
			System.out.println("network: "+network);
			System.out.println("capacities: "+ edgeCapacities);
			System.out.println("multipliedCapacities: "+multipliedCapacities);
			System.out.println("supplies: "+supplies);
			System.out.println("oneSupplies "+oneSupplies);
		}
		if (ALGO_PROGRESS){
			System.out.println("Progress: Static transshipment algorithm is called for calculation of upper bound for time horizon..");
		}
		StaticTransshipment staticTransshipment = new StaticTransshipment(network, multipliedCapacities, oneSupplies);
		staticTransshipment.run();
		IdentifiableIntegerMapping<Edge> staticFlow = staticTransshipment.getFlow();
		if (ALGO_PROGRESS){
			System.out.println("Progress: .. call of static transshipment algorithm finished.");
		}
		if (BOUND_ESTIMATOR_LONG)
			System.out.println("Calculated static flow for upper bound: "+staticFlow);
		PathBasedFlow pathFlows = PathDecomposition.calculatePathDecomposition(network, supplies, sources, sinks, staticFlow);
		
		int maxLength = 0;
		for (StaticFlowPath staticPathFlow : pathFlows){
			int length = 0;
			for (Edge edge : staticPathFlow){
				length += transitTimes.get(edge);
			}
			if (length > maxLength)
				maxLength = length;
		}
		
		if (BOUND_ESTIMATOR_LONG) {
			System.out.println("Path decomposition: " + pathFlows);
		}
		if (BOUND_ESTIMATOR){
			System.out.println();
			System.out.println("Max Length: " + maxLength + " Max Supply: "
					+ maxSupply + " Sum:" + (maxLength + maxSupply)
					* sources.size());
			System.out.println();
		}
		
		// +1 because upper bound itself is never tested.
		return ((maxLength + maxSupply)* sources.size()+1);
	}
	
	public static int calculateBound(DirectedGraph network,
			IdentifiableIntegerMapping<Edge> transitTimes,
			IdentifiableIntegerMapping<Edge> edgeCapacities,
			IdentifiableIntegerMapping<Node> supplies){
		if (BOUND_ESTIMATOR){
			System.out.println("");
		}
		int c = Integer.MAX_VALUE;//calculateBoundByStaticTransshipment(network, transitTimes, edgeCapacities, supplies);
		int a = Integer.MAX_VALUE;//calculateBoundByStaticTransshipmentAndScaleFactorSearch(network, transitTimes, edgeCapacities, supplies);
		int b =calculateBoundByLongestPath(network, transitTimes, edgeCapacities, supplies);
		int d = Integer.MAX_VALUE;//calculateBoundByStaticMaxFlows(network, transitTimes, edgeCapacities, supplies);
		System.out.println("Bounds calculated: "+a+" "+b+" "+c+" "+d);
	    return Math.min(a, Math.min(b,c));
	}
	
}
