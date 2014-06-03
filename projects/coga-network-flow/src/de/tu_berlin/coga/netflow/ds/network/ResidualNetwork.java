/* zet evacuation tool copyright (c) 2007-10 zet evacuation team
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
 * ResidualNetwork.java
 *
 */
package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.netflow.ds.network.NetworkInterface;
import de.tu_berlin.coga.netflow.ds.network.Network;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.container.mapping.Mappings;
import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;

/**
 * The {@code ResidualNetwork} class provides flow algorithms with the
 * functionality to create and work with residual networks. The residual
 * networks implemented by this class are based on the {@link AbstractNetwork} class and
 * make use of the speed of its static implementation as well as its ability to
 * hide nodes and edges.
 */
public class ResidualNetwork extends Network {

	/** The underlying base network. */
	protected NetworkInterface network;
	/**  The number of edges that the original AbstractNetwork had (without the residual edges). */
	private int originalNumberOfEdges;
	/** The flow associated with this residual network. */
	protected IdentifiableIntegerMapping<Edge> flow;
	/** The residual capacities of this residual network. */
	protected IdentifiableIntegerMapping<Edge> residualCapacities;
	/** The residual transit times of this residual network. */
	private IdentifiableIntegerMapping<Edge> residualTransitTimes;

	/**
	 * A constructor for clone and overriding classes.
	 * @param initialNodeCapacity
	 * @param initialEdgeCapacity
	 */
	protected ResidualNetwork( int initialNodeCapacity, int initialEdgeCapacity ) {
		super( initialNodeCapacity, initialEdgeCapacity );

		originalNumberOfEdges = initialEdgeCapacity;
	}

	/**
	 * Creates a new residual network, based on the specified network, the
	 * zero flow and the specidied capacities.
	 * @param network the base network for the residual network.
	 * @param capacities the base capacities for the residual network.
	 */
	public ResidualNetwork( NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities ) {
		super( 0, 0 );
		if( network instanceof Network ) {
			setUp( (Network)network, capacities );
		} else
			setUp( network, capacities );


	}

	private void setUp( Network network, IdentifiableIntegerMapping<Edge> capacities ) {
		setNodeCapacity( network.allNumberOfNodes() );
		setEdgeCapacity( network.allNumberOfEdges() * 2 );
		originalNumberOfEdges = network.allNumberOfEdges();

		IdentifiableCollection<Edge> ne = network.allEdges();

		setNodes( network.allNodes() );
		setEdges( network.allEdges() );
		for( Edge edge : network.allEdges() ) {
			Edge rev = createAndSetEdge( edge.end(), edge.start() );
			if( network.isHidden( edge ) ) {
				System.out.println( "Created a residual arc for a hidden arc" );
				setHidden( edge, true );
				setHidden( rev, true );
			}
		}

		this.network = network;
		flow = new IdentifiableIntegerMapping<>( network.allNumberOfEdges() );
		residualCapacities = new IdentifiableIntegerMapping<>( network.allNumberOfEdges() * 2 );
		for( Edge edge : edges.getAll() )
			if( isReverseEdge( edge ) ) {
				residualCapacities.set( edge, 0 );
				setHidden( edge, true );
			} else {
				flow.set( edge, 0 );
				residualCapacities.set( edge, capacities.get( edge ) );
			}
	}

	/**
	 * Updates hidden edges in the residual network. Call this, if the network
	 * has changed and another iteration of a flow algorithm should be called
	 */
	public void update() {
		if( !(network instanceof Network) )
			return; // No hidden edges and no update!
		Network n = (Network)network;
		for( Edge edge : n.allEdges() ) {
			Edge rev = allGetEdge( edge.end(), edge.start() );
			if( rev == null )
				throw new IllegalStateException( "Reverse edge is null! (Hidden?)" );
			if( n.isHidden( edge ) ) {
				setHidden( edge, true );
				setHidden( rev, true);
			} else {
				if( residualCapacities.get( edge ) > 0 )
					setHidden( edge, false );
				else
					setHidden( edge, true );
				if( residualCapacities.get( rev ) > 0 )
					setHidden( rev, false );
				else
					setHidden( rev, true );
			}
		}

	}


	private void setUp( NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities  ) {
		setNodeCapacity( network.nodeCount() );
		setEdgeCapacity( network.edgeCount() * 2 );
		originalNumberOfEdges = network.edgeCount();

		setNodes( network.nodes() );
		setEdges( network.edges() );
		for( Edge edge : network.edges() )
			createAndSetEdge( edge.end(), edge.start() );
		this.network = network;
		flow = new IdentifiableIntegerMapping<>( network.edgeCount() );
		residualCapacities = new IdentifiableIntegerMapping<>( network.edgeCount() * 2 );
		for( Edge edge : edges )
			if( isReverseEdge( edge ) ) {
				residualCapacities.set( edge, 0 );
				setHidden( edge, true );
			} else {
				flow.set( edge, 0 );
				residualCapacities.set( edge, capacities.get( edge ) );
			}
	}

	/**
	 * Creates a new residual network, based on the specified network, the
	 * zero flow and the specidied capacities and transit times.
	 * @param network the base network for the residual network.
	 * @param capacities the base capacities for the residual network.
	 * @param transitTimes the base transit times for the residual network.
	 */
	public ResidualNetwork( NetworkInterface network, IdentifiableIntegerMapping<Edge> capacities, IdentifiableIntegerMapping<Edge> transitTimes ) {
		this( network, capacities );
		residualTransitTimes = expandCostFunction( transitTimes );
		/*
		residualTransitTimes = new IdentifiableIntegerMapping<Edge>(network.edgeCount() * 2);
		for (Edge edge : edges) {
		System.out.println(edge);
		if (isReverseEdge(edge)) {
		residualTransitTimes.set(edge, -transitTimes.get(edge));
		} else {
		residualTransitTimes.set(edge, transitTimes.get(edge));
		}
		}*/
	}

	/**
	 * Augments a specified amount of flow along the specified edge. The
	 * residual capacities of the edge and its reverse edge are updated
	 * automatically. The residual network is updated as well, if neccessary.
	 * Runtime O(1).
	 * @param edge the edge along which flow is to be augmented.
	 * @param amount the amount of flow to augment.
	 */
	public void augmentFlow( Edge edge, int amount ) {
		Edge reverseEdge = reverseEdge( edge );
		if( isReverseEdge( edge ) ) {
			flow.decrease( reverseEdge, amount );
			//System.out.println( "FLOW on EDGE " + reverseEdge + " set to " + flow.get( reverseEdge ) );
		} else {
			flow.increase( edge, amount );
			//System.out.println( "FLOW on EDGE " + edge + " set to " + flow.get( edge ) );
		}
		residualCapacities.decrease( edge, amount );
		residualCapacities.increase( reverseEdge, amount );
		if( 0 == residualCapacities.get( edge ) )
			setHidden( edge, true );
		if( 0 < residualCapacities.get( reverseEdge ) )
			setHidden( reverseEdge, false );
	}

	/**
	 * Returns the capacities of the edges in this residual network (with regard
	 * to the flow associated with the network). Runtime O(1).
	 * @return the capacities of the edges in this residual network.
	 */
	public IdentifiableIntegerMapping<Edge> residualCapacities() {
		return residualCapacities;
	}

	/**
	 * Returns the transit times in this residual network (with regard
	 * to the flow associated with the network). Returns {@code null} if
	 * this network has been created without transit times. Runtime O(1).
	 * @return the transit times in this residual network.
	 */
	public IdentifiableIntegerMapping<Edge> residualTransitTimes() {
		return residualTransitTimes;
	}

	/**
	 * Returns the flow associated with this residual network. Runtime O(1).
	 * @return the flow associated with this residual network.
	 */
	public IdentifiableIntegerMapping<Edge> flow() {
		return flow;
	}

	/**
	 * Returns the reverse edge of the specified edge. Runtime O(1).
	 * @param edge the edge for which the reverse edge is to be returned.
	 * @return the reverse edge of the specified edge. Runtime O(1).
	 */
	public Edge reverseEdge( Edge edge ) {
		if( edge.id() < originalNumberOfEdges )
			return edges.getEvenIfHidden( edge.id() + originalNumberOfEdges );
		else
			return edges.getEvenIfHidden( edge.id() - originalNumberOfEdges );
	}

	/**
	 * Checks is whether the specified edge is a reverse edge. An edge is called
	 * reverse if it does not exist in the original network. Runtime O(1).
	 * @param edge the edge to be tested.
	 * @return {@code true} if the specified edge is a reverse edge,
	 * {@code false} otherwise.
	 */
	public boolean isReverseEdge( Edge edge ) {
		return edge.id() >= originalNumberOfEdges;
	}

	/**
	 * This method expand the given cost function over some network to cover
	 * also the residual network
	 * @param costs The old cost function to be expanded
	 * @return an new costs function that is identical with the old function
	 * on the old domain. On all other edges in the residual network it returns
	 * either the ngated cost of the oposite edge if it exists or 0.
	 */
	public IdentifiableIntegerMapping<Edge> expandCostFunction( IdentifiableIntegerMapping<Edge> costs ) {
		IdentifiableIntegerMapping<Edge> result = new IdentifiableIntegerMapping<>( getEdgeCapacity() );
		for( int id = 0; id < getEdgeCapacity(); id++ ) {
			Edge edge = edges.getEvenIfHidden( id );
			if( isReverseEdge( edge ) )
				result.set( edge, -costs.get( reverseEdge( edge ) ) );
			else
				result.set( edge, costs.get( edge ) );
		}
		return result;
	}

	/**
	 * Creates a copy of this residual network.
	 * @return a copy of this residual network.
	 */
	@Override
	public ResidualNetwork clone() {
		ResidualNetwork clone = new ResidualNetwork( getNodeCapacity(), getEdgeCapacity() );
		boolean[] hidden = new boolean[getEdgeCapacity()];
		for( int i = 0; i < getEdgeCapacity(); i++ )
			hidden[i] = edges.isHidden( i );
		edges.showAll();
		clone.setNodes( nodes );
		clone.setEdges( edges );
		for( int i = 0; i < getEdgeCapacity(); i++ ) {
			edges.setHidden( i, hidden[i] );
			clone.edges.setHidden( i, hidden[i] );
		}
		clone.network = network;
		clone.flow = flow.clone();
		clone.residualCapacities = residualCapacities.clone();
		if( residualTransitTimes != null )
			clone.residualTransitTimes = residualTransitTimes.clone();
		return clone;
	}

	/**
	 * Compares this object with the specified object. If the specified object
	 * is equivalent to this one {@code true} is returned, {@code false
	 * } otherwise. A object is considered equivalent if and only if it is
	 * a residual network with equals components (nodes, edges, base network,
	 * flow, ...). Runtime O(n + m).
	 * @param o the object to compare this one to.
	 * @return {@code true} if the specified object
	 * is equivalent to this one, {@code false
	 * } otherwise.
	 */
	@Override
	public boolean equals( Object o ) {
		if( o instanceof ResidualNetwork ) {
			ResidualNetwork rn = (ResidualNetwork) o;
			if( residualTransitTimes == null )
				return network.equals( rn.network ) && residualCapacities.equals( rn.residualCapacities ) && super.equals( o );
			else
				return network.equals( rn.network ) && residualCapacities.equals( rn.residualCapacities ) && residualTransitTimes.equals( rn.residualTransitTimes );
		} else
			return false;
	}

	/**
	 * Returns a hash code for this residual network.
	 * Runtime O(n + m).
	 * @return a hash code computed by the sum of the hash codes of its
	 * components.
	 */
	@Override
	public int hashCode() {
		int hashCode = super.hashCode() + network.hashCode() + residualCapacities.hashCode();
		return hashCode;
	}

	/**
	 * Returns a string representation of this residual network. The
	 * representation consists of the underlying base network, the nodes and
	 * edges of this residual network and its residual capacities
	 * (and transit times, if it has them). Runtime O(n + m).
	 * @return  a string representation of this residual network.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Base network: " ).append( network.toString() ).append( "\n" );
		builder.append( "Residual network: " ).append( super.toString() ).append( "\n" );
		builder.append( "Residual capacities: " ).append( Mappings.toString( edges(), residualCapacities ) ).append( "\n" );
		if( residualTransitTimes != null )
			builder.append( "Residual transit times: " ).append( residualTransitTimes.toString() ).append( "\n" );
		return builder.toString();
	}
}
