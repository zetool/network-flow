
package org.zetool.netflow.ds.network;

import org.zetool.container.mapping.IdentifiableIntegerMapping;
import org.zetool.graph.Edge;
import org.zetool.graph.Graph;
import org.zetool.graph.Node;
import java.util.Collection;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface Network extends Graph {
  Graph getGraph();

  Collection<Node> sinks();

  Collection<Node> sources();

  int sinkCount();

  int sourceCount();

  int getCapacity( Edge e );

  IdentifiableIntegerMapping<Edge> getCapacities();

}
