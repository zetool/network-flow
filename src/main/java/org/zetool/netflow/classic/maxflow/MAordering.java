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

import org.zetool.graph.Node;
import java.util.List;
import java.util.ArrayList;
import org.zetool.graph.Edge;
import java.util.HashMap;
import org.zetool.netflow.ds.network.rational.RationalResidualGraph;
import org.zetool.container.mapping.IdentifiableDoubleMapping;

/**
 *
 * @author Sebastian Schenker
 */
public class MAordering {

	private RationalResidualGraph graph;
	private IdentifiableDoubleMapping<Edge> capacities;
	private Node source, sink;
	private IdentifiableDoubleMapping<Node> demands;
	private List<Node> ordering;
	private HashMap<Node, List<Edge>> nodelist;
	private List<Node> VminusW;
	private int iterations;

	public MAordering( RationalResidualGraph resGraph, Node s, Node t, IdentifiableDoubleMapping<Edge> cap ) {
		graph = resGraph;
		capacities = cap;
		source = s;
		sink = t;
		demands = new IdentifiableDoubleMapping<>( resGraph.nodeCount() );
		VminusW = new ArrayList<>( resGraph.nodeCount() );
		nodelist = new HashMap<>( resGraph.nodeCount() );
		for( Node n : resGraph.nodes() ) {
			demands.set( n, 0.0 );
			nodelist.put( n, new ArrayList<>() );

			if( n != source ) {
				VminusW.add( n );
			}
		}

		ordering = new ArrayList<Node>( resGraph.nodeCount() );
		iterations = 0;
	}

	public List<Node> getMAordering() {
		return ordering;
	}

	public List<Edge> getEdgeList( Node n ) {
		return nodelist.get( n );
	}

	public int getIterations() {
		return iterations;
	}

	public Double getDemand( Node n ) {
		return demands.get( n );
	}

	public void computeMAordering() {
		Node currentNode = source;
		ordering.add( source );
		Node endNode, maxNode;
		double maxDemandValue, value;
		while( currentNode != sink ) {
			for( Edge e : graph.outgoingEdges( currentNode ) ) {
				endNode = e.end();
				if( VminusW.contains( endNode ) ) {
					demands.increase( endNode, capacities.get( e ) );
					nodelist.get( endNode ).add( e );

				}
			}
			maxNode = null;
			maxDemandValue = -1.0;
			for( Node n : VminusW ) {
				if( (value = demands.get( n )) > maxDemandValue ) {
					maxDemandValue = value;
					maxNode = n;
				}
			}

			currentNode = maxNode;
			++iterations;
			ordering.add( currentNode );
			VminusW.remove( currentNode );
		}
	}

}
