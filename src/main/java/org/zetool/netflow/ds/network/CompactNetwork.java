/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zetool.netflow.ds.network;

import org.zetool.graph.Edge;
import org.zetool.container.collection.IdentifiableCollection;
import org.zetool.graph.Node;

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
