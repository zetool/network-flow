/*
 * ChainDecompositionProblem.java
 *
 */

package org.zetool.netflow.dynamic.problems;

import org.zetool.netflow.ds.network.ImplicitTimeExpandedResidualNetwork;
import org.zetool.netflow.ds.structure.FlowOverTimeEdgeSequence;
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
