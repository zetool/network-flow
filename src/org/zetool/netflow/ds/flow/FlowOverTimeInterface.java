
package org.zetool.netflow.ds.flow;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface FlowOverTimeInterface {
    public EdgeBasedFlowOverTime getEdgeBased();

    public PathBasedFlowOverTime getPathBased();

}
