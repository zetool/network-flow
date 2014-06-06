
package de.tu_berlin.coga.netflow.ds.network;

import de.tu_berlin.coga.container.mapping.IdentifiableIntegerMapping;
import de.tu_berlin.coga.graph.DirectedGraph;
import de.tu_berlin.coga.graph.Edge;
import de.tu_berlin.coga.graph.Node;
import java.util.Collection;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface  Network extends DirectedGraph {
  DirectedGraph getGraph();

  Collection<Node> sinks();

  Collection<Node> sources();

  int sinkCount();

  int sourceCount();

  int getCapacity( Edge e );

  IdentifiableIntegerMapping<Edge> getCapacities();

}
