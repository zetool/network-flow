/**
 * FlowOverTimeInterface.java
 * Created: 07.06.2012, 16:21:16
 */
package de.tu_berlin.coga.netflow.ds.flow;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public abstract class FlowOverTimeInterface {
	public abstract EdgeBasedFlowOverTime getEdgeBased();

	public abstract PathBasedFlowOverTime getPathBased();
}
