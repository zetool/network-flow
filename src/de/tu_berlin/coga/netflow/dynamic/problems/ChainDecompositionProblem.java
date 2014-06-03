/*
 * ChainDecompositionProblem.java
 *
 */

package de.tu_berlin.coga.netflow.dynamic.problems;

import de.tu_berlin.coga.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import de.tu_berlin.coga.netflow.ds.structure.FlowOverTimeEdgeSequence;
import java.util.List;

/**
 *
 * @author Martin Groß
 */
public class ChainDecompositionProblem {

    private List<FlowOverTimeEdgeSequence> edgeSequences;
    private ImplicitTimeExpandedResidualNetwork network;

    public ChainDecompositionProblem(List<FlowOverTimeEdgeSequence> edgeSequences, ImplicitTimeExpandedResidualNetwork network) {
        this.edgeSequences = edgeSequences;
        this.network = network;
    }

    public List<FlowOverTimeEdgeSequence> getEdgeSequences() {
        return edgeSequences;
    }

    public void setEdgeSequences(List<FlowOverTimeEdgeSequence> edgeSequences) {
        this.edgeSequences = edgeSequences;
    }

    public ImplicitTimeExpandedResidualNetwork getNetwork() {
        return network;
    }

    public void setNetwork(ImplicitTimeExpandedResidualNetwork network) {
        this.network = network;
    }
}
