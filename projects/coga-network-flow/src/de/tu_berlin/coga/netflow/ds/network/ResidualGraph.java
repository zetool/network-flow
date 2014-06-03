/*
 * ResidualGraph.java
 *
 */
package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.mapping.IdentifiableDoubleMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import ds.graph.StaticGraph;

/**
 *
 * @author Martin Groß / Sebastian Schenker
 */
public class ResidualGraph extends StaticGraph {

	private DirectedGraph graph;

	private int maxnodeid; //maximal id of nodes in original graph
	private int maxedgeid; //maximal id of edges in original graph

	private IdentifiableDoubleMapping<Edge> flow;
	private IdentifiableDoubleMapping<Edge> residualCapacities;
	private IdentifiableDoubleMapping<Edge> residualTransitTimes;

	public ResidualGraph( DirectedGraph gr, IdentifiableDoubleMapping<Edge> capacities, int mnodeid, int medgeid ) {
		super( true, (mnodeid + 1), (medgeid + 1) * 2 );
		maxnodeid = mnodeid;
		maxedgeid = medgeid;
		graph = gr;

		setNodes( gr.nodes() );
		setEdges( gr.edges() );

		for( Edge edge : gr.edges() ) {
			createEdge( edge.end(), edge.start(), edge.id() + (maxedgeid + 1) );
		}

		flow = new IdentifiableDoubleMapping<>( gr.edgeCount() );

		residualCapacities = new IdentifiableDoubleMapping<>( gr.edgeCount() * 2 );

		for( Edge edge : edges ) {

			if( isReverseEdge( edge ) ) {
				residualCapacities.set( edge, 0 );
				changeVisibility( edge, false );

			} else {
				changeVisibility( edge, true );
				flow.set( edge, 0.0 );
				residualCapacities.set( edge, capacities.get( edge ) );
			}

		}

	}

	public void augmentFlow( Edge edge, double amount ) {

		Edge reverseEdge = getReverseEdge( edge );

		if( isReverseEdge( edge ) ) {
			flow.decrease( reverseEdge, amount );
		} else {
			flow.increase( edge, amount );
		}
		residualCapacities.decrease( edge, amount );
		residualCapacities.increase( reverseEdge, amount );
		if( eq( residualCapacities.get( edge ), 0.0 ) ) {
			//setHidden(edge, true);
			changeVisibility( edge, false );
		}
		if( less( 0.0, residualCapacities.get( reverseEdge ) ) ) {
			//setHidden(reverseEdge, false);
			changeVisibility( reverseEdge, true );
		}
	}

	public IdentifiableDoubleMapping<Edge> getResidualCapacities() {
		return residualCapacities;
	}

	public IdentifiableDoubleMapping<Edge> getResidualTransitTimes() {
		return residualTransitTimes;
	}

	public IdentifiableDoubleMapping<Edge> getFlow() {
		return flow;
	}

	public DirectedGraph getGraph() {
		return graph;
	}

	public Edge getReverseEdge( Edge edge ) {
		if( edge.id() <= maxedgeid ) {       //changed by Sebastian: edge.id() < graph.edgeCount()
			return edges.get( edge.id() + (maxedgeid + 1) );
		} else {
			return edges.get( edge.id() - (maxedgeid + 1) );
		}
	}

	public boolean isReverseEdge( Edge edge ) {

		return (edge.id() > maxedgeid); //changed by Sebastian: edge.id() >= graph.edgeCount();
	}

	public static boolean eq( double x, double y ) {
		return Math.abs( x - y ) < 10E-9;
	}

	public static boolean less( double x, double y ) {
		return x + 10E-9 < y;
	}

}
