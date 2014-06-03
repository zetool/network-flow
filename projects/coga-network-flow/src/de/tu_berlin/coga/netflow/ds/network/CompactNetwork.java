/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.container.collection.IdentifiableCollection;
import de.tu_berlin.coga.graph.Node;

/**
 *
 * @author Martin
 */
public class CompactNetwork {

    private int[] nodeStartIndices;
    private int[] edgeEndIDs;
    private int[] edgeCapacities;

    public boolean isDirected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IdentifiableCollection<Edge> edges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IdentifiableCollection<Node> nodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int numberOfEdges() {
        return edgeEndIDs.length;
    }

    public int numberOfNodes() {
        return nodeStartIndices.length;
    }

    public int outdegree(int node) {
        return ((node+1 == nodeStartIndices.length)? edgeEndIDs.length : nodeStartIndices[node+1]) - nodeStartIndices[node];
    }
}
