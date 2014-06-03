/**
 * ExtendedNetwork.java
 * Created: 09.12.2011, 16:36:14
 */
package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.graph.Node;

/**
 * A special {@code Network} extending an static network, i.e. a network that
 * cannot be changed, such that it can be modified by adding some sources and
 * edges.
 * @author Jan-Philipp Kappmeier
 */
public class ExtendedNetwork extends Network {
	private int newNodes;
	private int newEdges;
	private int originalEdgeCount;
	private int originalNodeCount;
	
	public ExtendedNetwork( Network network, int newNodes, int newEdges ) {
		super( network );
		originalNodeCount = network.getNodeCapacity();
		network.setNodeCapacity( network.getNodeCapacity() + newNodes );
		originalEdgeCount = network.getEdgeCapacity();
		network.setEdgeCapacity( network.getEdgeCapacity() + newEdges );
	}

	public Node getFirstNewNode() {
		return this.getNode( originalNodeCount );
	}

	public int getFirstNewEdgeIndex() {
		return originalEdgeCount;
	}
	
	public void undo() {
		setNodeCapacity( originalNodeCount );
		setEdgeCapacity( originalEdgeCount );
	}
}
